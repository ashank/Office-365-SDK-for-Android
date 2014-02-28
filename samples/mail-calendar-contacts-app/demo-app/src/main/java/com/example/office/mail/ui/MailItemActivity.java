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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.example.office.R;
import com.example.office.logger.Logger;
import com.example.office.mail.data.MailConfig;
import com.example.office.mail.data.MailItem;
import com.example.office.mail.storage.MailConfigPreferences;
import com.example.office.ui.BaseActivity;
import com.microsoft.exchange.services.odata.model.Me;

/**
 * Activity managing specific email details.
 */
public class MailItemActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mail_item_activity);
        try {
            ActionBar actionBar = getActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            findViewById(R.id.mail_send_button).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMail();
                    finish();
                }
            });
        } catch (Exception e) {
            Logger.logApplicationException(e, getClass().getSimpleName() + ".onCreate(): Error.");
        }
    }
    
    /**
     * Sends email given in intent in different thread.
     */
    public void sendMail() {
        final MailItem mail = (MailItem) getIntent().getExtras().get(getString(R.string.intent_mail_key));
        AsyncTask.execute(new Runnable() {
            public void run() {
                try {
                    Me.getMessages().get(mail.getId()).send();
                } catch (Exception e) {
                    showErrorDuringSending("Error during message send");
                }
            }
        });
        
        MailConfig config = MailConfigPreferences.loadConfig();
        config.removeMailById(mail.getId());
        MailConfigPreferences.saveConfiguration(config);
    }

    /**
     * Shows toast with error message.
     */
    protected void showErrorDuringSending(final String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MailItemActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

}
