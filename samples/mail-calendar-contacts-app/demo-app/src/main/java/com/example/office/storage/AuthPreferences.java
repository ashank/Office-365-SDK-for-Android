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
package com.example.office.storage;


import com.example.office.OfficeApplication;
import com.example.office.events.CredentialsStoredEvent;
import com.example.office.logger.Logger;
import com.example.office.utils.Utility;
import com.microsoft.office.core.auth.IOfficeCredentials;

/**
 * Implements authentication preferences.
 */
public class AuthPreferences {

    /**
     * The name of the file where the authentication dta is saved.
     */
    private static final String AUTH_PREFERENCES_FILENAME = "com.example.sharepoint.client";

    /**
     * Options preferences file.
     */
    protected static final String OPTIONS_PREFERENCES_FILENAME = "com.example.sharepoint.client.options";

    /**
     * Stores user {@linkplain ISharePointCredentials} into SharedPreferences.
     *
     * @param credentials Authentication credentials.
     */
    public static void storeCredentials(IOfficeCredentials credentials){
        try {
            LocalPersistence.writeObjectToFile(OfficeApplication.getContext(), credentials, AUTH_PREFERENCES_FILENAME);
            new CredentialsStoredEvent(credentials).submit();
        } catch (final Exception e) {
            Logger.logApplicationException(e, AuthPreferences.class.getSimpleName() + ".storeCredentials(): Error.");
            Utility.showAlertDialog(AuthPreferences.class.getSimpleName() + ".storeCredentials(): Failed. " + e.toString(), OfficeApplication.getContext());
        }
    }

    /**
     * Returns user {@linkplain ISharePointCredentials} from SharedPreferences.
     *
     * @return Initialized {@linkplain ISharePointCredentials} instance. In case of exception returns <code>null</code>.
     */
    public static IOfficeCredentials loadCredentials(){
            try{
            return (IOfficeCredentials) LocalPersistence.readObjectFromFile(OfficeApplication.getContext(), AUTH_PREFERENCES_FILENAME);
        } catch (final Exception e) {
            Logger.logApplicationException(e, AuthPreferences.class.getSimpleName() + ".loadCredentials(): Error.");
            Utility.showAlertDialog(AuthPreferences.class.getSimpleName() + ".loadCredentials(): Failed. " + e.toString(), OfficeApplication.getContext());
        }
        return null;
    }

    /**
     * Private constructor to prevent creating new instance of the class.
     */
    private AuthPreferences(){
    }
}
