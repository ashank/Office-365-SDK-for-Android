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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.exchange.services.odata.model.DefaultFolder;
import com.microsoft.exchange.services.odata.model.Me;
import com.microsoft.exchange.services.odata.model.Messages;
import com.microsoft.exchange.services.odata.model.types.BodyType;
import com.microsoft.exchange.services.odata.model.types.IMessage;
import com.microsoft.exchange.services.odata.model.types.IMessageCollection;
import com.microsoft.exchange.services.odata.model.types.Importance;
import com.microsoft.exchange.services.odata.model.types.ItemBody;
import com.microsoft.exchange.services.odata.model.types.Recipient;
import com.msopentech.odatajclient.engine.data.ODataEntity;
import com.msopentech.odatajclient.engine.data.ODataProperty;


public class MessagesTestCase extends AbstractTest {

    private static IMessage message;

    private static ODataEntity sourceMessage;

    public void testCreate() {
        createAndCheck();
        // clean-up
        removeMessage();
    }

    public void testRead() {
     // create message first
        prepareMessage();
        Me.flush();
        readAndCheck();
        //clean-up
        removeMessage();
    }

    public void testUpdate() {
        // create message first
        prepareMessage();
        Me.flush();
        updateAndCheck();
        // clean up
        removeMessage();
    }

    public void testDelete() {
        // create message first
        prepareMessage();
        Me.flush();

        // then remove
        deleteAndCheck();
    }

    public void testCreateInDefaultFolder() {
        message = Messages.newMessage();
        sourceMessage = getEntityFromResource("simpleMessage.json");
        String subject = sourceMessage.getProperty("Subject").getPrimitiveValue().toString();
        message.setSubject(subject);
        Me.flush();
        assertTrue(StringUtils.isNotEmpty(message.getId()));
        assertEquals(message.getParentFolderId(), Me.getDrafts().getId());

        removeMessage();
    }

    public void testEnums() {
        prepareMessage();
        ItemBody body = new ItemBody();
        body.setContent("<!DOCTYPE html><html><body><h1>test</h1></body></html>");
        body.setContentType(BodyType.HTML);
        message.setBody(body);
        assertEquals(message.getBody().getContentType(), body.getContentType());
        message.setImportance(Importance.Low);
        assertEquals(message.getImportance(), Importance.Low);

        Me.flush();
        message = Me.getMessages().get(message.getId());
        assertEquals(message.getBody().getContentType(), body.getContentType());
        assertEquals(message.getImportance(), Importance.Low);
        removeMessage();
    }

    public void testMessageCRUD() {
        try {
            // CREATE
            createAndCheck();

            // READ
            readAndCheck();

            // UPDATE
            updateAndCheck();

            // DELETE
            deleteAndCheck();
        } catch (Exception e) {
            removeMessage();
        }
    }

    public void itestSend() {//Unable to get Sender/Address
        prepareMessage();
        message.setToRecipients(new ArrayList<Recipient>() {{ add(new Recipient().setAddress(username)); }});
        Me.flush();
        message.send();
        IMessageCollection inbox = Me.getInbox().getMessages().createQuery().
                setFilter("Sender/Address eq '" + username + "'").getResult();
        try {
            IMessage inboxMessage = inbox.iterator().next();
            assertEquals(message.getSubject(), inboxMessage.getSubject());
            Me.getMessages().delete(inboxMessage.getId());
            Me.getMessages().delete(Me.getSentItems().getMessages().iterator().next().getId());
        } catch (NoSuchElementException e) {
            fail("message has not been sent");
        }
    }

    public void testReply() {
        // first send message to self
        final String subject = "reply test" + (int) (Math.random() * 1000000);
        message = (IMessage) Messages.newMessage()
                .setToRecipients(new ArrayList<Recipient>() {{ add(new Recipient().setAddress(username)); }})
                .setSubject(subject);
        message.send(); // flush will be performed automatically before operations that have side effects
        // find message in inbox after a little delay, otherwise service sometimes lags with message processing
        IMessage inboxMessage = null;
        for (int i = 0; i < 20; ++i) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}

            if (Me.getInbox().getMessages().createQuery().setFilter("Subject eq '" + subject + "'").getResult().size() > 0) {
                inboxMessage = Me.getInbox().getMessages().createQuery().setFilter("Subject eq '" + subject + "'").getSingleResult();
                break;
            }
        }

        if (inboxMessage == null) {
            fail("message did not send");
        }

        final String reply = "reply on test message";
        inboxMessage.reply(reply);

        // find reply after a little delay
        IMessageCollection replies = null;
        for (int i = 0; i < 20; ++i) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}

            if (Me.getInbox().getMessages().createQuery().setFilter("Subject eq 'RE: " + subject + "'").getResult().size() > 0) {
                replies = Me.getInbox().getMessages().createQuery().setFilter("Subject eq 'RE: " + subject + "'").getResult();
                break;
            }
        }

        if (replies == null) {
            fail("reply did not send");
        }
        assertEquals(1, replies.size());

        Iterator<IMessage> messages = Me.getSentItems().getMessages().createQuery()
                .setFilter("contains(Subject, '" + subject + "')").getResult().iterator();

        while (messages.hasNext()) {
            Me.getMessages().delete(messages.next().getId());
        }

        messages = Me.getInbox().getMessages().createQuery()
                .setFilter("contains(Subject, '" + subject + "')").getResult().iterator();
        while (messages.hasNext()) {
            Me.getMessages().delete(messages.next().getId());
        }

        Me.flush();
    }

    public void testMoveAndCopy() {
        final String subject = "move and copy test" + (int) (Math.random() * 1000000);
        message = (IMessage) Messages.newMessage(DefaultFolder.ROOT).setSubject(subject);
        IMessage copied = null;
        // move
        message = message.move(Me.getDrafts().getId());
        copied = message.copy(Me.getRootFolder().getId());
        Me.getMessages().delete(message.getId());
        if (copied != null) {
            Me.getMessages().delete(copied.getId());
        }

        Me.flush();
    }

    private void createAndCheck() {
        prepareMessage();
        Me.flush();
        assertTrue(StringUtils.isNotEmpty(message.getId()));
    }

    private void readAndCheck() {
        // reread a message
        message = Me.getMessages().get(message.getId());
        Class<?> cls = message.getClass();
        Class<?>[] emptyParametersArray = new Class<?>[0];
        for (ODataProperty property : sourceMessage.getProperties()) {
            try {
                Method getter = cls.getMethod("get" + property.getName(), emptyParametersArray);
                assertEquals(property.getPrimitiveValue().toValue(), getter.invoke(message));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void updateAndCheck() {
        final String content = "updated body text";
        ItemBody newBody = new ItemBody();
        newBody.setContent(content);
        newBody.setContentType(BodyType.Text);

        message.setBody(newBody);
        Me.flush();

        assertEquals(BodyType.Text, message.getBody().getContentType());
        assertEquals(content, message.getBody().getContent());
        // ensure that changes were pushed to endpoint
        message = Me.getMessages().get(message.getId());
        assertEquals(BodyType.Text, message.getBody().getContentType());
        assertEquals(content, message.getBody().getContent());
    }

    private void deleteAndCheck() {
        removeMessage();
        assertNull(Me.getMessages().get(message.getId()));
    }

    private void prepareMessage() {
        sourceMessage = getEntityFromResource("simpleMessage.json");
        message = Messages.newMessage(DefaultFolder.DRAFTS);
        String subject = sourceMessage.getProperty("Subject").getPrimitiveValue().toString();
        message.setSubject(subject);
    }

    private void removeMessage() {
        Me.getMessages().delete(message.getId());
        Me.flush();
    }

}
