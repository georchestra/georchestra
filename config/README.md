Config
======

Configuration in Georchestra is designed to reduce the amount of work required to maintain a full configuration for all modules as much as possible at the same time allowing for complete customisation.

The rough concept is that a configuration jar is created containing sub directories corresponding to the maven modules in Georchestra.  The files in the configuration will overwrite the files in the module (or add to module if file does not exist).

However, there are several problems with this solution.  For example if a file is large and only a couple lines need to be changed it makes more sense to only update those lines.  The second problem is that many projects have shared configuration parameters like database urls.

There are two parts that are designed to overcome these problems:

 * The **shared.maven.filters** properties file in the root config directory is a file called *shared.maven.filters*.  This file contains the parameters that are shared between many projects.  The file contains the default values and when the configuration jar is being built; all text files with @propertyName@ will be replaced with the property in *shared.maven.filters* based on *propertyName*.  
 * To provide further flexibility configurations can have a script in the *build_support* directory called *GenerateConfig.groovy*.  [Groovy](http://groovy.codehaus.org/) is a scripting language based on Java syntax, but has several useful features to make writing scripts easier.

Configuration Build Process
===========================
Suppose the maven command:

mvn install -Dserver=myproj -Dsub.target=test

 1. Build config module 
  1. Execute the configuration/myproj/build_support/GenerateConfig.groovy script
    * **Note:** the value test (value of sub.target property) is passed to GenerateConfig.groovy as the subTarget parameter
  2. copy files from [default, configuration/<config>/ and target/generate] to target/classes
    * All text files are processed and all @propertyName@ tags are replaced by properties loaded from (top has priority):
      * target/generated/shared.maven.filters
      * configuration/<config>/build_support/shared.maven.filters
      * config/shared.maven.filters
    * Note: build_support directory is not copied
  3. The files in target/classes are bundles up as a jar and installed in the local repository
 2. Build the other modules in Georchestra
   1. in the maven prepare-resources phase that unpacks the config jar into the modules target directory
   2. in the maven copy-resources phase the files in src/main/filtered-resources and target/conf/<module name> are copied and processed using the filters in target/conf/<module name>/maven.filters
   3. Normal maven processes continue

Module Components
=================

- config
 - shared.maven.filters
 - defaults - contains configuration settings and default branding
  - DeployScript.groovy - the default deploy script
  - each sub-directory is a name of one of the Georchestra modules.  The purpose of each sub-directory is to override files in the actual project module refer to.  For example the file security-proxy/WEB-INF/classes/log4j.properties in the defaults folder will overwrite the WEB-INF/classes/log4j.properties file in the security proxy war if it exists. If the file does not exist the file will be added to the war.
 - configuration  - contains all the configurations that can be built by configuration module
  - <config> - directory containing all files that differ from the defaults for a particular target platform.  the name of the directory matches the server java property. (mvn -Dserver=config for example)
    - build_support - special directory that is *NOT* copied to the config
      - GenerateConfig.groovy - Script for creating/copying configuration files.
      - shared.maven.filters - Properties referenced by the main shared.maven.filters or properties that will override the main share.maven.filter properties
 - src - contains [Groovy](http://groovy.codehaus.org/) files for helping implement *GenerateConfig.groovy* scripts (see below)

shared.maven.filters
====================

The *shared.maven.filters* properties file in the root config directory is a file called *shared.maven.filters*.  This file contains the parameters that are shared between many projects.  The file contains the default values and when the configuration jar is being built; all text files with @propertyName@ will be replaced with the property in *shared.maven.filters* based on *propertyName*.

In addition to the *shared.maven.filters* each configuration has to have a *shared.maven.filters* in its build_support directory as well.  At minimum these files must have:

 * shared.privileged.geoserver.user
 * shared.privileged.geoserver
 * shared.ldap.admin.password
 * shared.server.name
 * application_home

It may also override any properties in the default *shared.maven.filters* and add new properties specific to this configuration

A final *shared.maven.filters* can be generated by the *GenerateConfig.groovy* file to the target/generated directory.  This properties in this file take precedence over both other files.

GenerateConfig.groovy
=====================

Each config can contain a GenerateConfig.groovy file which can generate config files into the target/generated directory.  The files in target/generated take precedence over all other configuration files.

This script has two purposes:

 1. The purpose of this script is to allow maximum reuse of the default configuration files
 2. Allow a single configuration directory to be used for test, integration and production servers.  

The way that these scripts can satisfy these two purposes is by reading the base configuration file (be it in defaults, configuration or the basic project module) modifying it and saving it to the target/generated directory.

Consider a couple of simple examples.  

Example 1 - Single Configuration multiple target servers:
---------------------------------------------------------

Suppose the test server of a project had one public url and the production server had another.  One might put the public url in the configuration's *shared.maven.filters* file and the GenerateConfig.groovy file will check the subTarget parameter and when the parameter is 'test' (or whatever value the developer chooses) the script will create a new *shared.maven.filters* in the target/generated (passed to script as the outputDir parameter).  Since the *shared.maven.filters* in the target/generated directory has highest precedence it effective overrides the production value with the test server value.

    class GenerateConfig {
      def generate(def project, def log, def ant, def basedirFile, 
							def target, def subTarget, def targetDir, 
							def buildSupportDir, def outputDir) {
        new File(outputDir, 'shared.maven.filters') << "shared.server.name=integration.host.com"
      }
    }

Example 2 - Change single property in a default maven.filters file:
-------------------------------------------------------------------

Suppose for a particular application the extraction files should all start with the *proj-extract-* prefix instead of the *extraction-* prefix which is in the defaults/extractorapp/maven.filters folder.  The maven.filters files don't have an override mechanism like shared.maven.filters thus the entire maven.filters must be in the config even if only 1 value is modified.  This is bad for a maintenance point of view because if the defaults has a new value added of one of the defaults changes the new application will not get that change.  It is better to only make the single change.  

In this case instead of copying the entire maven.filters file one could use the PropertyUpdater class to copy the default maven.filters file updating only the single property. Thanks to the PropertyUpdater this process is simple.

    class GenerateConfig {
      def generate(def project, def log, def ant, def basedirFile, 
							def target, def subTarget, def targetDir, 
							def buildSupportDir, def outputDir) {
        new PropertyUpdate(
          path: 'extractorapp/maven.filters',
          from: 'defaults/',
          to: outputDir).update { originalProperties ->
            properties['extractionFolderPrefix'] = 'proj-extract-'
        }
      }
    }


Example 3 - Change value in an xml file part of extractorapp module:
--------------------------------------------------------------------

Suppose that the urlrewrite.xml file in the extractorapp module needs to be modified for the project.  The obvious way would be to copy the file to the configuration/project/extractorapp/WEB-INF directory and modify it.  This has the same problem as Example 2, it overwrites the original completely.  Another way would be to use scripting to modify the file and write the updated file to target/generated/extractorapp/WEB-INF.

    class GenerateConfig {
      def generate(def project, def log, def ant, def basedirFile, 
							def target, def subTarget, def targetDir, 
							def buildSupportDir, def outputDir) {
        new XmlUpdate(
          path: 'WEB-INF/urlrewrite.xml',
          fromProject: 'extractorapp',
          from: 'src/main/webapp/WEB-INF',
          to: outputDir+'/extractorapp').update { originalXml ->
            originalXml.
        }
      }
    }

Script Writing Resources
------------------------

> The scripting language used for in the GenerateConfig.groovy is the [Groovy](http://groovy.codehaus.org/) programming language.  It is 


### Create new file

### PropertyUpdater

### XmlUpdater

### TextUpdater

### MavenDownloader


Property vs Profile
===================

