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
package com.microsoft.office.core;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

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

    @BeforeClass
    public static void prepareMessage() {
        message = Messages.newMessage();
        message.setSubject("Attachments test");
        Me.flush();
    }

    @BeforeClass
    public static void prepareAttachedMessage() {
        itemAttachment = Messages.newMessage();
        itemAttachment.setSubject(itemAttachmentSubject);
        Me.flush();
    }

    @AfterClass
    public static void removeMessage() {
        Me.getMessages().delete(message.getId());
        Me.flush();
    }

    @AfterClass
    public static void removeAttachedMessage() {
        Me.getMessages().delete(itemAttachment.getId());
        Me.flush();
    }

    @Test
    public void createFileAttachmentTest() {
        IFileAttachment attachment = createFileAttachment();

        Me.flush();
        checkCreated(attachment);

        removeAttachment(attachment);
    }

    @Test
    public void readFileAttachmentTest() {
        IFileAttachment attachment = createFileAttachment();
        Me.flush();

        checkFileAttachmentRead(attachment);
        removeAttachment(attachment);
    }

    @Test
    @Ignore(value = "HTTP 405 Method Not Allowed")
    public void updateFileAttachmentTest() {
        IFileAttachment attachment = createFileAttachment();
        Me.flush();

        checkUpdated(attachment);

        removeAttachment(attachment);
    }

    @Test
    public void deleteFileAttachmentTest() {
        IFileAttachment attachment = createFileAttachment();
        Me.flush();

        removeAttachment(attachment);
        checkDeleted(attachment);
    }

    @Test
    public void fileAttachmentCRUDTest() {
        IFileAttachment attachment = createFileAttachment();
        Me.flush();
        checkCreated(attachment);
        checkFileAttachmentRead(attachment);
        // update seems to be not implemented so skip
//        checkUpdated(attachment);
        removeAttachment(attachment);
        checkDeleted(attachment);
    }

    @Test
    @Ignore(value = "HTTP 500 Internal Server Error")
    public void createItemAttachmentTest() {
        IItemAttachment attachment = createItemAttachment();
        Me.flush();
        checkCreated(attachment);
        removeAttachment(attachment);
    }

    @Test
    @Ignore(value = "HTTP 500 Internal Server Error")
    public void readItemAttachmentTest() {
        IItemAttachment attachment = createItemAttachment();
        Me.flush();

        checkItemAttachmentRead(attachment);
        removeAttachment(attachment);
    }

    @Test
    @Ignore(value = "HTTP 500 Internal Server Error")
    public void updateItemAttachmentTest() {
        IItemAttachment attachment = createItemAttachment();
        Me.flush();
        updateItemAttachmentAndCheck(attachment);
        removeAttachment(attachment);
    }

    @Test
    @Ignore(value = "HTTP 500 Internal Server Error")
    public void deleteItemAttachmentTest() {
        IItemAttachment attachment = createItemAttachment();
        Me.flush();

        removeAttachment(attachment);
        checkDeleted(attachment);
    }

    @Test
    @Ignore(value = "HTTP 500 Internal Server Error")
    public void itemAttachmentCRUDTest() {
        IItemAttachment attachment = createItemAttachment();
        Me.flush();
        checkCreated(attachment);
        checkItemAttachmentRead(attachment);
        updateItemAttachmentAndCheck(attachment);
        removeAttachment(attachment);
        checkDeleted(attachment);
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
        byte[] content = getImage("/images/mslogo.png");

        assertArrayEquals(content, attachment.getContentBytes());
        assertEquals(attachmentName, attachment.getName());

        assertArrayEquals(content, message.getAttachments().get(attachment.getId(), IFileAttachment.class).getContentBytes());
        assertEquals(attachmentName, message.getAttachments().get(attachment.getId()).getName());
    }

    private void checkUpdated(IFileAttachment attachment) {
        byte[] content = getImage("/images/mslogo-inverted.png");
        attachment.setContentBytes(content);
        Me.flush();
        assertArrayEquals(content, message.getAttachments().get(attachment.getId(), IFileAttachment.class).getContentBytes());
    }

    private void checkDeleted(IAttachment attachment) {
        assertNull(message.getAttachments().get(attachment.getId()));
    }

    private IFileAttachment createFileAttachment() {
        IFileAttachment attachment = message.getAttachments().newFileAttachment();
        attachment.setContentBytes(getImage("/images/mslogo.png"));
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
        byte[] content = null;
        try {
            content = IOUtils.toByteArray(getClass().getResourceAsStream(path));
        } catch (IOException e) {
            fail("Unable to read attachment file");
        }
        return content;
    }

    private void removeAttachment(IAttachment attachment) {
        message.getAttachments().delete(attachment.getId());
        Me.flush();
    }
}
