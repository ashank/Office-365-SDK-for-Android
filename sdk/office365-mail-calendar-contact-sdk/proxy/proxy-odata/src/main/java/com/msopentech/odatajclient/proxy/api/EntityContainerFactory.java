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
package com.msopentech.odatajclient.proxy.api;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.msopentech.odatajclient.engine.client.Configuration;
import com.msopentech.odatajclient.engine.client.ODataClient;
import com.msopentech.odatajclient.engine.client.ODataClientFactory;
import com.msopentech.odatajclient.engine.communication.request.retrieve.ODataMetadataRequest;
import com.msopentech.odatajclient.engine.communication.response.ODataRetrieveResponse;
import com.msopentech.odatajclient.engine.data.metadata.AbstractEdmMetadata;
import com.msopentech.odatajclient.engine.uri.filter.FilterFactory;
import com.msopentech.odatajclient.proxy.api.context.Context;
import com.msopentech.odatajclient.proxy.api.impl.EntityContainerInvocationHandler;

/**
 * Entry point for ODataJClient proxy mode, gives access to entity container instances.
 */
public class EntityContainerFactory {

    protected static final Object MONITOR = new Object();

    protected static Context context = null;

    /**
     * Factories singletons.
     */
    protected static final Map<String, EntityContainerFactory> FACTORY_PER_SERVICEROOT =
            new ConcurrentHashMap<String, EntityContainerFactory>();

    /**
     * Entity container singletons. Each entity container class can be instantiated once only.
     */
    protected static final Map<Class<?>, Object> ENTITY_CONTAINERS =
            new ConcurrentHashMap<Class<?>, Object>();

    protected final ODataClient client;

    protected final String serviceRoot;

    protected AbstractEdmMetadata metadata;

    public static Context getContext() {
        synchronized (MONITOR) {
            if (context == null) {
                context = new Context();
            }
        }

        return context;
    }

    public static EntityContainerFactory getInstance(final ODataClient client, final String serviceRoot) {
        if (!FACTORY_PER_SERVICEROOT.containsKey(serviceRoot)) {
            final EntityContainerFactory instance = new EntityContainerFactory(client, serviceRoot);
            FACTORY_PER_SERVICEROOT.put(serviceRoot, instance);
        }
        return FACTORY_PER_SERVICEROOT.get(serviceRoot);
    }

    public static EntityContainerFactory getV3Instance(final String serviceRoot) {
        return getInstance(ODataClientFactory.getV3(), serviceRoot);
    }

    public static EntityContainerFactory getV4Instance(final String serviceRoot) {
        return getInstance(ODataClientFactory.getV4(), serviceRoot);
    }

    protected EntityContainerFactory(final ODataClient client, final String serviceRoot) {
        this.client = client;
        this.serviceRoot = serviceRoot;
    }

    public Configuration getConfiguration() {
        return client.getConfiguration();
    }

    public FilterFactory getFilterFactory() {
        return client.getFilterFactory();
    }

    public String getServiceRoot() {
        return serviceRoot;
    }

    public AbstractEdmMetadata getMetadata() {
        synchronized (this) {
            if (metadata == null) {
                final ODataMetadataRequest req = client.getRetrieveRequestFactory().getMetadataRequest(serviceRoot);
                final ODataRetrieveResponse<AbstractEdmMetadata> res = req.execute();
                metadata = res.getBody();
                if (metadata == null) {
                    throw new IllegalStateException("No metadata found at URI '" + serviceRoot + "'");
                }
            }
        }

        return metadata;
    }

    /**
     * Return an initialized concrete implementation of the passed EntityContainer interface.
     *
     * @param <T> interface annotated as EntityContainer
     * @param reference class object of the EntityContainer annotated interface
     * @return an initialized concrete implementation of the passed reference
     * @throws IllegalStateException if <tt>serviceRoot</tt> was not set
     * @throws IllegalArgumentException if the passed reference is not an interface annotated as EntityContainer
     * @see com.msopentech.odatajclient.proxy.api.annotations.EntityContainer
     */
    @SuppressWarnings("unchecked")
    public <T> T getEntityContainer(final Class<T> reference) throws IllegalStateException, IllegalArgumentException {
        if (StringUtils.isBlank(serviceRoot)) {
            throw new IllegalStateException("serviceRoot was not set");
        }

        if (!ENTITY_CONTAINERS.containsKey(reference)) {
            createContainer(reference);
        }
        return (T) ENTITY_CONTAINERS.get(reference);
    }

    /**
     * Creates new instance of given container type and puts it to ENTITY_CONTAINERS.
     * 
     * @param reference Container type.
     */
    protected void createContainer(final Class<?> reference) {
        final Object entityContainer = Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { reference },
                EntityContainerInvocationHandler.getInstance(client, reference, this));
        ENTITY_CONTAINERS.put(reference, entityContainer);
    }
}
