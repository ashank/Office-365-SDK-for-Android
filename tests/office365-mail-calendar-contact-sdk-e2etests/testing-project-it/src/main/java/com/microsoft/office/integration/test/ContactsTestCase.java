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

import org.apache.commons.lang3.StringUtils;

import com.microsoft.exchange.services.odata.model.Me;
import com.microsoft.exchange.services.odata.model.types.IContact;
import com.msopentech.odatajclient.engine.data.ODataEntity;
import com.msopentech.odatajclient.engine.data.ODataProperty;

public class ContactsTestCase extends AbstractTest {

    private IContact contact;

    private ODataEntity sourceContact;

    public void testCreate() {
        createAndCheck();
        // clean-up
        removeContact();
    }

    public void testRead() {
        // create contact first
        prepareContact();
        Me.flush();

        readAndCheck();
        // clean-up
        removeContact();
    }

    public void testUpdate() {
        // create contact first
        prepareContact();
        Me.flush();
        updateAndCheck();
        // clean up
        removeContact();
    }

    public void testDelete() {
        // create contact first
        prepareContact();
        Me.flush();
        // then remove
        deleteAndCheck();
    }

    public void testContactCRUD() {
        try {
            // CREATE
            createAndCheck();

            // READ
            readAndCheck();

            // UPDATE
            updateAndCheck();

            // DELETE
            deleteAndCheck();
        } catch (AssertionError e) {
            removeContact();
            throw e;
        }
    }

    public void itestSettersChain() {//Server does not return required fields
        prepareContact();
        final String displayName = "test name";
        final String email = "test@example.com";
        IContact returned = contact.setDisplayName(displayName).setEmailAddress1(email);
        assertSame(contact, returned);
        Me.flush();
        try {
            assertEquals(displayName, contact.getDisplayName());
            assertEquals(email, contact.getEmailAddress1());
        } finally {
            removeContact();
        }
    }

    private void createAndCheck() {
        prepareContact();
        Me.flush();
        assertTrue(StringUtils.isNotEmpty(contact.getId()));
    }

    private void readAndCheck() {
        // reread a contact
        contact = Me.getContacts().get(contact.getId());
        Class<?> cls = contact.getClass();
        Class<?>[] emptyParametersArray = new Class<?>[0];
        for (ODataProperty property : sourceContact.getProperties()) {
            try {
                Method getter = cls.getMethod("get" + property.getName(), emptyParametersArray);
                assertEquals(property.getPrimitiveValue().toValue(), getter.invoke(contact));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void updateAndCheck() {
        final String newName = "new name";
        contact.setGivenName(newName);
        Me.flush();
        assertEquals(newName, contact.getGivenName());
        // ensure that changes were pushed to endpoint
        contact = Me.getContacts().get(contact.getId());
        assertEquals(newName, contact.getGivenName());
    }

    private void deleteAndCheck() {
        removeContact();
        assertNull(Me.getContacts().get(contact.getId()));
    }

    private void prepareContact() {
        sourceContact = getEntityFromResource("testContact.json");
        contact = Me.getContacts().newContact();
        contact.setGivenName((String)sourceContact.getProperty("GivenName").getPrimitiveValue().toValue());
    }

    private void removeContact() {
        Me.getContacts().delete(contact.getId());
        Me.flush();
    }

}
