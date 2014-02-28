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
package com.microsoft.office.core;

import java.net.URI;

import com.microsoft.office.core.auth.DefaultAuthenticationFactory;
import com.microsoft.office.core.auth.DefaultRequestAuthenticationFactory;
import com.microsoft.office.core.auth.method.IAuthenticator;
import com.microsoft.office.proxy.ContainerType;
import com.microsoft.office.proxy.OfficeEntityContainerFactory;
import com.microsoft.office.proxy.OfficeEntityContainerInvocationHandler;
import com.msopentech.odatajclient.proxy.api.EntityContainerFactory;

/**
 * Provides access to configuring SDK main parameters like service URL and authentication method.
 */
public final class Configuration {

    /**
     * Base URL to access SharePoint and related services.
     */
    private static String sServerBaseUrl = null;

    /**
     * Currently used authentication strategy.
     */
    private static IAuthenticator sAuthenticator = null;

    /**
     * {@link EntityContainerFactory} instance.
     */
    private static EntityContainerFactory sFactory = null;

    /**
     * Indicates should we trust untrusted certificates.
     */
    private static boolean sTrustAll = false;

    /**
     * Sets base URL for application.
     *
     * @param serverBaseUrl New base URL value.
     * @throws IllegalArgumentException if given string is not a valid url.
     */
    public static void setServerBaseUrl(String serverBaseUrl) throws IllegalArgumentException {
        try {
            URI.create(serverBaseUrl).toURL(); // throws an exception if url is invalid
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        sServerBaseUrl = serverBaseUrl;
        if (!sServerBaseUrl.endsWith("/")) {
            sServerBaseUrl += "/";
        }

        sFactory = OfficeEntityContainerFactory.getV4Instance(URI.create(
                sServerBaseUrl.substring(0, sServerBaseUrl.length() - 1)).toASCIIString());
        sFactory.getConfiguration().setUseChuncked(false);
        if (sAuthenticator != null) {
            setAuthenticator(sAuthenticator); // set auth factory
        }
    }

    /**
     * Gets current value of base URL.
     *
     * @return Current base URL.
     */
    public static String getServerBaseUrl() {
        return sServerBaseUrl;
    }

    /**
     * Sets currently used authentication strategy.
     *
     * @param auth New authentication strategy.
     */
    public static void setAuthenticator(IAuthenticator auth) {
        if (sFactory != null) {
            sFactory.getConfiguration().setHttpClientFactory(new DefaultAuthenticationFactory());
            sFactory.getConfiguration().setHttpUriRequestFactory(new DefaultRequestAuthenticationFactory());
        }
        sAuthenticator = auth;
    }

    /**
     * Gets currently used authentication strategy.
     *
     * @return Current authentication strategy.
     */
    public static IAuthenticator getAuthenticator() {
        return sAuthenticator;
    }

    /**
     * Gets a value indicating should we trust untrusted certificates.
     *
     * @return True if we trust all certificates, false otherwise.
     */
    public static boolean isTrustAll() {
        return sTrustAll;
    }

    /**
     * Sets a value indicating should we trust untrusted certificates. Enable this option on your own risk.
     *
     * @param trustAll True if we should trust all certificates, false otherwise.
     */
    public static void setTrustAll(boolean trustAll) {
        Configuration.sTrustAll = trustAll;
    }

    /**
     * Gets currently used container type.
     *
     * @return Container type in use.
     */
    public static ContainerType getContainerType() {
        return OfficeEntityContainerInvocationHandler.getContainerType();
    }

    /**
     * Sets container type to be used.
     *
     * @param containerType container type to be used for service communication.
     */
    public static void setContainerType(ContainerType containerType) {
        OfficeEntityContainerInvocationHandler.setContainerType(containerType);
    }
}