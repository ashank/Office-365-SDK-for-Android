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
package com.microsoft.office.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActionMapKey {

    private final String functionName;
    private final String bindingParameterTypeName;
    private final Boolean isBindingParameterCollection;
    private final List<String> parameterNames;

    public ActionMapKey(final String functionName, final String bindingParameterTypeName, final Boolean isBindingParameterCollection,
            final List<String> parameterNames) {
        this.functionName = functionName;
        if (bindingParameterTypeName != null && isBindingParameterCollection == null) {
            throw new IllegalArgumentException("Indicator that the bindingparameter is a collection must not be null if its an bound function.");
        }
        this.bindingParameterTypeName = bindingParameterTypeName;
        this.isBindingParameterCollection = isBindingParameterCollection;
        this.parameterNames = new ArrayList<String>();
        if (parameterNames != null) {
            this.parameterNames.addAll(parameterNames);
            Collections.sort(this.parameterNames);
        }
    }

    @Override
    public int hashCode() {
        String hash = functionName;

        if (bindingParameterTypeName != null) {
            hash = hash + bindingParameterTypeName;
        } else {
            hash = hash + "typeNull";
        }

        if (isBindingParameterCollection != null) {
            hash = hash + isBindingParameterCollection.toString();
        } else {
            hash = hash + "collectionNull";
        }

        if (!parameterNames.isEmpty()) {
            for (String name : parameterNames) {
                hash = hash + name;
            }
        } else {
            hash = hash + "parameterNamesEmpty";
        }

        return hash.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || !(obj instanceof ActionMapKey)) {
            return false;
        }
        final ActionMapKey other = (ActionMapKey) obj;

        if (functionName.equals(other.functionName)) {
            if ((bindingParameterTypeName == null && other.bindingParameterTypeName == null)
                    || (bindingParameterTypeName != null && bindingParameterTypeName.equals(other.bindingParameterTypeName))) {
                if ((isBindingParameterCollection == null && other.isBindingParameterCollection == null)
                        || (isBindingParameterCollection != null && isBindingParameterCollection.equals(other.isBindingParameterCollection))) {
                    if (parameterNames == null && other.parameterNames == null) {
                        return true;
                    } else if (parameterNames.size() == other.parameterNames.size()) {
                        for (String name : parameterNames) {
                            if (!other.parameterNames.contains(name)) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getBindingParameterTypeName() {
        return bindingParameterTypeName;
    }

    public Boolean isBindingParameterCollection() {
        return isBindingParameterCollection;
    }

    public List<String> getParameterNames() {
        return parameterNames;
    }
}
