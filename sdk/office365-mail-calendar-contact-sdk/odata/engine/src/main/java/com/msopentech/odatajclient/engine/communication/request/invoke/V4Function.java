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
package com.msopentech.odatajclient.engine.communication.request.invoke;

import com.msopentech.odatajclient.engine.data.metadata.edm.v4.ReturnType;

public class V4Function implements AbstractOperation {

    private ReturnType returnType;
    
    private boolean isBound;
    
    private String uri;
    
    public V4Function(ReturnType type, String targetURI) {
        returnType = type;
        uri = targetURI;
    }
    
    public String getReturnType() {
        return returnType.getType();
    }
    
    public String getURI() {
        return uri;
    }
    
    @Override
    public boolean isSideEffecting() {
        return false;
    }
}
