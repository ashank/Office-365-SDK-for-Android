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
import java.security.KeyStore;

import com.msopentech.org.apache.http.client.HttpClient;
import com.msopentech.org.apache.http.conn.ClientConnectionManager;
import com.msopentech.org.apache.http.conn.scheme.PlainSocketFactory;
import com.msopentech.org.apache.http.conn.scheme.Scheme;
import com.msopentech.org.apache.http.conn.scheme.SchemeRegistry;
import com.msopentech.org.apache.http.conn.ssl.SSLSocketFactory;
import com.msopentech.org.apache.http.impl.client.DefaultHttpClient;
import com.msopentech.org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import com.msopentech.org.apache.http.params.CoreConnectionPNames;

import com.microsoft.office.core.Configuration;
import com.microsoft.office.core.auth.method.IAuthenticator;
import com.msopentech.odatajclient.engine.client.http.DefaultHttpClientFactory;
import com.msopentech.odatajclient.engine.client.http.HttpMethod;
import com.msopentech.odatajclient.engine.client.http.TrustAllSSLSocketFactory;

/**
 * Abstract implementation of authentication factory.
 */
abstract class AbstractAuthenticationFactory extends DefaultHttpClientFactory {

    private static final long serialVersionUID = -5832892947506196894L;

    /**
     * Creates a new instance of {@link DefaultAuthenticationFactory} class.
     */
    public AbstractAuthenticationFactory() {}

    /**
     * Creates HttpClient instance for given method and URI.
     *
     * @param method Http method.
     * @param uri Target URI.
     * @return HttpClient instance prepared to make request.
     */
    @SuppressWarnings("deprecation")
    public HttpClient createHttpClient(HttpMethod method, URI uri) {
        HttpClient httpclient = super.createHttpClient(method, uri);

        final IAuthenticator creds = Configuration.getAuthenticator();
        if (creds != null) {
            creds.prepareClient(httpclient);
        }

        httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, getConnectionTimeout());
        httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, getSocketTimeout());

        if (Configuration.isTrustAll()) {
            try {
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);

                SSLSocketFactory sf = new TrustAllSSLSocketFactory(trustStore);
                sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                SchemeRegistry registry = new SchemeRegistry();
                registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                registry.register(new Scheme("https", sf, 443));
                ClientConnectionManager ccm = new ThreadSafeClientConnManager(registry);
                httpclient = new DefaultHttpClient(ccm, httpclient.getParams());
            } catch (Exception e) {}
        }

        return httpclient;
    }

    /**
     * Gets connection timeout for current {@link AbstractAuthenticationFactory} instance.
     *
     * @return Connection timeout.
     */
    public abstract int getConnectionTimeout();

    /**
     * Gets socket timeout for current {@link AbstractAuthenticationFactory} instance.
     *
     * @return Socket timeout.
     */
    public abstract int getSocketTimeout();
}