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
package com.microsoft.office.integration;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Semaphore;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.microsoft.exchange.services.odata.model.Me;
import com.microsoft.office.core.auth.OfficeCredentialsImpl;
import com.microsoft.office.core.auth.IOfficeCredentials;
import com.microsoft.office.integration.R;
import com.msopentech.odatajclient.engine.client.ODataClientFactory;
import com.msopentech.odatajclient.proxy.api.AsyncCall;

public class TestActivity extends Activity {

    public static final String AUTHORITY_URL = "https://login.windows-ppe.net/p365ppetap04.ccsctp.net";
    public static final String CLIENT_ID = "a7558c9a-c964-4fbf-be19-2f277f78a586";
    public static final String RESOURCE_ID = "https://outlook.office365.com/";
    public static final String REDIRECT_URL = "http://msopentech.com";

    /**
     * Oauth2 office authenticator.
     */
    AbstractOfficeAuthenticator mOfficeAuthenticator = null;
	public static Semaphore available = new Semaphore(0);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        com.microsoft.office.core.Configuration.setServerBaseUrl("https://outlook.office365.com/ews/OData");

        mOfficeAuthenticator = new AbstractOfficeAuthenticator() {
            @Override
             protected IOfficeCredentials getCredentials() {
                OfficeCredentialsImpl creds = new OfficeCredentialsImpl(AUTHORITY_URL, CLIENT_ID, RESOURCE_ID, REDIRECT_URL);
                creds.setUserHint("Enter your username here");
                return creds;
            }
            @Override
            protected Activity getActivity() {
                return TestActivity.this;
            }
			@Override
			public void onDone(String result) {
			    super.onDone(result);
				available.release();
			}
			@Override
			public void onError(Throwable error) {
				super.onError(error);
				//FIXME assert fail  on it.
			}
        };
        com.microsoft.office.core.Configuration.setAuthenticator(mOfficeAuthenticator);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mOfficeAuthenticator != null) {
            mOfficeAuthenticator.onActivityResult(requestCode, resultCode, data);
        }
    }
}

