Office 365 SDK for Android Preview
==========================

Overview
--------
It’s now possible to use data stored in Microsoft Office 365 from your Android Apps. You can access SharePoint lists, folders or Exchange calendar, contacts and emails from within your Android applications. 

Microsoft Open Technologies, Inc. (MS Open Tech) has built the Office 365 SDK for Android Preview, an open source project that strives to help Android developers access Office 365 data from their apps. 

This SDK provides access to: Microsoft SharePoint Lists, Microsoft SharePoint Files, Microsoft Exchange Calendar, Microsoft Exchange Contacts, Microsoft Exchange Mail.

Best of all, it’s FOSS (Free and Open Source Software) so that you can participate in the development process as we build these libraries. 

Details
-------

The SDK is composed of three independent packages, so that you can import only the SDK that you need in your project.

1.	office365-files-sdk [depends on office365-base-sdk]
2.	office365-lists-sdk  [depends on office365-base-sdk]
3.	office365-mail-calendar-contact-sdk

The SDK is compatible with the following Android versions: 4.0.3 (API15),4.1.2 (API16),4.2.2 (API 17), 4.3 (API18),4.4.2 (API19)

To help you get started quickly, we have created sample applications, including:

•	Asset management app that allows the user to view the items in a particular list of a SharePoint site, add a new item with a picture into this list, update and delete an item from this list. 

•	Mail contact and calendar app that lets the user view all his mails from the drafts folder and send mail, events from all his calendars and all his contacts. 

Additionally we are working on an SDK that covers the discovery API. Until then please look at the files-discovery-app that we have written that uses the discovery API to get the list of files from my-lists on SharePoint. 

Quick Start for SharePoint lists and files
-----------------------------------------

Asset-management-app 

-	Download the following code onto your machine

  o	office365-base-sdk from this repo

  o	office365-lists-sdk from this repo
  
  o	asset-management from this repo
  
-	Import the above code into your favorite IDE. 
-	Add a dependency on the office365-base-sdk from the office365-lists-sdk
-	Add a dependency on the office365-lists-sdk from the asset-management app
-	Subscribe to SharePoint online from http://msdn.microsoft.com/en-us/library/fp179924(v=office.15).aspx or use an existing SharePoint Online site. 
-	The application expects a picture library on the site with Title and Description columns visible in the default view of the library. 
-	Run the application and walk through the screens as shown below. The first screen shot is the front screen of the app. Click on the cog wheel at the top of the app and you will see the second screen that has the list of settings that need to be configured. Examples are below.

  o	SharePoint URL would be like “https://contosomotors.sharepoint.com

  o	Site URL would be like “sites/developers”
  
  o	Library name would be like “ContosomotorsPictureLibrary”
  
-	Please choose the Cookie authentication method under authentication method.
-	That’s all the configuration for the app is done. You can go back to the first screen and click on the box next to the settings to retrieve the items in the picture library, add an item or update the title or description of an existing item.

All the code that calls into the lists SDK is in the /assetmanagement/src/com/microsoft/assetmanagement/datasource/ListItemsDataSource.java class

  o	View the list items – Refer to the getDefaultListViewItems method
  
  o	Add a list item – Refer to saveNewCar method
  
  o	Update a list item – Refer to updateCarData method
  
  o	Delete a list item – Refer to deleteCar method

Note: The app uses cookie based authentication and has been tested on Android versions API 14 and 17 .

Files-discovery-app

-	Download the following code onto your machine from this repo: office365-base-sdk, office365-files-sdk, files-discovery-app.
-	Download the Azure Active directory Android library [AADAL] from the following repo https://github.com/MSOpenTech/azure-activedirectory-library-for-android
-	Import the above code into your favorite IDE. 
-	Add a dependency on the office365-base-sdk from the office365-lists-sdk
-	Add a dependency on the office365-base-sdk from the office365-files-sdk
-	Add a dependency on the office365-files-sdk and AADAL the from the files demo app
-	Subscribe to SharePoint online from http://msdn.microsoft.com/en-us/library/fp179924(v=office.15).aspx or use an existing SharePoint Online site. 
-	Please edit the /file-discovery-app/src/com/microsoft/office365/file-discovery-app/Constants.java file and provide the values for the constants below.

	public static final String CLIENT_ID = "your-client-id"; 
	
	public static final String REDIRECT_URL = "http://your-redirect-url.com";
	

-	Click on the one drive link on your SharePoint site to instantiate it. 
-	Run the application. You will be asked to login with your SP account. Once logged in, the app will retrieve the lists of files from your OneDrive account.  
https://<sharepoint URL>/personal/<account name>/Documents/Forms/All.aspx
-	A breakdown of the code is below. 

Step 1: The app gets authorized by the user by calling the Authorization URL and passing its hardcoded scope.

Step 2: The app gets a token for Discovery by calling the Token URL and passing the code from
OfficeClient officeClient = mApplication.getOfficeClient(DiscoveryFragment.this.getActivity(), Constants.DISCOVERY_RESOURCE_ID).get();

Step 3: The app discovers the services that implement its desired scope by calling the Discovery URL passing the token from step #2.

List<DiscoveryInformation> services = officeClient.getDiscoveryInfo("https://api.officeppe.com/discovery/me/services").get();

Step 4: For each consented capability, Discovery will return a service URL and a service resource ID.

DiscoveryInformation fileService = null;
for (DiscoveryInformation service : services) {
  if (service.getCapability().equals(Constants.MYFILES_CAPABILITY)) {
  fileService = service;
  break;
 }
}

Then for the desired service, the app does 

Step 5: Get a token for the service by calling the Token URL and passing the service resource ID from step #4.

Step 6: Now the app is set to call the service using the service URL and the token from step #5.

String sharepointResourceId = fileService.getServiceResourceId();
String endpointUrl = fileService.getServiceEndpointUri();
String sharepointUrl = endpointUrl.split("_api")[0];
FileClient fileClient = mApplication.getFileClient(DiscoveryFragment.this.getActivity(), sharepointResourceId, sharepointUrl).get();

Quick Start for Exchange mail, calendar and contact apps
--------------------------------------------------------

There are 2 apps in the samples folder that utilize the mail-calendar-contact sdk.
The mail app is a simple app that retrieves emails from the user's drafts folder. The mail-calendar-contact app retrieves emails from the users drafts folder, sends email, retrieves events from the user's calendar and retrieves contacts from the user's contact list.

The set up for both apps is given below.

Download the following code onto your machine: mail-app from this app or the mail-calendar-contact-app, Azure Active directory Android library [AADAL] from the following repo https://github.com/MSOpenTech/azure-activedirectory-library-for-android

Add a dependency on AADAL from the mail-app.

Subscribe to a mail account from http://msdn.microsoft.com/en-us/library/fp179924(v=office.15).aspx or use an existing mail account.

Modify the following in the constants.java file

    public static final String DOMAIN = "Enter the domain for the user name"; // For example if the user name is foo@bar.com, then bar.com is the domain name.
    
    public static final String CLIENT_ID = "Grab this from the Azure management portal after you register your application";
    
    public static final String REDIRECT_URL = "Grab this from the Azure management portal after you register your application";

If you hit a JAR Mismatch issue with the Android-Support-v4.jar, please replace this jar in the AADAL libs folder with the latest one from the Android SDK. 

Run the application. User will be asked to enter his account details and all the mails from the drafts folder are retrieved.

The code that calls the mail-contact-calendar-contacts-sdk from within the 2 apps is below:



Note:

Both the mail apps listed above already include a reference to the mail-calendar-contacts.jar file. 
If you choose to build the SDK, please follow the steps listed in README.txt located in the root folder of the SDK to produce the JAR file.

Execute “mvn clean install” to generate the jar file and set it up into local maven repository. Jar will be generated in “<sdk-root>\core\target\mail-calendar-contact-core-0.11.1.jar”



License
-------

Copyright (c) Microsoft Open Technologies, Inc. All rights reserved. Licensed under the Apache License, Version 2.0 (the "License"); 

	




