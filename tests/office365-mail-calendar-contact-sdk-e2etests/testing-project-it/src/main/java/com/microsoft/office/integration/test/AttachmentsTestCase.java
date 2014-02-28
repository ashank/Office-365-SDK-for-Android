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
package com.microsoft.office.integration.test;

import com.microsoft.exchange.services.odata.model.Me;
import com.microsoft.exchange.services.odata.model.Messages;
import com.microsoft.exchange.services.odata.model.types.IAttachment;
import com.microsoft.exchange.services.odata.model.types.IFileAttachment;
import com.microsoft.exchange.services.odata.model.types.IItem;
import com.microsoft.exchange.services.odata.model.types.IItemAttachment;
import com.microsoft.exchange.services.odata.model.types.IMessage;

public class AttachmentsTestCase extends AbstractTest {

    private static IMessage message;

    private final String attachmentName = "test attachment";

    private static IItem itemAttachment;

    private static final String itemAttachmentSubject = "item attachment test";

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        message = Messages.newMessage();
        message.setSubject("Attachments test");
        Me.flush();

        itemAttachment = Messages.newMessage();
        itemAttachment.setSubject(itemAttachmentSubject);
        Me.flush();
    }

    public void testCreateFileAttachment() {
        IFileAttachment attachment = createFileAttachment();

        Me.flush();
        checkCreated(attachment);

        removeAttachment(attachment);
        removeMessages();
    }

    public void testReadFileAttachment() {
        IFileAttachment attachment = createFileAttachment();
        Me.flush();

        checkFileAttachmentRead(attachment);
        removeAttachment(attachment);

        removeMessages();
    }

    public void itestUpdateFileAttachment() {//HTTP 405 Method Not Allowed
        IFileAttachment attachment = createFileAttachment();
        Me.flush();

        checkUpdated(attachment);

        removeAttachment(attachment);

        removeMessages();
    }

    public void testDeleteFileAttachment() {
        IFileAttachment attachment = createFileAttachment();
        Me.flush();

        removeAttachment(attachment);
        checkDeleted(attachment);

        removeMessages();
    }

    public void testFileAttachmentCRUD() {
        IFileAttachment attachment = createFileAttachment();
        Me.flush();
        checkCreated(attachment);
        checkFileAttachmentRead(attachment);
        // update seems to be not implemented so skip
//        checkUpdated(attachment);
        removeAttachment(attachment);
        checkDeleted(attachment);

        removeMessages();
    }

    public void itestCreateItemAttachment() {//HTTP 500 Internal Server Error
        IItemAttachment attachment = createItemAttachment();
        Me.flush();
        checkCreated(attachment);
        removeAttachment(attachment);

        removeMessages();
    }

    public void itestReadItemAttachment() {//HTTP 500 Internal Server Error
        IItemAttachment attachment = createItemAttachment();
        Me.flush();

        checkItemAttachmentRead(attachment);
        removeAttachment(attachment);

        removeMessages();
    }

    public void itestupdateItemAttachment() {//HTTP 500 Internal Server Error
        IItemAttachment attachment = createItemAttachment();
        Me.flush();
        updateItemAttachmentAndCheck(attachment);
        removeAttachment(attachment);

        removeMessages();
    }

    public void itestDeleteItemAttachment() {//HTTP 500 Internal Server Error
        IItemAttachment attachment = createItemAttachment();
        Me.flush();

        removeAttachment(attachment);
        checkDeleted(attachment);

        removeMessages();
    }

    public void itestItemAttachmentCRUD() {//HTTP 500 Internal Server Error
        IItemAttachment attachment = createItemAttachment();
        Me.flush();
        checkCreated(attachment);
        checkItemAttachmentRead(attachment);
        updateItemAttachmentAndCheck(attachment);
        removeAttachment(attachment);
        checkDeleted(attachment);

        removeMessages();
    }

    private void updateItemAttachmentAndCheck(IItemAttachment attachment) {
        IItem newItem = Messages.newMessage().setSubject("updated item");
        attachment.setItem(newItem);
        Me.flush();
        assertEquals(newItem.getId(), message.getAttachments().iterator().next().getId());
        Me.getMessages().delete(newItem.getId());
    }

    private void checkItemAttachmentRead(IAttachment attachment) {
        assertNotNull(message.getAttachments().get(itemAttachment.getId()));
    }

    private void checkCreated(IAttachment attachment) {
        assertNotNull(attachment.getId());
    }

    private void checkFileAttachmentRead(IFileAttachment attachment) {
        byte[] content = getImage("images/mslogo.png");

        assertArrayEquals(content, attachment.getContentBytes());
        assertEquals(attachmentName, attachment.getName());

        assertArrayEquals(content, message.getAttachments().get(attachment.getId(), IFileAttachment.class).getContentBytes());
        assertEquals(attachmentName, message.getAttachments().get(attachment.getId()).getName());
    }

    private void checkUpdated(IFileAttachment attachment) {
        byte[] content = getImage("images/mslogo-inverted.png");
        attachment.setContentBytes(content);
        Me.flush();
        assertArrayEquals(content, message.getAttachments().get(attachment.getId(), IFileAttachment.class).getContentBytes());
    }

    private void checkDeleted(IAttachment attachment) {
        assertNull(message.getAttachments().get(attachment.getId()));
    }

    private IFileAttachment createFileAttachment() {
        IFileAttachment attachment = message.getAttachments().newFileAttachment();
        attachment.setContentBytes(getImage("images/mslogo.png"));
        attachment.setName(attachmentName);
        return attachment;
    }

    private IItemAttachment createItemAttachment() {
        IItemAttachment attachment = message.getAttachments().newItemAttachment();
        attachment.setName(attachmentName);
        Me.flush();
        attachment.setItem(itemAttachment);
        return attachment;
    }

    private byte[] getImage(String path) {
        return getImageInByteFromResource(path);
    }

    private void removeAttachment(IAttachment attachment) {
        message.getAttachments().delete(attachment.getId());
        Me.flush();
    }

    private void removeMessages() {
        Me.getMessages().delete(message.getId());
        Me.flush();

        Me.getMessages().delete(itemAttachment.getId());
        Me.flush();
    }
}
