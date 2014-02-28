1. Build Mail-Contact-Calendar SDK: 
	Execute 
	>> "mvn install" 
	in "../office365-mail-calendar-contact-sdk" directory.
	
2. Build tested application:
	Execute
	>> "mvn install android:deploy"
	in tested application directory ("tested-project").

NOTE: steps 1, and 2 are required only: 
- if those project has been updated since the last execution
- if it is the first time you're running these tests on current machine. 
Otherwise you can SKIP these steps.	

3. Build, deploy and run testing application: 
	Execute
	>> "mvn package android:deploy android:instrument"
	in testing application directory ("testing-project-it").
	
	You can specify "-Dtest=testA" parameter to run single test
	'testA' is name of test method.