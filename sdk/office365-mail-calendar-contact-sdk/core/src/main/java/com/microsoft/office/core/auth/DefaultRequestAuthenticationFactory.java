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
package com.microsoft.office.core.auth;

import java.net.URI;

import com.msopentech.org.apache.http.client.methods.HttpUriRequest;

import com.microsoft.office.core.Configuration;
import com.microsoft.office.core.auth.method.IAuthenticator;
import com.msopentech.odatajclient.engine.client.http.DefaultHttpUriRequestFactory;
import com.msopentech.odatajclient.engine.client.http.HttpMethod;

/**
 * Default implementation of request authentication factory.
 */
public class DefaultRequestAuthenticationFactory extends DefaultHttpUriRequestFactory {

    /**
     * Creates request for given uri with given method.
     *
     * @param method HTTP method.
     * @param uri Target URI.
     * @return {@link HttpUriRequest} instance prepared to make request.
     */
    @Override
    public HttpUriRequest createHttpUriRequest(final HttpMethod method, final URI uri) {
        final HttpUriRequest request = super.createHttpUriRequest(method, uri);
        try {
            final IAuthenticator creds = Configuration.getAuthenticator();
            if (creds != null) {
                creds.prepareRequest(request);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not sign request via OAuth", e);
        }
        return request;
    }
}