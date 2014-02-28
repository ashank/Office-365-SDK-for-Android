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

import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.microsoft.exchange.services.odata.model.Me;
import com.microsoft.exchange.services.odata.model.types.ICalendar;
import com.msopentech.odatajclient.engine.data.ODataEntity;

public class CalendarsTestCase extends AbstractTest {

    private ODataEntity sourceCalendar;
    private ICalendar calendar;

    @Test
    @Ignore(value = "Calendar can not be created on server for now")
    public void createTest() {
        try {
            createAndCheck();
        } finally {
            removeCalendar();
        }
    }

    @Test
    @Ignore(value = "Calendar can not be created on server for now")
    public void readTest() {
        // create first
        prepareCalendar();
        Me.flush();
        try {
            readAndCheck();
        } finally {
            removeCalendar();
        }
    }

    @Test
    @Ignore(value = "Calendar can not be created on server for now")
    public void updateTest() {
        // create first
        prepareCalendar();
        Me.flush();

        try {
            updateAndCheck();
        } finally {
            removeCalendar();
        }
    }

    @Test
    @Ignore(value = "Calendar can not be created on server for now")
    public void deleteTest() {
        // create first
        prepareCalendar();
        Me.flush();
        // then remove
        deleteAndCheck();
    }

    @Test
    @Ignore(value = "Calendar can not be created on server for now")
    public void calendarsTest() {
        // calendar recreated after each of test so we can not use them
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
            removeCalendar();
        }
    }

    @Test
    public void sizeTest() {
        Me.getCalendars().size();
    }

    private void createAndCheck() {
        prepareCalendar();
        Me.flush();
        assertTrue(StringUtils.isNotEmpty(calendar.getId()));
    }

    private void readAndCheck() {
        // reread calendar
        calendar = Me.getCalendars().get(calendar.getId());
        assertEquals(sourceCalendar.getProperty("Name").getPrimitiveValue().toString(), calendar.getName());
    }

    private void updateAndCheck() {
        final String newName = "new name";
        calendar.setName(newName);
        Me.flush();
        assertEquals(newName, calendar.getName());
        // ensure that changes were pushed to endpoint
        calendar = Me.getCalendars().get(calendar.getId());
        assertEquals(newName, calendar.getName());
    }

    private void deleteAndCheck() {
        removeCalendar();
        assertNull(Me.getCalendars().get(calendar.getId()));
    }

    private void prepareCalendar() {
        sourceCalendar = getEntityFromResource("testCalendar.json");
        calendar = Me.getCalendars().newCalendar();

        calendar.setName((String) sourceCalendar.getProperty("Name").getPrimitiveValue().toValue());
    }

    private void removeCalendar() {
        Me.getCalendars().delete(calendar.getId());
        Me.flush();
    }
}
