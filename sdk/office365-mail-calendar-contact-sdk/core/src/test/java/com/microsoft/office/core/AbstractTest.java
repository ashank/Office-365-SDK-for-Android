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

import java.io.InputStream;
import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.microsoft.exchange.services.odata.model.Me;
import com.microsoft.office.core.Configuration;
import com.microsoft.office.core.auth.method.AbstractBasicAuthenticator;
import com.msopentech.odatajclient.engine.client.ODataClient;
import com.msopentech.odatajclient.engine.client.ODataClientFactory;
import com.msopentech.odatajclient.engine.client.ODataV3Client;
import com.msopentech.odatajclient.engine.client.ODataV4Client;
import com.msopentech.odatajclient.engine.data.ODataEntity;
import com.msopentech.odatajclient.engine.format.ODataFormat;
import com.msopentech.odatajclient.engine.format.ODataPubFormat;
import com.msopentech.odatajclient.proxy.api.EntityContainerFactory;

public abstract class AbstractTest extends Assert {

    protected static ODataV3Client v3Client;

    protected static ODataV4Client v4Client;

    protected static final String endpoint = "https://outlook.office365.com/ews/odata";

    protected static final String username = "Enter your username here";

    protected static final String password = "Enter your password here";

    /**
     * This is needed for correct number handling (Double, for example).
     */
    @BeforeClass
    public static void setEnglishLocale() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @BeforeClass
    public static void setClientInstances() {
        v3Client = ODataClientFactory.getV3();
        v4Client = ODataClientFactory.getV4();
    }

    @BeforeClass
    public static void setConfiguration() {
        Configuration.setServerBaseUrl(endpoint);
        Configuration.setAuthenticator(new AbstractBasicAuthenticator() {
            protected String getUsername() {
                return username;
            }

            protected String getPassword() {
                return password;
            }
        });
    }
    
    @After
    public void clearContext() {
        // clear entity context after each test to avoid side effects
        EntityContainerFactory.getContext().detachAll();
    }

    protected String getSuffix(final ODataPubFormat format) {
        return format == ODataPubFormat.ATOM ? "xml" : "json";
    }

    protected String getSuffix(final ODataFormat format) {
        return format == ODataFormat.XML ? "xml" : "json";
    }
    
    protected ODataClient getClient() {
        return v4Client;
    }

    protected ODataEntity getEntityFromResource(String resourceFileName) {
        final InputStream input = getClass().getResourceAsStream(resourceFileName);
        return getClient().getBinder().getODataEntity(
                getClient().getDeserializer().toEntry(input, getClient().getResourceFactory().entryClassForFormat(ODataPubFormat.JSON)));
    }
}
