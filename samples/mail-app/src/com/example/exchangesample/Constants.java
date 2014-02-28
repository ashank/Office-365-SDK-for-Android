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
package com.example.exchangesample;

/**
 * Stores application public constants such as URLs to update configurations, default check back-in values, etc.
 */
public class Constants {

    /**
     * Domain.
     */
    public static final String DOMAIN = "Enter your domain here";
    
   /**
     * Url for Oauth2 authorization page.
     */
    public static final String AUTHORITY_URL = "https://login.windows-ppe.net/" + DOMAIN;
    

    /**
     * Application unique ID for Oauth2 authorization.
     */
    public static final String CLIENT_ID = "a7558c9a-c964-4fbf-be19-2f277f78a586";

    /**
     * Resource id for authorization and where need get access.
     */
    public static final String RESOURCE_ID = "https://outlook.office365.com/";
    /**
     * Url application will be redirected after authentication.
     */
    public static final String REDIRECT_URL = "http://msopentech.com";

}
