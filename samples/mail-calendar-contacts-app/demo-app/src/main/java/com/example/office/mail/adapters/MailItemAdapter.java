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
package com.example.office.mail.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.office.R;
import com.example.office.adapters.SearchableAdapter;
import com.example.office.mail.data.MailItem;
import com.microsoft.exchange.services.odata.model.types.Importance;

/**
 * Adapter for displaying MailItem in ListView
 */
public class MailItemAdapter extends SearchableAdapter<MailItem> {

    /**
     * Default constructor.
     *
     * @param context Application context.
     * @param resource List item resource id.
     * @param data Data to populate.
     */
    public MailItemAdapter(Context context, int resource, List<MailItem> data) {
        super(context, resource, data);
    }

    @Override
    protected boolean isMatch(MailItem item, CharSequence constraint) {
        if (item != null && !TextUtils.isEmpty(constraint)) {
            List<String> list = new ArrayList<String>();
            list.add(item.getSender() == null ? null : item.getSender().getAddress());
            list.add(item.getBodyPreview());
            list.add(item.getSubject());

            for (String value : list) {
                if (!TextUtils.isEmpty(value) && value.contains(constraint)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Constructs and returns View for filling ListView
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            MailItemHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(mItemResource, null);

                holder = new MailItemHolder();
                holder.sender = (TextView) convertView.findViewById(R.id.mailSenderName);
                holder.date = (TextView) convertView.findViewById(R.id.mailSentTime);
                holder.subject = (TextView) convertView.findViewById(R.id.mailSubject);
                holder.content = (TextView) convertView.findViewById(R.id.mailContent);
                holder.hasAttachments = (ImageView) convertView.findViewById(R.id.mail_attachment_icon);
                holder.additionalPropertyIcon = (ImageView) convertView.findViewById(R.id.additional_property_icon);

                convertView.setTag(holder);
            } else {
                holder = (MailItemHolder) convertView.getTag();
            }

            MailItem item = getItem(position);
            if (item != null) {
                String sender = getContext().getString(R.string.unknown_sender_text_stub);
                if (item.getSender() != null) {
                    if (!TextUtils.isEmpty(item.getSender().getAddress())) {
                        sender = item.getSender().getAddress();
                    }
                }
                setViewText(holder.sender, sender);
                setViewText(holder.date, "");

                String subject = item.getSubject() == null ? "" : item.getSubject();
                setViewText(holder.subject, subject);

                String preview = item.getBodyPreview() == null ? "" : item.getBodyPreview();
                setViewText(holder.content, preview);
                holder.hasAttachments.setVisibility(item.getHasAttachments() ? View.VISIBLE : View.GONE);
                holder.additionalPropertyIcon
                        .setVisibility(!item.getIsRead() || item.getImportance() == Importance.High ? View.VISIBLE
                                : View.INVISIBLE);
                if (item.getImportance() == Importance.High) {
                    holder.additionalPropertyIcon.setBackgroundResource(android.R.drawable.star_on);
                } else if (!item.getIsRead()) {
                    holder.additionalPropertyIcon.setBackgroundResource(R.drawable.ic_new_message);
                }
            }
        } catch (Exception e) {}
        return convertView;
    }

    /**
     * Represents an inner structure of single ListView item
     */
    private class MailItemHolder {
        ImageView additionalPropertyIcon;
        TextView sender;
        TextView date;
        TextView subject;
        TextView content;
        ImageView hasAttachments;
    }

}
