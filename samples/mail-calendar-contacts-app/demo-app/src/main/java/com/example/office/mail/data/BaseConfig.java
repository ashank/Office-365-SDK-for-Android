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
package com.example.office.mail.data;

import java.io.Serializable;

/**
 * Base abstract configuration.
 */
@SuppressWarnings("serial")
public abstract class BaseConfig implements Serializable {

    /**
     * Time, configuration was retrieved from server side.
     */
    protected long mTimeStamp;

    /**
     * Retrieves timestamp when the config was updated last time.
     *
     * @return Timestamp when the config was updated last time.
     */
    public long getTimeStamp() {
        return mTimeStamp;
    }

    /**
     * Whether the config is linked to specific SIM card number.
     *
     * @return True if the config is dependable on SIM card number (should be available with specific SIM), false otherwise.
     */
    public boolean isSimCardDependable() {
        return false;
    }

    /**
     * Retrieves linked SIM card number (this method will be called for config if the config is SIM card dependable only).
     *
     * @return Linked SIM card number.
     */
    public String getLinkedSimNumber() {
        return null;
    }
}
