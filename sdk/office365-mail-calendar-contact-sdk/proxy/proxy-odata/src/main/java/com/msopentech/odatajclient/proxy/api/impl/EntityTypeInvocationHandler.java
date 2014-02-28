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
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.msopentech.org.apache.commons.codec.binary.Base64;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.msopentech.odatajclient.engine.communication.request.invoke.AbstractOperation;
import com.msopentech.odatajclient.engine.communication.request.retrieve.ODataMediaRequest;
import com.msopentech.odatajclient.engine.communication.response.ODataRetrieveResponse;
import com.msopentech.odatajclient.engine.data.ODataEntity;
import com.msopentech.odatajclient.engine.data.ODataInlineEntity;
import com.msopentech.odatajclient.engine.data.ODataInlineEntitySet;
import com.msopentech.odatajclient.engine.data.ODataLink;
import com.msopentech.odatajclient.engine.data.ODataProperty;
import com.msopentech.odatajclient.engine.data.ODataTimestamp;
import com.msopentech.odatajclient.engine.data.metadata.AbstractEdmMetadata;
import com.msopentech.odatajclient.engine.data.metadata.EdmV4Metadata;
import com.msopentech.odatajclient.engine.data.metadata.edm.EdmSimpleType;
import com.msopentech.odatajclient.engine.format.ODataMediaFormat;
import com.msopentech.odatajclient.engine.utils.URIUtils;
import com.msopentech.odatajclient.proxy.api.AbstractEntityCollection;
import com.msopentech.odatajclient.proxy.api.AbstractOpenType;
import com.msopentech.odatajclient.proxy.api.EntityContainerFactory;
import com.msopentech.odatajclient.proxy.api.annotations.EntityType;
import com.msopentech.odatajclient.proxy.api.annotations.NavigationProperty;
import com.msopentech.odatajclient.proxy.api.annotations.Operation;
import com.msopentech.odatajclient.proxy.api.annotations.Property;
import com.msopentech.odatajclient.proxy.api.context.AttachedEntityStatus;
import com.msopentech.odatajclient.proxy.api.context.EntityContext;
import com.msopentech.odatajclient.proxy.api.context.EntityUUID;
import com.msopentech.odatajclient.proxy.utils.ClassUtils;
import com.msopentech.odatajclient.proxy.utils.EngineUtils;

public class EntityTypeInvocationHandler extends AbstractInvocationHandler implements AbstractOpenType {

    private static final long serialVersionUID = 2629912294765040037L;

    private final String entityContainerName;

    private final String entitySetName;

    private ODataEntity entity;

    private final Class<?> typeRef;

    private Map<String, Object> propertyChanges = new HashMap<String, Object>();

    private static final Logger LOG = LoggerFactory.getLogger(EntityTypeInvocationHandler.class);

    /**
     * Values that were read from entity.
     *
     * Originally all properties read from entity were added to {@link EntityTypeInvocationHandler#propertyChanges}.
     * Entity also got {@link AttachedEntityStatus#ATTACHED} status. Container discovered this status when flushing flushing and called {@link EntityTypeInvocationHandler#isChanged()}
     * method to check if entity has changed. This method checked if property {@link EntityTypeInvocationHandler#propertyChanges} is not empty.
     * So non-modified properties have been pushed to server again.
     *
     * This field and {@link EntityTypeInvocationHandler#linkCache} are added to avoid the kind of behavior described above.
     */
    private Map<String, Object> propertyCache = new HashMap<String, Object>();

    private Map<String, InputStream> streamedPropertyChanges = new HashMap<String, InputStream>(); // TODO cache too?

    private Map<NavigationProperty, Object> linkChanges = new HashMap<NavigationProperty, Object>();

    /**
     * Navigation properties that were read from entity.
     */
    private Map<NavigationProperty, Object> linkCache = new HashMap<NavigationProperty, Object>();

    private InputStream stream;

    private EntityUUID uuid;

    private final EntityContext entityContext = EntityContainerFactory.getContext().entityContext();

    private int propertiesTag;

    private int linksTag;

    static EntityTypeInvocationHandler getInstance(
            final ODataEntity entity,
            final EntitySetInvocationHandler<?, ?, ?> entitySet,
            final Class<?> typeRef) {

        return getInstance(
                entity,
                entitySet.containerHandler.getEntityContainerName(),
                entitySet.getEntitySetName(),
                typeRef,
                entitySet.containerHandler);
    }

    static EntityTypeInvocationHandler getInstance(
            final ODataEntity entity,
            final String entityContainerName,
            final String entitySetName,
            final Class<?> typeRef,
            final EntityContainerInvocationHandler containerHandler) {

        return new EntityTypeInvocationHandler(entity, entityContainerName, entitySetName, typeRef, containerHandler);
    }

    private EntityTypeInvocationHandler(
            final ODataEntity entity,
            final String entityContainerName,
            final String entitySetName,
            final Class<?> typeRef,
            final EntityContainerInvocationHandler containerHandler) {

        super(containerHandler.getClient(), containerHandler);
        this.entityContainerName = entityContainerName;
        this.typeRef = typeRef;

        this.entity = entity;
        this.entity.setMediaEntity(typeRef.getAnnotation(EntityType.class).hasStream());

        this.uuid = new EntityUUID(
                ClassUtils.getNamespace(typeRef),
                entityContainerName,
                entitySetName != null && entitySetName.contains("/") ?
                        entitySetName.substring(entitySetName.lastIndexOf('/') + 1) :
                        entitySetName, // important! Use name only
                entity.getName() != null ? // if server did not specify @odata.type we can set it explicitly
                        entity.getName() :
                        ClassUtils.getNamespace(typeRef) + "." + ClassUtils.getEntityTypeName(typeRef),
                EngineUtils.getKey(containerHandler.getFactory().getMetadata(), typeRef, entity));

        this.entitySetName = entitySetName;
        this.stream = null;
        this.propertiesTag = 0;
        this.linksTag = 0;
    }

    public void setEntity(final ODataEntity entity) {
        this.entity = entity;
        this.entity.setMediaEntity(typeRef.getAnnotation(EntityType.class).hasStream());

        this.uuid = new EntityUUID(
                getUUID().getSchemaName(),
                getUUID().getContainerName(),
                getUUID().getEntitySetName(),
                getUUID().getName(),
                EngineUtils.getKey(containerHandler.getFactory().getMetadata(), typeRef, entity));

        this.propertyChanges.clear();
        this.linkChanges.clear();
        this.streamedPropertyChanges.clear();
        this.propertyCache.clear();
        this.linkCache.clear();
        this.propertiesTag = 0;
        this.linksTag = 0;
        this.stream = null;
    }

    public EntityUUID getUUID() {
        return uuid;
    }

    public String getEntityContainerName() {
        return uuid.getContainerName();
    }

    public String getEntitySetName() {
        return entitySetName;
    }

    public Class<?> getTypeRef() {
        return typeRef;
    }

    public ODataEntity getEntity() {
        return entity;
    }

    public Map<String, Object> getPropertyChanges() {
        return propertyChanges;
    }

    public Map<NavigationProperty, Object> getLinkChanges() {
        return linkChanges;
    }

    public Map<String, Object> getPropertyCache() {
        return propertyCache;
    }

    public Map<NavigationProperty, Object> getLinkCache() {
        return linkCache;
    }

    /**
     * Gets the current ETag defined into the wrapped entity.
     *
     * @return
     */
    public String getETag() {
        return this.entity.getETag();
    }

    /**
     * Overrides ETag value defined into the wrapped entity.
     *
     * @param eTag ETag.
     */
    public void setETag(final String eTag) {
        this.entity.setETag(eTag);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Annotation[] methodAnnots = method.getAnnotations();

        if (isSelfMethod(method, args)) {
            return invokeSelfMethod(method, args);
        } else if (!ArrayUtils.isEmpty(methodAnnots) && methodAnnots[0] instanceof Operation) {
            // search for operation in metadata
            EdmV4Metadata meta = (EdmV4Metadata) containerHandler.getFactory().getMetadata();
            Operation annot = (Operation) methodAnnots[0];
            // if current entity is not created on server side yet, flush
            if (getEntity().getEditLink() == null) {
                getContainer().flush();
            }

            final AbstractOperation abstractOperation = getOperation(method, args, methodAnnots, meta, annot,
                    ClassUtils.getNamespace(typeRef) + "." + ClassUtils.getEntityTypeName(typeRef), getEntity().getEditLink().toASCIIString());

            return functionImport((Operation) methodAnnots[0], method, args, abstractOperation);
        } // Assumption: for each getter will always exist a setter and viceversa. THIS IS WRONG FOR EXCHANGE - SOME FIELDS ARE READ-ONLY!
        else if (method.getName().startsWith("get")) {
            // get method annotation and check if it exists as expected
            final Object res;

            final Method getter = typeRef.getMethod(method.getName());

            final Property property = ClassUtils.getAnnotation(Property.class, getter);
            if (property == null) {
                final NavigationProperty navProp = ClassUtils.getAnnotation(NavigationProperty.class, getter);
                if (navProp == null) {
                    throw new UnsupportedOperationException("Unsupported method " + method.getName());
                } else {
                    // if the getter refers to a navigation property ... navigate and follow link if necessary
                    res = getNavigationPropertyValue(navProp, getter, containerHandler.getFactory().getMetadata());
                }
            } else {
                // if the getter refers to a property .... get property from wrapped entity
                res = getPropertyValue(property, getter.getGenericReturnType());
            }

            // attach the current handler - FIXME is this mandatory?
            attach();

            return res;
        } else if (method.getName().startsWith("set")) {
            // get the corresponding getter method (see assumption above). Correct for Exchange, if we have setter, getter always exists.
            final String getterName = method.getName().replaceFirst("set", "get");
            final Method getter = typeRef.getMethod(getterName);

            final Property property = ClassUtils.getAnnotation(Property.class, getter);
            if (property == null) {
                final NavigationProperty navProp = ClassUtils.getAnnotation(NavigationProperty.class, getter);
                if (navProp == null) {
                    throw new UnsupportedOperationException("Unsupported method " + method.getName());
                } else {
                    // if the getter refers to a navigation property ...
                    if (ArrayUtils.isEmpty(args) || args.length != 1) {
                        throw new IllegalArgumentException("Invalid argument");
                    }

                    setNavigationPropertyValue(navProp, args[0]);
                }
            } else {
                setPropertyValue(property, args[0]);
            }

            return proxy;
        } else if (method.getName().equalsIgnoreCase(getEntitySetName())) {
            // returns top level entity set by its name in container
            Method getter = containerHandler.getInterfaceType()
                    .getDeclaredMethod("get" + StringUtils.capitalize(method.getName()), (Class<?>[]) null);
            return containerHandler.invoke(null, getter, null);
        } else {
            throw new UnsupportedOperationException("Method not found: " + method);
        }
    }

    private Object getNavigationPropertyValue(final NavigationProperty property, final Method getter, AbstractEdmMetadata metadata)
            throws UnsupportedEncodingException {
        final Class<?> type = getter.getReturnType();
        final Class<?> collItemType;
        if (AbstractEntityCollection.class.isAssignableFrom(type)) {
            final Type[] entityCollectionParams =
                    ((ParameterizedType) type.getGenericInterfaces()[0]).getActualTypeArguments();
            collItemType = (Class<?>) entityCollectionParams[0];
        } else {
            collItemType = type;
        }

        final Object navPropValue;

        if (linkChanges.containsKey(property)) {
            navPropValue = linkChanges.get(property);
        } else if (linkCache.containsKey(property)){
            navPropValue = linkCache.get(property);
        } else {
            final ODataLink link = EngineUtils.getNavigationLink(property.name(), entity);
            if (link == null) {
                throw new IllegalStateException("You must flush your changes before accessing navigation properties");
            }

            if (link instanceof ODataInlineEntity) {
                // return entity
                navPropValue = getEntityProxy(
                        ((ODataInlineEntity) link).getEntity(),
                        property.targetContainer(),
                        property.targetEntitySet(),
                        type,
                        false);
            } else if (link instanceof ODataInlineEntitySet) {
                // return entity set
                navPropValue = getEntityCollection(
                        collItemType,
                        type,
                        property.targetContainer(),
                        ((ODataInlineEntitySet) link).getEntitySet(),
                        link.getLink(),
                        false);
            } else {
                // navigate
                final URI uri = URIUtils.getURI(
                        containerHandler.getFactory().getServiceRoot(), link.getLink().toASCIIString());

                if (AbstractEntityCollection.class.isAssignableFrom(type)) {
                    // implicit entity set can be returned here
                    if (property.targetEntitySet() != "") {
                        navPropValue = getEntitySet(type, property.name());
                    } else {
                        navPropValue = getEntityCollection(
                                collItemType,
                                type,
                                property.targetContainer(),
                                client.getRetrieveRequestFactory().getEntitySetRequest(uri).execute().getBody(),
                                uri,
                                true);
                    }
                } else {
                    final ODataRetrieveResponse<ODataEntity> res =
                            client.getRetrieveRequestFactory().getEntityRequest(uri).execute();

                    navPropValue = getEntityProxy(
                            res.getBody(),
                            property.targetContainer(),
                            property.targetEntitySet(),
                            type,
                            res.getEtag(),
                            true);
                }
            }

            if (navPropValue != null) {
                linkCache.put(property, navPropValue);
            }
        }

        return navPropValue;
    }

    /**
     * Navigates to entity set with given type.
     *
     * @param type Entity set type.
     * @param propertyName Navigation property name.
     * @return Proxy holding entity set instance.
     */
    private Object getEntitySet(final Class<?> type, String propertyName) throws UnsupportedEncodingException {
        LOG.debug("Accessing entity set '{}' with type {}", propertyName, type.getName());
        String path = EngineUtils.getNavigationLink(propertyName, entity).getLink().
                toASCIIString().substring(containerHandler.getFactory().getServiceRoot().length());
        EntitySetInvocationHandler<?, ?, ?> handler =
                EntitySetInvocationHandler.getInstance(type, containerHandler, path);

        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{type},
                handler);
    }

    private Object getPropertyValue(final String name, final Type type) {
        LOG.debug("Accessing property '{}' with type {}", name, type.getClass().getName());
        try {
            final Object res;

            if (propertyChanges.containsKey(name)) {
                res = propertyChanges.get(name);
            } else if (propertyCache.containsKey(name)) {
                res = propertyCache.get(name);
            } else {
                res = type == null
                        ? EngineUtils.getValueFromProperty(
                                containerHandler.getFactory().getMetadata(), entity.getProperty(name))
                        : EngineUtils.getValueFromProperty(
                                containerHandler.getFactory().getMetadata(), entity.getProperty(name), type);

                if (res != null) {
                    propertyCache.put(name, res);
                }
            }

            return res;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error getting value for property '" + name + "'", e);
        }
    }

    private Object getPropertyValue(final Property property, final Type type) {
        if (!(type instanceof ParameterizedType) && (Class<?>) type == InputStream.class) {
            return getStreamedProperty(property);
        } else {
            return convertValue(property.type(), getPropertyValue(property.name(), type));
            
        }
    }
    
    /**
     * Performs additional manipulation on data before return them.
     * 
     * @param type Return type.
     * @param value Value to make operations.
     * @return Processed value.
     */
    private Object convertValue(String type, Object value) {
        try {
            EdmSimpleType simpleType = EdmSimpleType.fromValue(type);
            // If we need to return Edm.Binary but now have String, this string is base64 encoded.
            if (simpleType == EdmSimpleType.Binary && 
                    value instanceof String &&
                    Base64.isBase64((String) value)) {
                return Base64.decodeBase64((String) value);
            } // if we need to return ODataTimeStamp but have String, just parse it
            else if (simpleType.javaType().equals(ODataTimestamp.class) &&
                     value instanceof String) {
                return ODataTimestamp.parse(EdmSimpleType.fromValue(type), (String) value);
            }
        
        // handle other types here if needed
        
        } catch (Exception e) {/* this is not simple type */}
            
        return value;
    }

    public Object getAdditionalProperty(final String name) {
        return getPropertyValue(name, null);
    }

    public Collection<String> getAdditionalPropertyNames() {
        final Set<String> res = new HashSet<String>(propertyChanges.keySet());
        final Set<String> propertyNames = new HashSet<String>();
        for (Method method : typeRef.getMethods()) {
            final Annotation ann = method.getAnnotation(Property.class);
            if (ann != null) {
                final String property = ((Property) ann).name();
                propertyNames.add(property);

                // maybe someone could add a normal attribute to the additional set
                res.remove(property);
            }
        }

        for (ODataProperty property : entity.getProperties()) {
            if (!propertyNames.contains(property.getName())) {
                res.add(property.getName());
            }
        }

        return res;
    }

    private void setNavigationPropertyValue(final NavigationProperty property, final Object value) {
        LOG.debug("Setting navigation property '{}' to {}", property.name(), value.toString());
        // 1) attach source entity
        attach(AttachedEntityStatus.CHANGED, false);

        // 2) attach the target entity handlers
        for (Object link : AbstractEntityCollection.class.isAssignableFrom(value.getClass())
                ? (AbstractEntityCollection) value : Collections.singleton(value)) {

            final InvocationHandler etih = Proxy.getInvocationHandler(link);
            if (!(etih instanceof EntityTypeInvocationHandler)) {
                throw new IllegalArgumentException("Invalid argument type");
            }

            final EntityTypeInvocationHandler handler = (EntityTypeInvocationHandler) etih;
            if (!handler.getTypeRef().isAnnotationPresent(EntityType.class)) {
                throw new IllegalArgumentException(
                        "Invalid argument type " + handler.getTypeRef().getSimpleName());
            }

            if (!entityContext.isAttached(handler)) {
                entityContext.attach(handler, AttachedEntityStatus.LINKED);
            }
        }

        // 3) add links
        linkChanges.put(property, value);
    }

    private void setPropertyValue(final Property property, final Object value) {
        LOG.debug("Setting property '{}' to {}", property.name(), value.toString());
        if (property.type().equalsIgnoreCase("Edm.Stream")) {
            setStreamedProperty(property, (InputStream) value);
        } else {
            propertyChanges.put(property.name(), value);
        }

            attach(AttachedEntityStatus.CHANGED);
        }

    public void addAdditionalProperty(final String name, final Object value) {
        propertyChanges.put(name, value);
        attach(AttachedEntityStatus.CHANGED);
    }

    public boolean isChanged() {
        return !this.linkChanges.isEmpty()
                || !this.propertyChanges.isEmpty()
                || this.stream != null
                || !this.streamedPropertyChanges.isEmpty();
    }

    public void setStream(final InputStream stream) {
        if (typeRef.getAnnotation(EntityType.class).hasStream()) {
            IOUtils.closeQuietly(this.stream);
            this.stream = stream;
            attach(AttachedEntityStatus.CHANGED);
        }
    }

    public InputStream getStreamChanges() {
        return this.stream;
    }

    public Map<String, InputStream> getStreamedPropertyChanges() {
        return streamedPropertyChanges;
    }

    public InputStream getStream() {

        final String contentSource = entity.getMediaContentSource();

        if (this.stream == null
                && typeRef.getAnnotation(EntityType.class).hasStream()
                && StringUtils.isNotBlank(contentSource)) {

            final String comntentType =
                    StringUtils.isBlank(entity.getMediaContentType()) ? "*/*" : entity.getMediaContentType();

            final URI contentSourceURI = URIUtils.getURI(containerHandler.getFactory().getServiceRoot(), contentSource);

            final ODataMediaRequest retrieveReq = client.getRetrieveRequestFactory().getMediaRequest(contentSourceURI);
            retrieveReq.setFormat(ODataMediaFormat.fromFormat(comntentType));

            this.stream = retrieveReq.execute().getBody();
        }

        return this.stream;
    }

    public Object getStreamedProperty(final Property property) {
        LOG.debug("Getting streamed property '{}'", property.name());
        InputStream res = streamedPropertyChanges.get(property.name());

        try {
            if (res == null) {
                final URI link = URIUtils.getURI(
                        containerHandler.getFactory().getServiceRoot(),
                        EngineUtils.getEditMediaLink(property.name(), this.entity).toASCIIString());

                final ODataMediaRequest req = client.getRetrieveRequestFactory().getMediaRequest(link);
                res = req.execute().getBody();

            }
        } catch (Exception e) {
            res = null;
        }

        return res;

    }

    private void setStreamedProperty(final Property property, final InputStream input) {
        LOG.debug("Setting streamed property '{}'", property.name());
        final Object obj = propertyChanges.get(property.name());
        if (obj != null && obj instanceof InputStream) {
            IOUtils.closeQuietly((InputStream) obj);
        }

        streamedPropertyChanges.put(property.name(), input);
    }

    private void attach() {
        if (!entityContext.isAttached(this)) {
            entityContext.attach(this, AttachedEntityStatus.ATTACHED);
        }
    }

    private void attach(final AttachedEntityStatus status) {
        attach(status, true);
    }

    private void attach(final AttachedEntityStatus status, final boolean override) {
        if (entityContext.isAttached(this)) {
            if (override) {
                entityContext.setStatus(this, status);
            }
        } else {
            entityContext.attach(this, status);
        }
    }

    @Override
    public String toString() {
        return uuid.toString();
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof EntityTypeInvocationHandler) {
            return ((EntityTypeInvocationHandler) obj).getUUID().equals(uuid);
        }
        if (Proxy.getInvocationHandler(obj) != null &&
                    Proxy.getInvocationHandler(obj) instanceof EntityTypeInvocationHandler) {
            return ((EntityTypeInvocationHandler) Proxy.getInvocationHandler(obj)).getUUID().equals(uuid);
        }

        return false;
    }
}
