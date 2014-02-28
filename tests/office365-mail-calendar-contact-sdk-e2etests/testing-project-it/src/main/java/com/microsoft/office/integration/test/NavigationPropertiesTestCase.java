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

import com.microsoft.exchange.services.odata.model.Messages;

public class NavigationPropertiesTestCase extends AbstractTest {

    public void testCreateEntityAndAccessNavigationPropertyFailure() {
        Exception exc = null;
        try {
            Messages.newMessage().getAttachments();
        } catch (Exception e) {
            exc = e;
        }

        assertNotNull(exc);
    }
}
