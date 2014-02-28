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

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

import com.example.office.Configuration;
import com.example.office.Constants;
import com.example.office.Constants.UI;
import com.example.office.OfficeApplication;
import com.example.office.R;
import com.example.office.auth.AbstractOfficeAuthenticator;
import com.example.office.auth.AuthType;
import com.example.office.auth.OfficeCredentials;
import com.example.office.logger.Logger;
import com.example.office.mail.adapters.MailItemAdapter;
import com.example.office.mail.data.MailConfig;
import com.example.office.mail.data.MailItem;
import com.example.office.mail.storage.MailConfigPreferences;
import com.example.office.mail.ui.MailItemActivity;
import com.example.office.storage.AuthPreferences;
import com.example.office.ui.ListFragment;
import com.example.office.utils.Utils;
import com.microsoft.exchange.services.odata.model.Me;
import com.microsoft.office.core.auth.IOfficeCredentials;

/**
 * Base fragment containing logic related to managing items.
 */
public abstract class ItemsFragment extends ListFragment<MailItem, MailItemAdapter> {

    /**
     * View used as a footer of the list;
     */
    protected View mListFooterView;

    /**
     * Oauth2 office authenticator.
     */
    protected AbstractOfficeAuthenticator mOfficeAuthenticator = null;

    protected int getListItemLayoutId() {
        return R.layout.mail_list_item;
    }

    @Override
    protected int getFragmentLayoutId() {
        return R.layout.mail_list_fragment;
    }

    @Override
    protected int getListViewId() {
        return R.id.mail_list;
    }

    @Override
    protected int getProgressViewId() {
        return R.id.mail_list_progress;
    }

    @Override
    protected int getContentContainerId() {
        return R.id.mail_list;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        try {
            final Activity activity = getActivity();

            final ListView mailListView = (ListView) rootView.findViewById(getListViewId());
            mailListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        MailItem mail = getListAdapterInstance().getItem(position);
                        mail.setIsRead(true);
                        MailConfig config = MailConfigPreferences.loadConfig();
                        config.updateMailById(mail.getId(), mail);
                        MailConfigPreferences.saveConfiguration(config);

                        Intent intent = new Intent(OfficeApplication.getContext(), MailItemActivity.class);
                        intent.putExtra(getActivity().getString(R.string.intent_mail_key), mail);
                        activity.startActivity(intent);

                    } catch (Exception e) {
                        Logger.logApplicationException(e, getClass().getSimpleName() + ".listView.onItemClick(): Error.");
                    }
                }
            });
            registerForContextMenu(mailListView);

        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + ".onCreateView(): Error.");
        }
        return rootView;
    }

    /**
     * Returns {@link Constants.UI.Screen} tht this fragment is describing.
     *
     * @return Box for this fragment, or <code>null</code> in case of error.
     */
    protected abstract UI.Screen getBox();

    @Override
    protected List<MailItem> getListData() {
        try {
            MailConfig config = MailConfigPreferences.loadConfig();
            boolean isValidList = false;
            if (config != null) {
                List<MailItem> mails = config.getMails();
                isValidList = mails != null && !mails.isEmpty();
                if (isValidList) {
                    return Utils.boxMail(mails, getBox());
                }
            }
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + ".getListData(): Error.");
        }
        return null;
    }

    @Override
    protected View getListFooterViewInstance() {
        try {
            if (mListFooterView == null) {
                mListFooterView = mInflater.inflate(R.layout.mail_list_footer, null);
                ((TextView) mListFooterView.findViewById(R.id.footer_mail_count)).setText(String.valueOf(getListAdapterInstance().getCount()));
            }
            return mListFooterView;
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + ".getListFooterView(): Error.");
        }
        return null;
    }

    @Override
    public MailItemAdapter getListAdapterInstance(List<MailItem> data) {
        try {
            if (mAdapter == null) {
                mAdapter = new MailItemAdapter(getActivity(), getListItemLayoutId(), data != null ? data : getListData());
            }
            return mAdapter;
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + ".getListAdapter(): Error.");
        }
        return null;
    }

    @Override
    protected void initList() {
        try {
            List<MailItem> mails = getListData();
            if (mails != null && !mails.isEmpty()) {
                updateList(mails);
            }
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + "initList(): Error.");
        }
    }

    /**
     * Updates list with new data.
     *
     * @param items Items to be displayed in the list.
     */
    protected void updateList(List<MailItem> items) {
        try {
            getListAdapterInstance().update(Utils.boxMail(items, getBox()));

            View rootView = getView();
            if (rootView != null) {
                ListView mailListView = (ListView) rootView.findViewById(getListViewId());
                if (mailListView != null) {
                    mailListView.setAdapter(getListAdapterInstance());
                }

                if (mListFooterView != null) {
                    if (getListAdapterInstance().getCount() <= 0) {
                        mailListView.removeFooterView(mListFooterView);
                    } else {
                        if (mailListView.getFooterViewsCount() == 0) {
                            mailListView.addFooterView(mListFooterView);
                        }
                        ((TextView) mListFooterView.findViewById(R.id.footer_mail_count))
                                .setText(String.valueOf(getListAdapterInstance().getCount()));
                    }
                }
            }
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + ".updateList(): Error.");
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {              
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == getListViewId()) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.mail_item_menu, menu);
        }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.mail_menu_send:
                final String id = getListAdapterInstance().getItem(info.position).getId();
                AsyncTask.execute(new Runnable() {
                    public void run() {
                        Me.getMessages().get(id).send();
                    }
                });
                MailConfig config = MailConfigPreferences.loadConfig();
                config.removeMailById(id);
                MailConfigPreferences.saveConfiguration(config);
                getListAdapterInstance().remove(info.position);
                getListAdapterInstance().notifyDataSetChanged();
                ((TextView) getListFooterViewInstance().findViewById(R.id.footer_mail_count)).setText(String.valueOf(config.getMails().size()));
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * Creates and returns an instance of authenticator used to get access to endpoint.
     * 
     * @return authenticator.
     */
    protected AbstractOfficeAuthenticator getAuthenticator() {
        return new AbstractOfficeAuthenticator() {
            @Override
            protected IOfficeCredentials getCredentials() {
                return AuthPreferences.loadCredentials();
            }
            @Override
            protected Activity getActivity() {
                return ItemsFragment.this.getActivity();
            }
            
            @Override
            public void onDone(String result) {
                super.onDone(result);
                AuthPreferences.storeCredentials(getCredentials().setToken(result));
            }
        };
    }
    
    /**
     * Returns TEST or RELEASE version of end point to retrieve list of emails in the inbox depending on {@link Configuration#DEBUG}
     * constant value.
     *
     * @return URL to retrieve list of emails in the inbox.
     */
    private String getEndpoint() {
        return Constants.MAIL_MESSAGES_TEST;
    }
    
    /**
     * Sets application configuration, like endpoint and credentials.
     */
    protected void setPreferences() {
        com.microsoft.office.core.Configuration.setServerBaseUrl(getEndpoint());
    
        OfficeCredentials creds = (OfficeCredentials) AuthPreferences.loadCredentials();
        // First timer
        if (creds == null) {
            creds = new OfficeCredentials(Constants.AUTHORITY_URL, Constants.CLIENT_ID, Constants.RESOURCE_ID, Constants.REDIRECT_URL);
            creds.setUserHint(Constants.USER_HINT);
            creds.setAuthType(AuthType.OAUTH);
            AuthPreferences.storeCredentials(creds);
        }
    
        mOfficeAuthenticator = getAuthenticator();
        com.microsoft.office.core.Configuration.setAuthenticator(mOfficeAuthenticator);
    }
    
}
