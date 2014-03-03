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
package com.example.office365sample;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.microsoft.adal.AuthenticationCallback;
import com.microsoft.adal.AuthenticationContext;
import com.microsoft.adal.AuthenticationResult;
import com.microsoft.exchange.services.odata.model.IMessages;
import com.microsoft.exchange.services.odata.model.Me;
import com.microsoft.exchange.services.odata.model.types.IFolder;
import com.microsoft.exchange.services.odata.model.types.IMessage;
import com.microsoft.office.core.auth.method.IAuthenticator;
import com.microsoft.office.core.net.NetworkException;
import com.msopentech.org.apache.http.client.HttpClient;
import com.msopentech.org.apache.http.client.methods.HttpUriRequest;

public class MainActivity extends ListActivity {

    OfficeCredentials mCredentials;

    private AuthenticationContext mAuthContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.microsoft.office.core.Configuration.setServerBaseUrl(Constants.RESOURCE_ID + "ews/OData");
        
        getListView().setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[] {"Loading..."}));

        mCredentials = new OfficeCredentials(Constants.AUTHORITY_URL, Constants.CLIENT_ID, Constants.RESOURCE_ID, Constants.REDIRECT_URL);
        mCredentials.setUserHint(Constants.USER_HINT);

            try {
                mAuthContext = new AuthenticationContext(this, mCredentials.getAuthorityUrl(), false);
                mAuthContext.acquireToken(this, mCredentials.getResourceId(),
                        mCredentials.getClientId(), mCredentials.getRedirectUrl(),
                        mCredentials.getUserHint(),
                        new AuthenticationCallback<AuthenticationResult>() {
                            @Override
                            public void onSuccess(AuthenticationResult result) {
                                if (result != null && !TextUtils.isEmpty(result.getAccessToken())) {
                                    mCredentials.setToken(result.getAccessToken());
                                    com.microsoft.office.core.Configuration.setAuthenticator( new IAuthenticator() {
                                        @Override
                                        public void prepareRequest(HttpUriRequest request) {
                                            request.addHeader("Authorization", "Bearer " + mCredentials.getToken());
                                        }
                                        @Override
                                        public void prepareClient(HttpClient client) throws NetworkException {}
                                    });

                                    MainActivity.this.readMessages();
                                }
                            }

                            @Override
                            public void onError(Exception exc) {
                                Log.i("office365simpledemo", "error1");
                                getListView().setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1,
                                        new String[] {"Error during authentication:", exc.getMessage() == null ? "<null>" : exc.getMessage()}));
                            }
                        });
            } catch (Exception exc) {
                Log.i("office365simpledemo", "error2");
                getListView().setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1,
                        new String[] {"Error during authentication:", exc.getMessage()}));
            }
    }

    public void readMessages() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    IFolder drafts = Me.getDrafts();
                    IMessages c = drafts.getMessages();

                    List<MailItem> messages = new ArrayList<MailItem>();
                    
                    for (IMessage message : c) {
                        messages.add(new MailItem(message));
                    }
                    
                    updateList(messages);
                } catch (final Exception e) {
                    Log.d("office365simpledemo", "error3");
                    getListView().setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1,
                            new String[] {"Error during messages retrieving:", e.getMessage()}));
                }
                return null;
            }
        }.execute();
    }
    
    private void updateList(final List<MailItem> messages) {
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    ArrayAdapter<MailItem> adapter = new ArrayAdapter<MailItem>(MainActivity.this, android.R.layout.simple_list_item_2,
                            android.R.id.text1, messages) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                            TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                            String sender = "<unknown sender>";
                            text1.setText(sender);
                            try {
                                text1.setText(messages.get(position).getSender().getAddress());
                            } catch (Exception e) {}
                            text2.setText(messages.get(position).getSubject());
                            return view;
                        }
                    };

                    setListAdapter(adapter);
                } catch (Exception e) {
                    Log.e("office365simpledemo", "error", e);
                }
            }
        });
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mAuthContext != null) {
            mAuthContext.onActivityResult(requestCode, resultCode, data);
        }
    }

}
