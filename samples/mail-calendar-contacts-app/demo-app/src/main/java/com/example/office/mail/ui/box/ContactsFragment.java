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
package com.example.office.mail.ui.box;

import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.office.Constants.UI;
import com.example.office.R;
import com.example.office.logger.Logger;
import com.example.office.mail.data.NetworkState;
import com.example.office.utils.NetworkUtils;
import com.microsoft.exchange.services.odata.model.IContacts;
import com.microsoft.exchange.services.odata.model.Me;
import com.microsoft.exchange.services.odata.model.types.IContact;
import com.msopentech.odatajclient.engine.client.ODataClientFactory;
import com.msopentech.odatajclient.proxy.api.AsyncCall;

/**
 * Contains contacts.
 */
public class ContactsFragment extends ItemsFragment {
    
    private boolean isInitializing = false;
    
    @Override
    protected UI.Screen getScreen() {
        return UI.Screen.CONTACTS;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        final ListView listView = (ListView) rootView.findViewById(getListViewId());
        listView.setOnItemClickListener(null);
        
        return rootView;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (!isInitializing) {
            isInitializing = true;
            initList();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initList() {
        try {
            NetworkState nState = NetworkUtils.getNetworkState(getActivity());
            if (nState.getWifiConnectedState() || nState.getDataState() == NetworkUtils.NETWORK_UTILS_CONNECTION_STATE_CONNECTED) {
                showWorkInProgress(true, true);
                setPreferences();

                // TODO: wrap this implementation
                final Future<ArrayList<IContact>> contacts = new AsyncCall<ArrayList<IContact>>(ODataClientFactory.getV4().getConfiguration()) {
                    @Override
                    public ArrayList<IContact> call() {
                        IContacts c = Me.getContacts();

                        ArrayList<IContact> result = new ArrayList<IContact>();
                        for (IContact contact : c) {
                            result.add(contact);
                        }

                        return result;
                    }
                };

                new AsyncTask<Future<ArrayList<IContact>>, Void, Void>() {
                    @Override
                    protected Void doInBackground(final Future<ArrayList<IContact>>... params) {
                        try {
                            final ArrayList<IContact> result = contacts.get(12000, TimeUnit.SECONDS);
                            if (result != null) {
                                onDone(result);
                            } else {
                                onError(new Exception("Error while processing Contacts request"));
                            }
                        } catch (final Exception e) {
                            onError(e);
                        } finally {
                            isInitializing = false;
                        }

                        return null;
                    }
                }.execute(contacts);
            } else {
                Toast.makeText(getActivity(), R.string.data_connection_no_data_connection, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + "initList(): Error.");
        }
    }
    
    /**
     * Invoked when Contacts retrieving operation has been succeeded.
     * 
     * @param result Result of operation.
     */
    public void onDone(final ArrayList<IContact> result) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ListView listView = (ListView) getActivity().findViewById(getListViewId());
                
                ArrayAdapter<IContact> adapter = new ArrayAdapter<IContact>(getActivity(), android.R.layout.simple_list_item_2,
                        android.R.id.text1, result) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                        TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                        text1.setText(result.get(position).getDisplayName());
                        text2.setText(result.get(position).getGivenName());
                        return view;
                    }
                };
                listView.setAdapter(adapter);
                
                showWorkInProgress(false, false);
                listView.setVisibility(View.VISIBLE);
                ((TextView) getListFooterViewInstance().findViewById(R.id.footer_mail_count)).setText(String.valueOf(result.size()));
            }
        });
    }
    
    /**
     * Invoked when Contacts retrieving operation has been failed.
     * 
     * @param e an exception occured.
     */
    public void onError(final Throwable e) {
        Logger.logApplicationException(new Exception(e), getClass().getSimpleName() + ".onExecutionComplete(): Error.");
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                showWorkInProgress(false, false);
                getActivity().findViewById(R.id.mail_list).setVisibility(View.GONE);
                ((TextView) getActivity().findViewById(R.id.mail_failure_retrieving_message)).setText(R.string.contacts_retrieving_failure_message);
                getActivity().findViewById(R.id.mail_failure_retrieving_message).setVisibility(View.VISIBLE);
            }
        });
    }
}
