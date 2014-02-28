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

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import com.microsoft.adal.AuthenticationCallback;
import com.microsoft.adal.AuthenticationContext;
import com.microsoft.adal.AuthenticationResult;
import com.microsoft.office.core.auth.IOfficeCredentials;
import com.microsoft.office.core.auth.method.IAuthenticator;
import com.microsoft.office.core.net.NetworkException;
import com.msopentech.org.apache.http.client.HttpClient;
import com.msopentech.org.apache.http.client.methods.HttpUriRequest;

/**
 * Abstract implementation for credentials required to authorize to Office 365 online.
 */
public abstract class AbstractOfficeAuthenticator implements IAuthenticator {

    /**
     * Gets credentials used in this authenticator.
     * 
     * @return credentials in use.
     */
    protected abstract IOfficeCredentials getCredentials();

    /**
     * Returns an activity passed to ADAL as a context.
     * 
     * @return activity.
     */
    protected abstract Activity getActivity();

    public AbstractOfficeAuthenticator() {
        mCredentials = getCredentials();
    }

    IOfficeCredentials mCredentials;

    Runnable mUiRunnable;
    private AuthenticationContext mAuthContext = null;

    /**
     * Invoked when ADAL activity reports about finish.
     * 
     * @param requestCode Request code.
     * @param resultCode Result code.
     * @param data Data.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mAuthContext != null) {
            mAuthContext.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void prepareRequest(HttpUriRequest request) {
        // Will be called after prepareClient that will retrieve token if non available.
        request.addHeader("Authorization", "Bearer " + mCredentials.getToken());
    }

    @Override
    public void prepareClient(final HttpClient client) throws NetworkException {
        // We do have credentials, simply pass this step. Token will be set later in prepareRequest().
        if (mCredentials != null && !StringUtils.isEmpty(mCredentials.getAuthorityUrl()) && !StringUtils.isEmpty(mCredentials.getResourceId()) && 
                !StringUtils.isEmpty(mCredentials.getClientId()) && !StringUtils.isEmpty(mCredentials.getToken())) {
            return;
        }

        final Activity activity = getActivity();

        mCredentials = getCredentials();

        // Should call this on UI thread b/c WebVew must be instantiated and run on UI thread only.
        try {
            mUiRunnable = new Runnable() {
                public void run() {
                    try {
                        mAuthContext = new AuthenticationContext(activity, mCredentials.getAuthorityUrl(), false);
                        mAuthContext.acquireToken(activity, mCredentials.getResourceId(), mCredentials.getClientId(), mCredentials.getRedirectUrl(), mCredentials.getUserHint(),
                                new AuthenticationCallback<AuthenticationResult>() {
                                    @Override
                                    public void onSuccess(AuthenticationResult result) {
                                        if (result != null && !TextUtils.isEmpty(result.getAccessToken())) {
                                            AbstractOfficeAuthenticator.this.onDone(result.getAccessToken());
                                        }
                                    }

                                    @Override
                                    public void onError(Exception exc) {
                                        AbstractOfficeAuthenticator.this.onError(exc);
                                    }
                                });
                    } catch (Exception exc) {
                        AbstractOfficeAuthenticator.this.onError(exc);
                    }
                }
            };
            // As WebView is running on it's own thread we should block an wait until it's finished.
            synchronized (mUiRunnable) {
                getActivity().runOnUiThread(mUiRunnable);
                mUiRunnable.wait();
            }
        } catch (Exception e) {
            // TODO: Log it
            throw new NetworkException("Authentication: Error executing access code retrieval." + e.getMessage(), e);
        }
    }

    public void onDone(String result) {
        try {
            mCredentials.setToken(result);
            releaseUiThread();
        } catch (Exception e) {
            // TODO: log it.
        }
    }

    public void onError(Throwable error) {
        releaseUiThread();
    }

    private void releaseUiThread() {
        try {
            synchronized (mUiRunnable) {
                mUiRunnable.notify();
            }
        } catch (Exception e) {
            // Ignore.
        }
    }
}
