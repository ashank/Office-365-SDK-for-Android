The test app uses Cookies authentication. You shouldn't have any issues accessing any sharepoint site located at .sharepoint.com 
 
In order to run the test you will need to create (or have an existing) list and the same goes for a picture library. This is mandatory since we don't have a way of creating picture libraries or lists.
 
In your sharepoint I've created
 
A picture library named : "TestPicLib"
A list named : "TestList" 
 
Depending which set of test you want to run (files or tests) you will need to update the test lib name under settings.
 
<TestApp1.PNG> 
 
<TestApp2.PNG> 
 
Once you have everything configured, simple select wich set of tests one to run (depending the name of the listname you have in the configuration), select the tests and run them. 100% green.
 
I've run both set of test here: https://msopentechandroidtest.sharepoint.com/ without problems
If by any chance you're using the test app targeting another sharepoint server, please, erase the cookies first. The authentication scheme is the same as the asset-manament app.
 
So, to recap, these are the settings used:
Sharepoint url : https://msopentechandroidtest.sharepoint.com 
Site : /
Listname : TestDocLib (for files) and TestList (for lists)
 
When you run the test for first time, it will ask for your credentials.
