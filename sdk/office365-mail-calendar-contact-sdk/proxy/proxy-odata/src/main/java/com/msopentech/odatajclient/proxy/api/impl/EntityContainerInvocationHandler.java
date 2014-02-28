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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.msopentech.odatajclient.engine.client.ODataClient;
import com.msopentech.odatajclient.engine.data.ODataEntity;
import com.msopentech.odatajclient.engine.data.metadata.edm.AbstractEntityContainer;
import com.msopentech.odatajclient.engine.data.metadata.edm.AbstractFunctionImport;
import com.msopentech.odatajclient.engine.uri.URIBuilder;
import com.msopentech.odatajclient.engine.utils.URIUtils;
import com.msopentech.odatajclient.proxy.api.EntityContainerFactory;
import com.msopentech.odatajclient.proxy.api.annotations.EntityContainer;
import com.msopentech.odatajclient.proxy.api.annotations.Operation;
import com.msopentech.odatajclient.proxy.api.annotations.Singleton;
import com.msopentech.odatajclient.proxy.utils.ClassUtils;

public class EntityContainerInvocationHandler extends AbstractInvocationHandler {

    private static final long serialVersionUID = 7379006755693410764L;

    /**
     * Holds container handlers. It is correct since containers are singletons on server side.
     */
    protected static Map<Class<?>, EntityContainerInvocationHandler> sContainers = 
            new ConcurrentHashMap<Class<?>, EntityContainerInvocationHandler>();

    protected final EntityContainerFactory factory;

    protected final String schemaName;

    protected final String entityContainerName;

    protected final boolean defaultEC;
    
    private static final Logger LOG = LoggerFactory.getLogger(EntityContainerInvocationHandler.class);
    
    /**
     * Caches singleton instances.
     */
    private final Map<String, Object> singletonInstances = 
            new ConcurrentHashMap<String, Object>();

    /**
     * Actual EntityContainer interface type.
     */
    private final Class<?> typeRef;

    public static EntityContainerInvocationHandler getInstance(
            final ODataClient client, final Class<?> ref, final EntityContainerFactory factory) {

        if (sContainers.containsKey(ref)) {
            return sContainers.get(ref);
        }
        
        final EntityContainerInvocationHandler instance = new EntityContainerInvocationHandler(client, ref, factory);
        sContainers.put(ref, instance);
        instance.containerHandler = instance;
        return instance;
    }

    protected EntityContainerInvocationHandler(
            final ODataClient client, final Class<?> ref, final EntityContainerFactory factory) {

        super(client, null);

        final Annotation annotation = ref.getAnnotation(EntityContainer.class);
        if (!(annotation instanceof EntityContainer)) {
            throw new IllegalArgumentException(ref.getName()
                    + " is not annotated as @" + EntityContainer.class.getSimpleName());
        }

        this.factory = factory;
        this.typeRef = ref;
        this.entityContainerName = ((EntityContainer) annotation).name();
        this.defaultEC = ((EntityContainer) annotation).isDefaultEntityContainer();
        this.schemaName = ClassUtils.getNamespace(ref);
    }

    EntityContainerFactory getFactory() {
        return factory;
    }

    boolean isDefaultEntityContainer() {
        return defaultEC;
    }

    String getEntityContainerName() {
        return entityContainerName;
    }

    String getSchemaName() {
        return schemaName;
    }

    Class<?> getInterfaceType() {
        return typeRef;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (isSelfMethod(method, args)) {
            return invokeSelfMethod(method, args);
        } else if ("flush".equals(method.getName()) && ArrayUtils.isEmpty(args)) {
            LOG.debug("Flushing changes");
            getContainer().flush();
            return ClassUtils.returnVoid();
        } else {
            final Annotation[] methodAnnots = method.getAnnotations();
            // 1. access top-level entity sets
            if (methodAnnots.length == 0) {
                LOG.debug("Getting top-level {}", method.getReturnType().getSimpleName());
                final Class<?> returnType = method.getReturnType();

                return Proxy.newProxyInstance(
                        Thread.currentThread().getContextClassLoader(),
                        new Class<?>[] { returnType },
                        EntitySetInvocationHandler.getInstance(returnType, this));
            } // 2. invoke function imports
            else if (methodAnnots[0] instanceof Operation) {
                // TODO
                throw new UnsupportedOperationException("Action/Function imports are not implemented");
                
            } // 3. access singletons
            else if (methodAnnots[0] instanceof Singleton) {
                final String singletonName = ((Singleton)methodAnnots[0]).name();
                LOG.debug("Getting singleton {}", singletonName);
                if (!singletonInstances.containsKey(singletonName)) {
                    final Class<?> returnType = method.getReturnType();
    
                    final URIBuilder uri = client.getURIBuilder(getFactory().getServiceRoot()).appendEntitySetSegment(
                            StringUtils.capitalize(method.getName()));
    
                    final ODataEntity entity = client.getRetrieveRequestFactory().getEntityRequest(uri.build()).execute().getBody();
                    singletonInstances.put(singletonName, Proxy.newProxyInstance(
                            Thread.currentThread().getContextClassLoader(),
                            new Class<?>[] { returnType },
                            EntityTypeInvocationHandler.getInstance(entity, entityContainerName, null, returnType, this)));
                }

                return singletonInstances.get(singletonName);
            } else {
                throw new UnsupportedOperationException("Method not found: " + method);
            }
        }
    }
}
