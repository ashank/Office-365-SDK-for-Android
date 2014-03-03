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


import java.io.Serializable;

import com.microsoft.exchange.services.odata.model.types.IMessage;
import com.microsoft.exchange.services.odata.model.types.Importance;
import com.microsoft.exchange.services.odata.model.types.ItemBody;
import com.microsoft.exchange.services.odata.model.types.Recipient;

/**
 * Represents mail item assignable to a specific box.
 */
public class MailItem implements Serializable {

    private static final long serialVersionUID = -8581792564325186552L;

    private String mSubject;
    private ItemBody mBody;
    private String mBodyPreview;
    private Importance mImportance;
    private boolean mHasAttachments;
    private Recipient mFrom;
    private Recipient mSender;
    private String mId;
    private boolean mIsRead;

    /**
     * Default constructor.
     */
    public MailItem(IMessage source) {
        mSubject = source.getSubject();
        mBody = source.getBody();
        mBodyPreview = source.getBodyPreview();
        mImportance = source.getImportance();
        mHasAttachments = source.getHasAttachments();

        mFrom = source.getFrom();
        mSender = source.getSender();
        mId = source.getId();
        mIsRead = source.getIsRead();
    }

    public String getSubject() {
        return mSubject;
    }

    public void setSubject(String _subject) {
        mSubject = _subject;
    }

    public ItemBody getBody() {
        return mBody;
    }

    public void setBody(ItemBody _body) {
        mBody = _body;
    }

    public String getBodyPreview() {
        return mBodyPreview;
    }

    public void setBodyPreview(String _bodyPreview) {
        mBodyPreview = _bodyPreview;
    }

    public Importance getImportance() {
        return mImportance;
    }

    public void setImportance(Importance importance) {
        mImportance = importance;
    }

    public Boolean getHasAttachments() {
        return mHasAttachments;
    }

    public void setHasAttachments(Boolean _hasAttachments) {
        mHasAttachments = _hasAttachments;
    }

    public Recipient getFrom() {
        return mFrom;
    }

    public void setFrom(Recipient _from) {
        mFrom = _from;
    }

    public String getId() {
        return mId;
    }
    
    public void setId(String _id) {
        mId = _id;
    }
    
    public Recipient getSender() {
        return mSender;
    }

    public boolean getIsRead() {
        return mIsRead;
    }
    
    public void setIsRead(boolean _isRead) {
        mIsRead = _isRead;
    }
}
