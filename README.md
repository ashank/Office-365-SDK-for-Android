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

•	Mail contact and calendar app that lets the user view all his mails from the drafts folder, events from all his calendars and all his contacts. 

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

-	Note: The app uses cookie based authentication.



