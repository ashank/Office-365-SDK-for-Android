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


