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
package com.example.office.auth;

import com.microsoft.office.core.auth.OfficeCredentialsImpl;

/**
 * Stores credentials required to authenticate to Office 365 Online.
 */
public class OfficeCredentials extends OfficeCredentialsImpl {

    /**
     * Unique storage UUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Authentication type from {@link AuthType}.
     */
    private AuthType mAuthType = AuthType.UNDEFINED;

    /**
     * NTLM login.
     */
    private String mLogin;

    /**
     * NTLM login.
     */
    private String mPassword;

    /**
     * Basic constructor.
     */
    public OfficeCredentials() {}

    /**
     * {@inheritDoc}
     */
    public OfficeCredentials(String authorityUrl, String clientId, String resourceId, String redirectUrl) throws IllegalArgumentException {
        super(authorityUrl, clientId, resourceId, redirectUrl);
    }

    /**
     * Resets current authentication state i.e. clears out access code and token.
     * 
     * @return Updated credentials.
     */
    public OfficeCredentials reset() {
        setToken(null);
        setRefreshToken(null);
        return this;
    }

    /**
     * @return Authentication type.
     */
    public AuthType getAuthType() {
        return mAuthType;
    }

    /**
     * Sets authentication type.
     * 
     * @param type Authentication type. See {@link AuthType}.
     * 
     * @return Updated credentials.
     */
    public OfficeCredentials setAuthType(AuthType type) {
        mAuthType = type;
        return this;
    }

    /**
     * @return the Login.
     */
    public String getLogin() {
        return mLogin;
    }

    /**
     * @param login The login to set.
     * 
     * @return Updated credentials.
     */
    public OfficeCredentials setLogin(String login) {
        this.mLogin = login;
        return this;
    }

    /**
     * @return The password.
     */
    public String getPassword() {
        return mPassword;
    }

    /**
     * @param pass The pass to set.
     * 
     * @return Updated credentials.
     */
    public OfficeCredentials setPassword(String pass) {
        this.mPassword = pass;
        return this;
    }
}
