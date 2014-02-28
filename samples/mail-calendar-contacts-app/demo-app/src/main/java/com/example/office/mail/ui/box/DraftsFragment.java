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
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.office.Constants.UI;
import com.example.office.R;
import com.example.office.async.IOperationCallback;
import com.example.office.logger.Logger;
import com.example.office.mail.data.MailConfig;
import com.example.office.mail.data.MailItem;
import com.example.office.mail.data.NetworkState;
import com.example.office.mail.storage.MailConfigPreferences;
import com.example.office.utils.NetworkUtils;
import com.microsoft.exchange.services.odata.model.IMessages;
import com.microsoft.exchange.services.odata.model.Me;
import com.microsoft.exchange.services.odata.model.types.IFolder;
import com.microsoft.exchange.services.odata.model.types.IMessage;
import com.msopentech.odatajclient.engine.client.ODataClientFactory;
import com.msopentech.odatajclient.proxy.api.AsyncCall;

/**
 * 'Drafts' fragment containing logic related to managing drafts emails.
 */
public class DraftsFragment extends ItemsFragment implements IOperationCallback<List<IMessage>> {

    private boolean isInitializing = false;
    
    /**
     * Handler to process actions on UI thread when async task is finished.
     */
    private Handler mHandler;

    /**
     * Default constructor.
     */
    public DraftsFragment() {
        super();
        mHandler = new Handler();
    }

    @Override
    protected UI.Screen getBox() {
        return UI.Screen.MAILBOX;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (!isInitializing) {
            initList();
            isInitializing = true;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initList() {
        try {
            List<MailItem> mails = getListData();
            boolean hasData = false;
            if (hasData = (mails != null && !mails.isEmpty())) {
                updateList(mails);
            }

            // Update list from the web.
            NetworkState nState = NetworkUtils.getNetworkState(getActivity());
            if (nState.getWifiConnectedState() || nState.getDataState() == NetworkUtils.NETWORK_UTILS_CONNECTION_STATE_CONNECTED) {
                showWorkInProgress(true, !hasData);
                setPreferences();

                //TODO: wrap this implementation
                final Future<ArrayList<IMessage>> emails = new AsyncCall<ArrayList<IMessage>>(
                		ODataClientFactory.getV4().getConfiguration()) {
                    @Override
                    public ArrayList<IMessage> call() {
                        IFolder drafts = Me.getDrafts();
                        IMessages c = drafts.getMessages();

                        ArrayList<IMessage> result = new ArrayList<IMessage>();
                        for (IMessage message : c) {
                            result.add(message);
                        }

                        return result;
                    }
                };

                new AsyncTask<Future<ArrayList<IMessage>>, Void, Void>() {
                    @Override
                    protected Void doInBackground(final Future<ArrayList<IMessage>>... params) {
                        try {
                            final ArrayList<IMessage> result = emails.get(12000, TimeUnit.SECONDS);
                            if (result != null) {
                                onDone(result);
                            } else {
                                onError(new Exception("Error while processing Emails request"));
                            }
                        } catch (final Exception e) {
                            onError(e);
                        } finally {
                            isInitializing = false;
                        }

                        return null;
                    }
                }.execute(emails);
            } else {
                Toast.makeText(getActivity(), R.string.data_connection_no_data_connection, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + "initList(): Error.");
        }
    }

    @Override
    public void onDone(final List<IMessage> result) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                MailConfig newConfig = new MailConfig(System.currentTimeMillis());
                final List<MailItem> boxedMails = new ArrayList<MailItem>();
                for (IMessage mail : result) {
                    boxedMails.add(new MailItem(mail, UI.Screen.MAILBOX));
                }

                newConfig.setMails(boxedMails);
                MailConfigPreferences.updateConfiguration(newConfig);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        showWorkInProgress(false, false);
                        updateList(boxedMails);
                    }
                });
                return null;
            }
        }.execute();
    }

    @Override
    public void onError(final Throwable e) {
        Logger.logApplicationException(new Exception(e), getClass().getSimpleName() + ".onExecutionComplete(): Error.");
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                showWorkInProgress(false, false);
                getActivity().findViewById(R.id.mail_list).setVisibility(View.GONE);
                ((TextView) getActivity().findViewById(R.id.mail_failure_retrieving_message)).setText(R.string.mails_retrieving_failure_message);
                getActivity().findViewById(R.id.mail_failure_retrieving_message).setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mOfficeAuthenticator != null) {
            mOfficeAuthenticator.onActivityResult(requestCode, resultCode, data);
        }
    }
}
