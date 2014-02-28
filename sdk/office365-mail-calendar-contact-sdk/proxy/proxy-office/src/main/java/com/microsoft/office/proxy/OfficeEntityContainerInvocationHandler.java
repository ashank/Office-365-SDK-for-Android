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

import com.msopentech.odatajclient.engine.client.ODataClient;
import com.msopentech.odatajclient.proxy.api.AbstractContainer;
import com.msopentech.odatajclient.proxy.api.EntityContainerFactory;
import com.msopentech.odatajclient.proxy.api.impl.BatchContainer;
import com.msopentech.odatajclient.proxy.api.impl.EntityContainerInvocationHandler;
import com.msopentech.odatajclient.proxy.api.impl.SequentialContainer;

/**
 * Extends {@link EntityContainerInvocationHandler} to implement server communication type switching.
 */
public class OfficeEntityContainerInvocationHandler extends EntityContainerInvocationHandler {

    /**
     * Currently used communication type.
     */
    protected static ContainerType sContainerType = ContainerType.SEQUENTIAL;

    /**
     * Creates a new instance of {@link OfficeEntityContainerInvocationHandler} class.
     *
     * @param client Client to be used.
     * @param ref Container class.
     * @param factory Container factory.
     */
    protected OfficeEntityContainerInvocationHandler(ODataClient client, Class<?> ref, EntityContainerFactory factory) {
        super(client, ref, factory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractContainer getContainer() {
        switch (getContainerType()) {
            case BATCH:
                return new BatchContainer(client, factory);

            case SEQUENTIAL:
                return new SequentialContainer(client, factory);
        }

        throw new IllegalStateException("Unknown container type is set");
    }

    /**
     * Gets currently used container type.
     *
     * @return Container type in use.
     */
    public static ContainerType getContainerType() {
        return sContainerType;
    }

    /**
     * Sets container type to be used.
     *
     * @param containerType container type to be used for service communication.
     */
    public static void setContainerType(ContainerType containerType) {
        sContainerType = containerType;
    }

    /**
     * Gets an instance of {@link OfficeEntityContainerInvocationHandler} class.
     *
     * @param client Client to be used.
     * @param ref Container class.
     * @param factory Container factory.
     * @return An instance of {@link OfficeEntityContainerInvocationHandler} class.
     */
    public static OfficeEntityContainerInvocationHandler getInstance(
            final ODataClient client, final Class<?> ref, final OfficeEntityContainerFactory factory) {

        if (sContainers.containsKey(ref)) {
            return (OfficeEntityContainerInvocationHandler) sContainers.get(ref);
        }

        final OfficeEntityContainerInvocationHandler instance = new OfficeEntityContainerInvocationHandler(client, ref, factory);
        sContainers.put(ref, instance);
        instance.containerHandler = instance;
        return instance;
    }
}
