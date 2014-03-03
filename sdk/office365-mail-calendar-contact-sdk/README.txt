Prerequisites:
--------------

- Maven v3.1.1 or higher. Download: http://maven.apache.org/download.cgi

Installation:
-------------

 >> mvn clean install

 OR

 >> mvn install

Site/Javadoc generation:
------------------------

 You can generate simple javadoc for each project with:

 >> mvn javadoc:javadoc

 You can generate aggregated javadoc with:

 >> mvn javadoc:aggregate

 You can generate aggregated javadoc jar with:

 >> mvn javadoc:aggregate-jar

 To generate a site you'll first need to generate a usual site and then generate a staging site with valid links to submodules.

 1) >> mvn site:site
 2) >> mvn site:stage

 Valid site will be in "/target/staging" directory.

 To generate cross linked source view use JXR plugin:

 >> mvn jxr:jxr

 Results will be in "/target/site/xref" module directories. Also this will be automatically executed when generating a site.

Code verification:
------------------

 Following command will generate html report with code validation results

 >> mvn pmd:pmd

 Following command will generate html report with code duplication (copy-paste) results

 >> mvn pmd:cpd.

Working in Eclipse:
-------------------

 Eclipse files can be generated with:

 >> mvn eclipse:clean eclipse:eclipse

 As result each Maven module will get a consistent .project, .classpath and .settings file with which each module can be imported as existing project to Eclipse.

Testing:
--------

 >> mvn -Pe2eTests

 Mind that you'll need to install artifacts into your local repository first before running the tests. See [Installation] section.

 Integration tests take ~5 minutes. By default integration tests are skipped.
 You can skip all tests by using -Dmaven.test.skip paramater:

 >> mvn install -Dmaven.test.skip

Upgrading version:
------------------
NOTE: dont's use maven-release-plugin, it fails to do what it should.

 1. Execute (in parent directory):
  >> mvn versions:set -DnewVersion=X.YY.ZZ

 2. Execute (odata subdirectory) if you would like to update version of ODataJClient libs (e.g. due to introduced changes):
  >> mvn versions:set -DnewVersion=VV.RR.WW

 Substitute 'X.YY.ZZ' and 'VV.RR.WW' with appropriate version.

 3.
 	a) If you made a mistake, do
		>> mvn versions:revert
	b) Or confirm results
		>> mvn versions:commit


