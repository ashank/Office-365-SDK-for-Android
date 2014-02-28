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
package com.microsoft.office.proxy;

import java.lang.reflect.Proxy;

import com.msopentech.odatajclient.engine.client.ODataClient;
import com.msopentech.odatajclient.engine.client.ODataClientFactory;
import com.msopentech.odatajclient.proxy.api.EntityContainerFactory;

/**
 * Extends {@link EntityContainerFactory} to add methods allow to get instance of singleton container (as per OData 4 spec).
 */
public class OfficeEntityContainerFactory extends EntityContainerFactory {

    /**
     * Creates a new instance of {@link OfficeEntityContainerFactory} class.
     *
     * @param client Client to be used.
     * @param serviceRoot Service root url.
     */
    OfficeEntityContainerFactory(final ODataClient client, final String serviceRoot) {
        super(client, serviceRoot);
    }

    /**
     * Checks if {@link EntityContainerFactory} was already instantiated.
     * Assumption: user will work only with one service at once.
     *
     * @return {@link EntityContainerFactory} instance if any was already instantiated; <tt>null</tt> otherwise.
     */
    public static OfficeEntityContainerFactory getInstance() {
        if (FACTORY_PER_SERVICEROOT.isEmpty()) {
            return null;
        }

        return (OfficeEntityContainerFactory) FACTORY_PER_SERVICEROOT.values().iterator().next();
    }

    /**
     * Gets an instance of {@link OfficeEntityContainerFactory} class.
     *
     * @param client {@link ODataClient} instance to be used to communicate with service.
     * @param serviceRoot Service root url.
     * @return an instance of {@link OfficeEntityContainerFactory} class.
     */
    public static OfficeEntityContainerFactory getInstance(final ODataClient client, final String serviceRoot) {
        if (!FACTORY_PER_SERVICEROOT.containsKey(serviceRoot)) {
            final OfficeEntityContainerFactory instance = new OfficeEntityContainerFactory(client, serviceRoot);
            FACTORY_PER_SERVICEROOT.put(serviceRoot, instance);
        }
        return (OfficeEntityContainerFactory) FACTORY_PER_SERVICEROOT.get(serviceRoot);
    }

    /**
     * Gets an instance of {@link OfficeEntityContainerFactory} class.
     *
     * @param serviceRoot Service root url.
     * @return an instance of {@link OfficeEntityContainerFactory} class.
     */
    public static OfficeEntityContainerFactory getV4Instance(final String serviceRoot) {
        return (OfficeEntityContainerFactory) getInstance(ODataClientFactory.getV4(), serviceRoot);
    }

    @Override
    protected void createContainer(Class<?> reference) {
        final Object entityContainer = Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[] { reference },
                OfficeEntityContainerInvocationHandler.getInstance(client, reference, this));
        ENTITY_CONTAINERS.put(reference, entityContainer);
    }
}
