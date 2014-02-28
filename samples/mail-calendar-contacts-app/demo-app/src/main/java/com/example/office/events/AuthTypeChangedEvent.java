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
package com.example.office.events;

import com.example.office.auth.AuthType;

/**
 * Credentials changed event.
 */
public class AuthTypeChangedEvent extends AbstractEvent {

	private final AuthType type;

	/**
	 * Creates an instance.
	 *
	 * @param type New type.
	 */
	public AuthTypeChangedEvent(AuthType type) {
		this.type = type;
	}

	/**
	 * Gets new auth type.
	 *
	 * @return	{@link AuthType} instance.
	 */
	public AuthType getAuthType() {
		return type;
	}
}
