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
package com.example.office.utils;

import java.util.ArrayList;
import java.util.List;

import com.example.office.Constants.UI;
import com.example.office.mail.data.MailItem;

/**
 * Helper class with helpful functions.
 */
public final class Utils {

    /**
     * Private constructor to prevent creating new instance of the class.
     */
    private Utils() {}

    public static final List<MailItem> boxMail(List<MailItem> list, UI.Screen box) {
        List<MailItem> result = new ArrayList<MailItem>();
        for (MailItem boxedItem : list) {
            if (boxedItem.getBox() == box) result.add(boxedItem);
        }
        return result;
    }

}
