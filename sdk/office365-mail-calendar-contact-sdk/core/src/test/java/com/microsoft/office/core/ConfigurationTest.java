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

import com.msopentech.org.apache.commons.codec.binary.Base64;
import com.msopentech.org.apache.http.client.methods.HttpGet;
import com.msopentech.org.apache.http.client.methods.HttpUriRequest;
import org.junit.Test;

import com.microsoft.exchange.services.odata.model.Me;
import com.microsoft.office.core.Configuration;

public class ConfigurationTest extends AbstractTest {

    @Test
    public void configurationTest() {
        assertEquals(Configuration.getServerBaseUrl(), endpoint);
        HttpUriRequest request = new HttpGet();
        Configuration.getAuthenticator().prepareRequest(request);
        String encoded = Base64.encodeBase64String((username + ":" + password).getBytes()).trim();
        assertEquals(request.getFirstHeader("Authorization").getValue(), "Basic " + encoded);
    }

    @Test(timeout = 60000)
    public void authorizationTest() {
        // try any request
        try {
            Me.getAlias();
        } catch (Exception e) {
            // request must succeed with these parameters
            fail(e.toString());
        }
    }

}
