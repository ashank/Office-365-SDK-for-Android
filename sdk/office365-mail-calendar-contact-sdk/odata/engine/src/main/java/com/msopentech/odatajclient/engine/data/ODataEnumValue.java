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
package com.msopentech.odatajclient.engine.data;

public class ODataEnumValue extends ODataValue {

    private static final long serialVersionUID = 3351577648102478468L;

    /**
     * Contained value.
     */
    private Enum<?> value;

    /**
     * Type name.
     */
    private final String typeName;

    /**
     * Constructor.
     *
     * @param value Value.
     */
    public ODataEnumValue(Enum<?> value, String typeName) {
        this.value = value;
        this.typeName = typeName;
    }

    /**
     * Gets value.
     *
     * @return Value.
     */
    public Enum<?> getValue() {
        return value;
    }

    /**
     * Casts enum value.
     *
     * @param <T> cast.
     * @return casted value.
     */
    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T toCastValue() {
        return (T) value.getClass().cast(getValue());
    }

    /**
     * Gets type name.
     *
     * @return type name.
     */
    public String getTypeName() {
        return typeName;
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

}
