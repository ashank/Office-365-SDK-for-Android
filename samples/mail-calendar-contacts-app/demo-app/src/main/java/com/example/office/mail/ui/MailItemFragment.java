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
package com.example.office.mail.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.office.R;
import com.example.office.logger.Logger;
import com.example.office.mail.data.MailConfig;
import com.example.office.mail.data.MailItem;
import com.example.office.mail.storage.MailConfigPreferences;
import com.example.office.ui.BaseFragment;
import com.microsoft.exchange.services.odata.model.types.BodyType;
import com.microsoft.exchange.services.odata.model.types.Importance;

/**
 * Email details fragment.
 */
public class MailItemFragment extends BaseFragment {

    /**
     * Currently displayed email
     */
    protected MailItem mail;

    @Override
    protected int getFragmentLayoutId() {
        return R.layout.mail_item_fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        try {
            Activity activity = getActivity();
            activity.getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

            Intent intent = getActivity().getIntent();
            mail = (MailItem) intent.getExtras().get(getActivity().getString(R.string.intent_mail_key));
            displayMail(rootView);
            getActivity().setProgressBarIndeterminateVisibility(false);
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + ".onCreateView(): Error.");
        }

        return rootView;
    }

    /**
     * Fills fragment content with email fields
     *
     * @param root Root view for current fragment
     */
    private void displayMail(View root) {
        try {
            TextView subjectView = (TextView) root.findViewById(R.id.mail_fragment_subject);
            subjectView.setText(mail.getSubject());

            String sender = getActivity().getString(R.string.unknown_sender_text_stub);
            if (mail.getSender() != null) {
                if (!TextUtils.isEmpty(mail.getSender().getAddress())) {
                    sender = mail.getSender().getAddress();
                }
            }
            TextView participantsView = (TextView) root.findViewById(R.id.mail_fragment_participants);
            participantsView.setText(getActivity().getString(R.string.me_and_somebody_text_stub) + sender);

            ImageView importanceIcon = (ImageView) root.findViewById(R.id.mail_fragment_icon_mark_as_important);
            if (mail.getImportance() == Importance.High) {
                importanceIcon.setImageResource(android.R.drawable.star_on);
            } else {
                importanceIcon.setImageResource(android.R.drawable.star_off);
            }
            importanceIcon.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Importance importance = mail.getImportance();
                    if (importance == Importance.High) {
                        importance = Importance.Normal;
                    } else {
                        importance = Importance.High;
                    }

                    setEmailImportance(importance);

                    if (importance == Importance.High) {
                        ((ImageView) v).setImageResource(android.R.drawable.star_on);
                    } else {
                        ((ImageView) v).setImageResource(android.R.drawable.star_off);
                    }
                }
            });

            TextView senderView = (TextView) root.findViewById(R.id.mail_fragment_sender);
            senderView.setText(sender);

            TextView dateView = (TextView) root.findViewById(R.id.mail_fragment_date);
            dateView.setText("");

            WebView webview = (WebView) root.findViewById(R.id.mail_fragment_content);
            if (mail.getBody().getContentType() == BodyType.HTML) {
                webview.loadData(mail.getBody().getContent(), getActivity().getString(R.string.mime_type_text_html), "utf8");
            } else {
                webview.loadData(mail.getBody().getContent(), getActivity().getString(R.string.mime_type_text_plain), "utf8");
            }
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + ".displayMail(): Error.");
        }
    }

    /**
     * Sets and saves current mail importance
     *
     * @param importance Indicates new importance.
     */
    private void setEmailImportance(Importance importance) {
        try {
            mail.setImportance(importance);
            MailConfig config = MailConfigPreferences.loadConfig();
            config.updateMailById(mail.getId(), mail);
            MailConfigPreferences.saveConfiguration(config);
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + ".setEmailImportance(): Error.");
        }
    }
}
