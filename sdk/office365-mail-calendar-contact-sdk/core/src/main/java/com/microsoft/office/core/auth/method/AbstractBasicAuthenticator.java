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
package com.microsoft.office.core.auth.method;

import com.msopentech.org.apache.http.client.HttpClient;
import com.msopentech.org.apache.http.client.methods.HttpUriRequest;

import com.msopentech.org.apache.commons.codec.binary.Base64;

import com.microsoft.office.core.net.NetworkException;

/**
 * Abstract implementation for credentials required to authorize using Basic authentication method.
 */
public abstract class AbstractBasicAuthenticator implements IAuthenticator {

    /**
     * Gets username used for authentication.
     *
     * @return Username.
     */
    protected abstract String getUsername();

    /**
     * Gets password used for authentication.
     *
     * @return Password.
     */
    protected abstract String getPassword();

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareClient(HttpClient client) throws NetworkException {
        // TODO: verify
        //            CredentialsProvider provider = ((AbstractHttpClient) client).getCredentialsProvider();
        //            provider.setCredentials(
        //                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
        //                    new UsernamePasswordCredentials(getUsername(), getPassword()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareRequest(HttpUriRequest request) {
        String encodedValue = Base64.encodeBase64String((getUsername() + ":" + getPassword()).getBytes()).trim();
        request.setHeader("Authorization", "Basic " + encodedValue);
    }

}
