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

/**
 * Interface for a class providing a set of resources required to access SharePoint.
 */
public interface IOfficeCredentials {

    /**
     * Gets authority url.
     *
     * @return Authority url.
     */
    abstract String getAuthorityUrl();

    /**
     * Gets client id.
     *
     * @return Client id.
     */
    abstract String getClientId();

    /**
     * Gets resource id.
     *
     * @return Resource id.
     */
    abstract String getResourceId();

    /**
     * Gets redirect url.
     *
     * @return Redirect url.
     */
    public String getRedirectUrl();

    /**
     * Gets user hint.
     *
     * @return User hint.
     */
    public String getUserHint();

    /**
     * Sets user hint.
     *
     * @param userHint New user hint.
     * @return Current {@link IOfficeCredentials} instance.
     */
    public IOfficeCredentials setUserHint(String userHint);

    /**
     * Gets refresh token.
     *
     * @return Refresh token.
     */
    public String getRefreshToken();

    /**
     * Sets refresh token.
     *
     * @param token New refresh token.
     * @return Current {@link IOfficeCredentials} instance.
     */
    public IOfficeCredentials setRefreshToken(String token);

    /**
     * Gets authorization token.
     *
     * @return Authorization token.
     */
    abstract String getToken();

    /**
     * Sets new authorization token.
     *
     * @param token New authorization token.
     * @return Current {@link IOfficeCredentials} instance.
     */
    abstract IOfficeCredentials setToken(String token);
}
