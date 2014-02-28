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
package com.microsoft.office.core.net;

/**
 * Authentication credentials required to respond to a authentication
 * challenge are invalid
 */
public class NetworkException extends RuntimeException {

    private static final long serialVersionUID = 319558534317118022L;

    /**
     * Creates a new NetworkException with a <tt>null</tt> detail message.
     */
    public NetworkException() {
        super();
    }

    /**
     * Creates a new NetworkException with the specified message.
     *
     * @param message the exception detail message
     */
    public NetworkException(final String message) {
        super(message);
    }

    /**
     * Creates a new NetworkException with the specified detail message and cause.
     *
     * @param message the exception detail message
     * @param cause the <tt>Throwable</tt> that caused this exception, or <tt>null</tt>
     * if the cause is unavailable, unknown, or not a <tt>Throwable</tt>
     */
    public NetworkException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
