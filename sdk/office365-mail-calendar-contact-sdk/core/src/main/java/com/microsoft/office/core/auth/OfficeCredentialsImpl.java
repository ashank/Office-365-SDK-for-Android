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

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

/**
 * Stores credentials required to authenticate to Office 365 Online.
 */
public class OfficeCredentialsImpl implements IOfficeCredentials, Serializable {

    /**
     * Unique storage UUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * SharePoint authority url. E.g. "https://login.windows.net/common/oauth2/token".
     */
    private String mAuthorityUrl = "";

    /**
     * SharePoint application unique ID. E.g. "60188dfc-3250-44c5-8434-8f106c9e529e".
     */
    private String mClientId = "";

    /**
     * SharePoint resource unique ID. E.g. "https://outlook.office365.com/".
     */
    private String mResourceId = "";

    /**
     * Url application will be redirected after authentication. Should be SSL secure. "https://www.domain.com/redirect".
     */
    private String mRedirectUrl;

    /**
     * Refresh token returned by the SharePoint site after 2-st (final) step of authentication.
     */
    private String mRefreshToken;

    /**
     * Access token returned by the SharePoint site after 2-st (final) step of authentication.
     */
    private String mToken;

    /**
     * User hint for login on authorization page.
     */
    private String mUserHint;

    public OfficeCredentialsImpl() {}

    /**
     * Instantiates and validates credentials.
     *
     * @param authorityUrl SharePoint authority url. E.g. "https://login.windows.net/common/oauth2/token".
     * @param clientId SharePoint application unique ID. E.g. "60188dfc-3250-44c5-8434-8f106c9e529e".
     * @param resourceId SharePoint resource unique ID. E.g. "https://outlook.office365.com/".
     * @param redirectUrl Url application will be redirected after authentication. Should be SSL secure. "https://www.domain.com/redirect".
     * @throws IllegalArgumentException Thrown if arguments validation fails.
     */
    public OfficeCredentialsImpl(String authorityUrl, String clientId, String resourceId, String redirectUrl) throws IllegalArgumentException {
        mAuthorityUrl = authorityUrl;
        mClientId = clientId;
        mResourceId = resourceId;
        mRedirectUrl = redirectUrl;

        if (StringUtils.isEmpty(mAuthorityUrl)) {
            throw new IllegalArgumentException("Authority url can not be null or empty");
        }

        if (StringUtils.isEmpty(mClientId)) {
            throw new IllegalArgumentException("Client id can not be null or empty");
        }

        if (StringUtils.isEmpty(mResourceId)) {
            throw new IllegalArgumentException("Resource id can not be null or empty");
        }

        if (StringUtils.isEmpty(mRedirectUrl)) {
            throw new IllegalArgumentException("Redirect url can not be null or empty");
        }
    }

    @Override
    public String getAuthorityUrl() {
        return mAuthorityUrl;
    }

    @Override
    public String getClientId() {
        return mClientId;
    }

    @Override
    public String getResourceId() {
        return mResourceId;
    }

    @Override
    public String getRedirectUrl() {
        return mRedirectUrl;
    }

    @Override
    public String getRefreshToken() {
        return mRefreshToken;
    }

    @Override
    public IOfficeCredentials setRefreshToken(String token) {
        mRefreshToken = token;
        return this;
    }

    @Override
    public String getToken() {
        return mToken;
    }

    @Override
    public IOfficeCredentials setToken(String token) {
        mToken = token;
        return this;
    }

    @Override
    public String getUserHint() {
        return mUserHint;
    }

    @Override
    public IOfficeCredentials setUserHint(String userHint) {
        mUserHint = userHint;
        return this;
    }
}
