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
package com.msopentech.odatajclient.engine.data.json;

import java.io.IOException;

import org.w3c.dom.Node;

import com.fasterxml.jackson.core.JsonGenerator;
import com.msopentech.odatajclient.engine.client.ODataClient;
import com.msopentech.odatajclient.engine.utils.ODataVersion;

/**
 * DOM tree utilities class.
 */
final class DOMTreeUtils {

    private DOMTreeUtils() {
        // Empty private constructor for static utility classes
    }

    

    /**
     * Serializes DOM content as JSON.
     *
     * @param client OData client.
     * @param jgen JSON generator.
     * @param content content.
     * @throws IOException in case of write error.
     */
    public static void writeSubtree(final ODataClient client, final JsonGenerator jgen, final Node content)
            throws IOException {
        if (client.getWorkingVersion() == ODataVersion.V3) {
            DOMTreeUtilsV3.writeSubtree(client, jgen, content, false);
        } else if (client.getWorkingVersion() == ODataVersion.V4) {
            DOMTreeUtilsV4.writeSubtree(client, jgen, content, false);
        }
    }

}
