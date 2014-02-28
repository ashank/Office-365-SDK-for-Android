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
import com.microsoft.exchange.services.odata.model.types.IFolder;
import com.msopentech.odatajclient.engine.data.ODataEntity;
import com.msopentech.odatajclient.engine.data.ODataProperty;

public class FoldersTestCase extends AbstractTest {

    private ODataEntity sourceFolder;

    private IFolder folder;

    public void testCreate() {
        createAndCheck();
        removeFolder();
    }

    public void testRead() {
        prepareFolder();
        Me.flush();
        readAndCheck();
        removeFolder();
    }

    public void testUpdate() {
        prepareFolder();
        Me.flush();
        updateAndCheck();
        removeFolder();
    }

    public void testDelete() {
        prepareFolder();
        Me.flush();
        deleteAndCheck();
    }

    public void testFoldersCRUD() {
        try {
            createAndCheck();
            readAndCheck();
            updateAndCheck();
            deleteAndCheck();
        } catch (AssertionError e) {
            removeFolder();
            throw e;
        }
    }

    public void testMoveAndCopy() {
        final String name = "move and copy test" + (int) (Math.random() * 1000000);
        folder = Me.getRootFolder().getChildFolders().newFolder();
        folder.setDisplayName(name);
        IFolder copied = null;
        folder = folder.move(Me.getDrafts().getId());
        copied = folder.copy(Me.getRootFolder().getId());
        
        Me.getFolders().delete(folder.getId());
        if (copied != null) {
            Me.getFolders().delete(copied.getId());
        }
        
        Me.flush();
    }

    private void deleteAndCheck() {
        removeFolder();
        assertNull(Me.getFolders().get(folder.getId()));
    }

    private void updateAndCheck() {
        final String newName = "new name";
        folder.setDisplayName(newName);
        Me.flush();
        assertEquals(newName, folder.getDisplayName());
        // ensure that changes were pushed to endpoint
        folder = Me.getFolders().get(folder.getId());
        assertEquals(newName, folder.getDisplayName());
    }

    private void readAndCheck() {
        folder = Me.getFolders().get(folder.getId());
        Class<?> cls = folder.getClass();
        Class<?>[] emptyParametersArray = new Class<?>[0];
        for (ODataProperty property : sourceFolder.getProperties()) {
            try {
                Method getter = cls.getMethod("get" + property.getName(), emptyParametersArray);
                assertEquals(property.getPrimitiveValue().toValue(), getter.invoke(folder));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void removeFolder() {
        Me.getFolders().delete(folder.getId());
        Me.flush();
    }

    private void createAndCheck() {
        prepareFolder();
        Me.flush();
        assertTrue(StringUtils.isNotEmpty(folder.getId()));
    }

    private void prepareFolder() {
        sourceFolder = getEntityFromResource("testFolder.json");
        folder = Me.getRootFolder().getChildFolders().newFolder();
        folder.setDisplayName(sourceFolder.getProperty("DisplayName").getValue().toString());
    }
}
