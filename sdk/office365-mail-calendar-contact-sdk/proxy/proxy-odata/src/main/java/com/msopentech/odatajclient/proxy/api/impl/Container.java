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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.msopentech.odatajclient.engine.client.ODataClient;
import com.msopentech.odatajclient.engine.data.ODataEntity;
import com.msopentech.odatajclient.engine.data.ODataLink;
import com.msopentech.odatajclient.engine.data.ODataLinkType;
import com.msopentech.odatajclient.engine.data.ODataObjectFactory;
import com.msopentech.odatajclient.engine.utils.URIUtils;
import com.msopentech.odatajclient.proxy.api.AbstractContainer;
import com.msopentech.odatajclient.proxy.api.EntityContainerFactory;
import com.msopentech.odatajclient.proxy.api.context.AttachedEntityStatus;
import com.msopentech.odatajclient.proxy.api.context.EntityLinkDesc;

// TODO: This has been a dependency for Batch request.
// Batch request hasn't been tested as Exchange server doesn't support Batch requests.
//import com.thoughtworks.xstream.XStream;
//import com.thoughtworks.xstream.io.xml.Xpp3Driver;

/**
 * Provides common operations for all containers.
 */
public abstract class Container implements AbstractContainer {

    private static final long serialVersionUID = 3996249611681487281L;

    /**
     * Logger.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(Container.class);

    // TODO: This has been a dependency for Batch request.
    // Batch request hasn't been tested as Exchange server doesn't support Batch requests.
    //protected final XStream xstream = new XStream(new Xpp3Driver());

    protected final ODataClient client;

    protected final EntityContainerFactory factory;

    protected Container(final ODataClient client, final EntityContainerFactory factory) {
        this.client = client;
        this.factory = factory;
    }

    protected ODataLink buildNavigationLink(final String name, final URI uri, final ODataLinkType type) {
        switch (type) {
            case ENTITY_NAVIGATION:
                return ODataObjectFactory.newEntityNavigationLink(name, uri);

            case ENTITY_SET_NAVIGATION:
                return ODataObjectFactory.newFeedNavigationLink(name, uri);

            default:
                throw new IllegalArgumentException("Invalid link type " + type.name());
        }
    }

    protected URI prepareDelayedUpdate(TransactionItems items, int pos, EntityLinkDesc delayedUpdate, final ODataEntity changes) {
        ++pos;
        items.put(delayedUpdate.getSource(), pos);
        AttachedEntityStatus status =
                EntityContainerFactory.getContext().entityContext().getStatus(delayedUpdate.getSource());

        final URI sourceURI;
        if (status == AttachedEntityStatus.CHANGED) {
            sourceURI = URIUtils.getURI(
                    factory.getServiceRoot(),
                    delayedUpdate.getSource().getEntity().getEditLink().toASCIIString());
        } else {
            int sourcePos = items.get(delayedUpdate.getSource());
            sourceURI = URI.create("$" + sourcePos);
        }

        for (EntityTypeInvocationHandler target : delayedUpdate.getTargets()) {
            status = EntityContainerFactory.getContext().entityContext().getStatus(target);

            final URI targetURI;
            if (status == AttachedEntityStatus.CHANGED) {
                targetURI = URIUtils.getURI(
                        factory.getServiceRoot(), target.getEntity().getEditLink().toASCIIString());
            } else {
                int targetPos = items.get(target);
                targetURI = URI.create("$" + targetPos);
            }

            changes.addLink(delayedUpdate.getType() == ODataLinkType.ENTITY_NAVIGATION
                    ? ODataObjectFactory.newEntityNavigationLink(delayedUpdate.getSourceName(), targetURI)
                    : ODataObjectFactory.newFeedNavigationLink(delayedUpdate.getSourceName(), targetURI));

            LOG.debug("'{}' from {} to {}", new Object[] {
                delayedUpdate.getType().name(), sourceURI, targetURI});
        }
        return sourceURI;
    }

    protected class TransactionItems {

        private final List<EntityTypeInvocationHandler> keys = new ArrayList<EntityTypeInvocationHandler>();

        private final List<Integer> values = new ArrayList<Integer>();

        public EntityTypeInvocationHandler get(final Integer value) {
            if (value != null && values.contains(value)) {
                return keys.get(values.indexOf(value));
            } else {
                return null;
            }
        }

        public Integer get(final EntityTypeInvocationHandler key) {
            if (key != null && keys.contains(key)) {
                return values.get(keys.indexOf(key));
            } else {
                return null;
            }
        }

        public void remove(final EntityTypeInvocationHandler key) {
            if (keys.contains(key)) {
                values.remove(keys.indexOf(key));
                keys.remove(key);
            }
        }

        public void put(final EntityTypeInvocationHandler key, final Integer value) {
            // replace just in case of null current value; otherwise add the new entry
            if (key != null && keys.contains(key) && values.get(keys.indexOf(key)) == null) {
                remove(key);
            }
            keys.add(key);
            values.add(value);
        }

        public List<Integer> sortedValues() {
            final List<Integer> sortedValues = new ArrayList<Integer>(values);
            Collections.<Integer>sort(sortedValues);
            return sortedValues;
        }

        public boolean contains(final EntityTypeInvocationHandler key) {
            return keys.contains(key);
        }

        public int size() {
            return keys.size();
        }

        public boolean isEmpty() {
            return keys.isEmpty();
        }
    }
}
