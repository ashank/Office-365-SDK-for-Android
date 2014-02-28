/**
 * Copyright © Microsoft Open Technologies, Inc.
 *
 * All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A
 * PARTICULAR PURPOSE, MERCHANTABILITY OR NON-INFRINGEMENT.
 *
 * See the Apache License, Version 2.0 for the specific language
 * governing permissions and limitations under the License.
 */
package com.msopentech.odatajclient.proxy.api.impl;

import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.msopentech.odatajclient.engine.client.ODataClient;
import com.msopentech.odatajclient.engine.communication.header.ODataHeaderValues;
import com.msopentech.odatajclient.engine.communication.request.UpdateType;
import com.msopentech.odatajclient.engine.communication.request.batch.ODataBatchRequest;
import com.msopentech.odatajclient.engine.communication.request.batch.ODataBatchResponseItem;
import com.msopentech.odatajclient.engine.communication.request.batch.ODataChangeset;
import com.msopentech.odatajclient.engine.communication.request.batch.ODataChangesetResponseItem;
import com.msopentech.odatajclient.engine.communication.request.cud.ODataDeleteRequest;
import com.msopentech.odatajclient.engine.communication.request.cud.ODataEntityUpdateRequest;
import com.msopentech.odatajclient.engine.communication.request.streamed.ODataMediaEntityUpdateRequest;
import com.msopentech.odatajclient.engine.communication.request.streamed.ODataStreamUpdateRequest;
import com.msopentech.odatajclient.engine.communication.response.ODataBatchResponse;
import com.msopentech.odatajclient.engine.communication.response.ODataEntityCreateResponse;
import com.msopentech.odatajclient.engine.communication.response.ODataEntityUpdateResponse;
import com.msopentech.odatajclient.engine.communication.response.ODataResponse;
import com.msopentech.odatajclient.engine.data.ODataEntity;
import com.msopentech.odatajclient.engine.data.ODataLinkType;
import com.msopentech.odatajclient.engine.data.ODataObjectFactory;
import com.msopentech.odatajclient.engine.format.ODataMediaFormat;
import com.msopentech.odatajclient.engine.uri.URIBuilder;
import com.msopentech.odatajclient.engine.utils.URIUtils;
import com.msopentech.odatajclient.proxy.api.EntityContainerFactory;
import com.msopentech.odatajclient.proxy.api.annotations.NavigationProperty;
import com.msopentech.odatajclient.proxy.api.context.AttachedEntity;
import com.msopentech.odatajclient.proxy.api.context.AttachedEntityStatus;
import com.msopentech.odatajclient.proxy.api.context.EntityLinkDesc;
import com.msopentech.odatajclient.proxy.utils.EngineUtils;

public class BatchContainer extends Container {

    private static final long serialVersionUID = -3320312269235907501L;

    public BatchContainer(final ODataClient client, final EntityContainerFactory factory) {
        super(client, factory);
    }

    /**
     * Transactional changes commit.
     */
    @Override
    public void flush() {
        final ODataBatchRequest request = client.getBatchRequestFactory().getBatchRequest(factory.getServiceRoot());
        final ODataBatchRequest.BatchStreamManager streamManager = request.execute();
        final ODataChangeset changeset = streamManager.addChangeset();

        final TransactionItems items = new TransactionItems();
        final List<EntityLinkDesc> delayedUpdates = new ArrayList<EntityLinkDesc>();

        // what is this?
        int pos = 0;

        for (AttachedEntity attachedEntity : EntityContainerFactory.getContext().entityContext()) {
            final AttachedEntityStatus status = attachedEntity.getStatus();
            if (((status != AttachedEntityStatus.ATTACHED
                    && status != AttachedEntityStatus.LINKED) || attachedEntity.getEntity().isChanged())
                    && !items.contains(attachedEntity.getEntity())) {
                pos++;
                pos = processEntityContext(attachedEntity.getEntity(), pos, items, delayedUpdates, changeset);
            }
        }

        // why they are delayed?
        processDelayedUpdates(delayedUpdates, pos, items, changeset);

        final ODataBatchResponse response = streamManager.getResponse();

        if (response.getStatusCode() != 202) {
            throw new IllegalStateException("Operation failed");
        }

        final Iterator<ODataBatchResponseItem> iter = response.getBody();

        if (!items.isEmpty()) {
            if (!iter.hasNext()) {
                throw new IllegalStateException("Unexpected operation result");
            }

            final ODataBatchResponseItem item = iter.next();
            if (!(item instanceof ODataChangesetResponseItem)) {
                throw new IllegalStateException("Unexpected batch response item " + item.getClass().getSimpleName());
            }

            final ODataChangesetResponseItem chgres = (ODataChangesetResponseItem) item;

            for (Integer changesetItemId : items.sortedValues()) {
                LOG.debug("Expected changeset item {}", changesetItemId);
                final ODataResponse res = chgres.next();
                if (res.getStatusCode() >= 400) {
                    throw new IllegalStateException("Transaction failed: " + res.getStatusMessage());
                }

                final EntityTypeInvocationHandler handler = items.get(changesetItemId);

                if (handler != null) {
                    if (res instanceof ODataEntityCreateResponse) {
                        LOG.debug("Upgrade created object '{}'", handler);
                        handler.setEntity(((ODataEntityCreateResponse) res).getBody());
                    } else if (res instanceof ODataEntityUpdateResponse) {
                        LOG.debug("Upgrade updated object '{}'", handler);
                        handler.setEntity(((ODataEntityUpdateResponse) res).getBody());
                    }
                }
            }
        }

        EntityContainerFactory.getContext().detachAll();
    }

    private void batch(
            final EntityTypeInvocationHandler handler, final ODataEntity entity, final ODataChangeset changeset) {

        switch (EntityContainerFactory.getContext().entityContext().getStatus(handler)) {
            case NEW:
                batchCreate(handler, entity, changeset);
                break;

            case CHANGED:
                batchUpdate(handler, entity, changeset);
                break;

            case DELETED:
                batchDelete(handler, entity, changeset);
                break;

            default:
                if (handler.isChanged()) {
                    batchUpdate(handler, entity, changeset);
                }
        }
    }

    private void batchCreate(
            final EntityTypeInvocationHandler handler, final ODataEntity entity, final ODataChangeset changeset) {

        LOG.debug("Create '{}'", handler);

        final URIBuilder uriBuilder = client.getURIBuilder(factory.getServiceRoot()).
                appendEntitySetSegment(handler.getEntitySetName());
        changeset.addRequest(client.getCUDRequestFactory().getEntityCreateRequest(uriBuilder.build(), entity));
    }

    private void batchUpdateMediaEntity(
            final EntityTypeInvocationHandler handler,
            final URI uri,
            final InputStream input,
            final ODataChangeset changeset) {

        LOG.debug("Update media entity '{}'", uri);

        final ODataMediaEntityUpdateRequest req =
                client.getStreamedRequestFactory().getMediaEntityUpdateRequest(uri, input);

        req.setContentType(StringUtils.isBlank(handler.getEntity().getMediaContentType())
                ? ODataMediaFormat.WILDCARD.toString()
                : ODataMediaFormat.fromFormat(handler.getEntity().getMediaContentType()).toString());

        if (StringUtils.isNotBlank(handler.getETag())) {
            req.setIfMatch(handler.getETag());
        }

        changeset.addRequest(req);
    }

    private void batchUpdateMediaResource(
            final EntityTypeInvocationHandler handler,
            final URI uri,
            final InputStream input,
            final ODataChangeset changeset) {

        LOG.debug("Update media entity '{}'", uri);

        final ODataStreamUpdateRequest req = client.getStreamedRequestFactory().getStreamUpdateRequest(uri, input);

        if (StringUtils.isNotBlank(handler.getETag())) {
            req.setIfMatch(handler.getETag());
        }

        changeset.addRequest(req);
    }

    private void batchUpdate(
            final EntityTypeInvocationHandler handler,
            final ODataEntity changes,
            final ODataChangeset changeset) {

        LOG.debug("Update '{}'", changes.getEditLink());

        final ODataEntityUpdateRequest req =
                client.getCUDRequestFactory().getEntityUpdateRequest(UpdateType.PATCH, changes);
        req.setPrefer(ODataHeaderValues.preferReturnContent);

        if (StringUtils.isNotBlank(handler.getETag())) {
            req.setIfMatch(handler.getETag());
        }

        changeset.addRequest(req);
    }

    private void batchUpdate(
            final EntityTypeInvocationHandler handler,
            final URI uri,
            final ODataEntity changes,
            final ODataChangeset changeset) {

        LOG.debug("Update '{}'", uri);

        final ODataEntityUpdateRequest req =
                client.getCUDRequestFactory().getEntityUpdateRequest(uri, UpdateType.PATCH, changes);
        req.setPrefer(ODataHeaderValues.preferReturnContent);

        if (StringUtils.isNotBlank(handler.getETag())) {
            req.setIfMatch(handler.getETag());
        }

        changeset.addRequest(req);
    }

    private void batchDelete(
            final EntityTypeInvocationHandler handler, final ODataEntity entity, final ODataChangeset changeset) {

        LOG.debug("Delete '{}'", entity.getEditLink());

        final ODataDeleteRequest req = client.getCUDRequestFactory().getDeleteRequest(URIUtils.getURI(
                factory.getServiceRoot(), entity.getEditLink().toASCIIString()));

        if (StringUtils.isNotBlank(handler.getETag())) {
            req.setIfMatch(handler.getETag());
        }

        changeset.addRequest(req);
    }

    private int processEntityContext(
            final EntityTypeInvocationHandler handler,
            int pos,
            final TransactionItems items,
            final List<EntityLinkDesc> delayedUpdates,
            final ODataChangeset changeset) {

        LOG.debug("Process '{}'", handler);

        items.put(handler, null);

        //TODO: this change hasn't been tested as Exchange server doesn't support Batch requests.
        //final ODataEntity entity = (ODataEntity) xstream.fromXML(xstream.toXML(handler.getEntity()));
        final ODataEntity entity = handler.getEntity();

        entity.getNavigationLinks().clear();

        final AttachedEntityStatus currentStatus = EntityContainerFactory.getContext().entityContext().
                getStatus(handler);

        if (AttachedEntityStatus.DELETED != currentStatus) {
            entity.getProperties().clear();
            EngineUtils.addProperties(client, factory.getMetadata(), handler.getPropertyChanges(), entity);
        }

        for (Map.Entry<NavigationProperty, Object> property : handler.getLinkChanges().entrySet()) {
            final ODataLinkType type = Collection.class.isAssignableFrom(property.getValue().getClass())
                    ? ODataLinkType.ENTITY_SET_NAVIGATION
                    : ODataLinkType.ENTITY_NAVIGATION;

            final Set<EntityTypeInvocationHandler> toBeLinked = new HashSet<EntityTypeInvocationHandler>();
            final String serviceRoot = factory.getServiceRoot();

            for (Object proxy : type == ODataLinkType.ENTITY_SET_NAVIGATION
                    ? (Collection) property.getValue() : Collections.singleton(property.getValue())) {

                final EntityTypeInvocationHandler target =
                        (EntityTypeInvocationHandler) Proxy.getInvocationHandler(proxy);

                final AttachedEntityStatus status;

                try {
                    status = EntityContainerFactory.getContext().entityContext().getStatus(target);
                } catch (IllegalStateException e) {
                    // this case takes place if we iterate through collection and current item does not have any changes
                    // TODO find another way to look for changes in collection
                    continue;
                }

                final URI editLink = target.getEntity().getEditLink();

                if ((status == AttachedEntityStatus.ATTACHED || status == AttachedEntityStatus.LINKED)
                        && !target.isChanged()) {
                    entity.addLink(buildNavigationLink(
                            property.getKey().name(),
                            URIUtils.getURI(serviceRoot, editLink.toASCIIString()), type));
                } else {
                    if (!items.contains(target)) {
                        pos = processEntityContext(target, pos, items, delayedUpdates, changeset);
                        pos++;
                    }

                    final Integer targetPos = items.get(target);
                    if (targetPos == null) {
                        // schedule update for the current object
                        LOG.debug("Schedule '{}' from '{}' to '{}'", type.name(), handler, target);
                        toBeLinked.add(target);
                    } else if (status == AttachedEntityStatus.CHANGED) {
                        entity.addLink(buildNavigationLink(
                                property.getKey().name(),
                                URIUtils.getURI(serviceRoot, editLink.toASCIIString()), type));
                    } else {
                        // create the link for the current object
                        LOG.debug("'{}' from '{}' to (${}) '{}'", type.name(), handler, targetPos, target);

                        entity.addLink(
                                buildNavigationLink(property.getKey().name(), URI.create("$" + targetPos), type));
                    }
                }
            }

            if (!toBeLinked.isEmpty()) {
                delayedUpdates.add(new EntityLinkDesc(property.getKey().name(), handler, toBeLinked, type));
            }
        }

        // insert into the batch
        LOG.debug("{}: Insert '{}' into the batch", pos, handler);
        batch(handler, entity, changeset);

        items.put(handler, pos);

        int startingPos = pos;

        if (handler.getEntity().isMediaEntity()) {

            // update media properties
            if (!handler.getPropertyChanges().isEmpty()) {
                final URI targetURI = currentStatus == AttachedEntityStatus.NEW
                        ? URI.create("$" + startingPos)
                        : URIUtils.getURI(factory.getServiceRoot(), handler.getEntity().getEditLink().toASCIIString());
                batchUpdate(handler, targetURI, entity, changeset);
                pos++;
                items.put(handler, pos);
            }

            // update media content
            if (handler.getStreamChanges() != null) {
                final URI targetURI = currentStatus == AttachedEntityStatus.NEW
                        ? URI.create("$" + startingPos + "/$value")
                        : URIUtils.getURI(
                        factory.getServiceRoot(), handler.getEntity().getEditLink().toASCIIString() + "/$value");

                batchUpdateMediaEntity(handler, targetURI, handler.getStreamChanges(), changeset);

                // update media info (use null key)
                pos++;
                items.put(null, pos);
            }
        }

        for (Map.Entry<String, InputStream> streamedChanges : handler.getStreamedPropertyChanges().entrySet()) {
            final URI targetURI = currentStatus == AttachedEntityStatus.NEW
                    ? URI.create("$" + startingPos) : URIUtils.getURI(
                    factory.getServiceRoot(),
                    EngineUtils.getEditMediaLink(streamedChanges.getKey(), entity).toASCIIString());

            batchUpdateMediaResource(handler, targetURI, streamedChanges.getValue(), changeset);

            // update media info (use null key)
            pos++;
            items.put(handler, pos);
        }

        return pos;
    }

    private void processDelayedUpdates(
            final List<EntityLinkDesc> delayedUpdates,
            int pos,
            final TransactionItems items,
            final ODataChangeset changeset) {

        for (EntityLinkDesc delayedUpdate : delayedUpdates) {
            final ODataEntity changes = ODataObjectFactory.newEntity(delayedUpdate.getSource().getEntity().getName());
            final URI sourceURI = prepareDelayedUpdate(items, pos, delayedUpdate, changes);
            batchUpdate(delayedUpdate.getSource(), sourceURI, changes, changeset);
        }
    }
}
