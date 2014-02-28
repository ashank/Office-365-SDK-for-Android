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

import com.microsoft.office.core.net.NetworkException;

/**
 * Interface for credentials to be sent in a request.
 */
public interface IAuthenticator {

	/**
	 * Adds the credentials to the client.
	 *
	 * @param client HTTP client to prepare.
	 *
	 * @throws NetworkException if any of authentication steps fail.
	 */
	public void prepareClient(final HttpClient client) throws NetworkException;

    /**
     * Adds the credentials to the request.
     *
     * @param request HTTP request to prepare.
     */
    public void prepareRequest(final HttpUriRequest request);
}
