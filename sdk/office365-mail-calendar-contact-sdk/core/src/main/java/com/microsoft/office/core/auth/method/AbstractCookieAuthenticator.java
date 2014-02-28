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

import java.util.List;

import com.msopentech.org.apache.http.client.CookieStore;
import com.msopentech.org.apache.http.client.HttpClient;
import com.msopentech.org.apache.http.client.methods.HttpUriRequest;
import com.msopentech.org.apache.http.impl.client.DefaultHttpClient;
import com.msopentech.org.apache.http.impl.cookie.BasicClientCookie;

import com.microsoft.office.core.net.NetworkException;

/**
 * Abstract implementation for credentials required to authorize using cookies.
 */
public abstract class AbstractCookieAuthenticator implements IAuthenticator {

    /**
     * Provides cookies to insert into request.
     *
     * @return cookies to insert into request.
     */
    protected abstract List<BasicClientCookie> getCookies();

    /**
     * Default constructor.
     */
    public AbstractCookieAuthenticator() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareClient(HttpClient client) {
        try {
            CookieStore cookieStore = ((DefaultHttpClient) client).getCookieStore();

            for (BasicClientCookie cookie : getCookies()) {
                cookieStore.addCookie(cookie);
            }

            ((DefaultHttpClient) client).setCookieStore(cookieStore);
        } catch (Exception e) {
            throw new NetworkException("Error while preparing and adding authentication cookies to a request.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareRequest(HttpUriRequest request) {
    }

}
