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

> The scripting language used for in the GenerateConfig.groovy is the [Groovy](http://groovy.codehaus.org/) programming language.  It is based on the Java language and most java syntax will work in Groovy as well.  But Groovy is dynamic and has several conveniences that make it better for scripting than java.  It is pretty easy to use Google to find information about Groovy but hopefully the examples provided in this document and the Javadocs will provide a good introduction to the most common tasks needed.

'''Note:''' Javadocs are generated when the config module is built and can be viewed in config\target\site\apidocs\index.html

### Create new file
This example shows one way of creating file objects and writing to the file

    // assign file system path separator to variable
    def S = File.separator
    // create a new file. Use Groovy's string interpolation to put the correct path separator in path
    def outputFile = new File(outputDir, "geotwork-main${S}webapp${S}WEB-INF${S}newFile")
    // write some text to the file
    outputFile << "text of new file"

### Copy a file
This example shows one way of creating file objects and copying the contents of one file to the other file

    // assign file system path separator to variable
    def S = File.separator
    // use replace to change / to correct platform separator and use as keyword to change string to a file
    def outputFile = outputDir+"/geonetwork-main/webapp/WEB-INF/newFile".replace('/',S) as File.class
    def inputFile = basedirFile+"/../geonetwork-main/webapp/WEB-INF/log4j.cfg".replace('/',S) as File.class
    // copy one file to the other
    outputFile << inputFile.getBytes()

### Set class fields during construction

A common pattern used in the support classes of config is a syntax in Groovy where class fields can be set during construction.  Consider the following groovy class:

    class GroovyClass {
      def field1
      def field2
    }

(Note: def field1 is the same as Object field1)

There are no constructors defined but one can create an instance and set the properties in a single declaration:

    new GroovyClass( field1: 'field1', field2: 'field2')

or if one only wants to assign a single field:

    new GroovyClass(field1: 'field')

### Collections in Groovy

Collection objects in Groovy have special syntax to make them easier to work with:

Maps:

    // create map
    def map = ['key1': 'value1', 'key2': 'value2']
    // update map
    map['key1'] = 'newValue'
    // maps are java.util.Map objects so those methods apply:
    map.remove('key1')

List:

  // create a list. Result Type is java.util.List
  def list = ['value1', 'value2']
  // short hand to add new value
  list << 'newValue'
  // normal java.util.List method to add many
  list.addAll( ['nv1', 'nv2'] )
  // Access an element in list
  list[2]

For more on collections in groovy see: http://groovy.codehaus.org/Collections

### AbstractUpdate

Many of the support classes extend AbstractUpdate since it provides several ways of specifying the input and output of an update process.  For clarity, when I update I always mean load an input file, modify it and save it to the target/generated directory.  In no cases should the original file be modified.

The primary responsibility of AbstractUpdate is to provide convenience methods: getFromFile and getToFile for the subclasses based on the parameters.

    new PropertyUpdate (
        projectPath: 'geonetwork', // projectPath indicates the file is in the <georchestra-root>/geonetwork directory, not a config subdirectory
        path: 'webapp/WEB-INF/spring.xml'. // path is used to determing both to and from.  
        to: 'geonetwork-main', // the base of the to file (relative to target/generated).  The path will be appended to the to field.
        from: 'geonetwork-main/src/main' // the final from file is projectPath/from/path
    ).update { properties -> /* update properties */}

In many cases only 'to' and 'from' are required and even some subclasses of AbstractUpdate (like PropertyUpdate) only requires the 'to' field.  Although it is usually beneficial to define the 'path' field so that it doesn't need to be repeated in both 'from' and 'to' fields.

### PropertyUpdate
This example shows how to update (or create) a properties file using the PropertyUpdate support class.

 1. The maven.filter file is loaded into memory
 2. 4 properties are updated or added to the properties object
 3. the properties are written to target/generated/security-proxy/maven.filter

Example Code:

    new PropertyUpdate(
        path: 'maven.filter',
        from: 'defaults/security-proxy', 
        to: 'security-proxy').update { properties ->
            properties['shared.server.name'] = host
            properties['shared.default.log.level'] = logLevel
            properties['application_home'] = applicationHome
            properties['shared.ldapUrl'] = ldapUrl
    }

### XmlUpdate
This first example shows how to generate an xml file based on an existing xml file.  

 1. The file is loaded into memory
 2. All category elements are found
 3. findAll is used to find the category elements with the class attribute that contains the gn string
 4. geor is added to each class attribute in the elements found in the previous step
 5. the updated xml is written to target/generated/security-proxy/file.xml 

Example Code:

    new XmlUpdate(
      path: 'file.xml',
      from: 'defaults/security-proxy', 
      to: 'security-proxy').update { xml ->
          xml.category.findAll {it.@class.contains("gn")}. each {cat ->
              cat.@class = s.@class + " geor" // add new class to element
          }
    }

See http://groovy.codehaus.org/Reading+XML+using+Groovy%27s+XmlParser for more details on how to update the xml

This second example shows how to create a new xml file.  

 1. An XmlBuilder is created
 2. The builder is passed to the closure
 3. The closure constructs the xml:
  1. A config element is created (with no attributes)
  2. An import element is created as child of config.  The import element has a file attribute and no children
  3. A bean element is created as a child of config.  This element has children
  4. Etc...
 4. The xml is written to target/generated/security-proxy/file.xml

Example Code:

    new XmlUpdate(
      path: 'file.xml',
      to: 'security-proxy').write { builder ->
          builder.config() {
            import(file: 'importFile.xml')
            bean (id:'newbean', class: 'org.georchestra.Bean') {
              property (key: 'property', value: 'value')
            }
          }
    }

See http://groovy.codehaus.org/Creating+XML+using+Groovy%27s+MarkupBuilder for more details on how to construct xml documents with the Groovy MarkupBuilder.

### TextUpdate

The text update class assists in updating raw text file by searching for occurances of regular expressions and replacing the matched section with the new text.  This example also illustrates how one can take the text from a georchestra module (in this case Geonetwork) and update that text.

 1. Load <root>/geonetwork/web-client/src/main/resources/apps/georchestra/js/Settings.js into memory
   * Note: the from path is constructed from: <fromProject>/<from>/<path>
 2. The pattern GeoNetwork\.Util\.defaultLocale\s*=\s*'eng' is replaced with "GeoNetwork.Util.defaultLocale = 'fre'"
   * Note: List Javascript the /.../ indicates a regular expression.
   * Note: Currently all matches of the regular expression are replaced
 3. The text is written out to target/generated/geonetwork-client/apps/georchestra/js/Settings.js

Example Code:

    new TextUpdate(
      path:  'apps/georchestra/js/Settings.js',
      fromProject: "geonetwork",
      from: 'web-client/src/main/resources/',
      to: 'geonetwork-client/',
      patternsToReplace: [ /GeoNetwork\.Util\.defaultLocale\s*=\s*'eng'/: "GeoNetwork.Util.defaultLocale = 'fre'"]
    ).update()

### MavenDownloader

The maven downloader support class searches the repositories declared in the root pom.xml and the config pom.xml to locate Maven artifacts and download them.

The following example downloads a single jar to target/generated/geoserver-webapp/WEB-INF/lib.
  
    new MavenDownloader(
      artifact: ['com.vividsolutions','jts','1.13],
      to: 'geoserver-webapp/WEB-INF/lib').download()

One can also download several jars with one declaration by using the 'artifacts' field instead of the 'artifact' field.

    new MavenDownloader(
      artifacts: [
        ['org.geoserver.extension','control-flow','2.2.4'],
        ['com.vividsolutions','jts','1.13]
      ],
      to: 'geoserver-webapp/WEB-INF/lib').download()

### FileSet

A FileSet represents a set of files.  It can be the files in a directory, a single file or all descendants of a directory.
If the file set contains several files a sort and a filter can be applied to the files

<b>Note:</b> sorting the files requires loading all the files into memory and sorting.  This is both slower and 
requires more memory.

<b>Note:</b> Sorting only applies to a single source.  Not to all files in the file set.
 
Examples:
 
    // Represents all js files that are descendants of
    // $basedirFile/src/main/resources/georchestra/js
    // all directories are recursively visited
    new FileSet().descendants(
      source:"$basedirFile/src/main/resources/georchestra/js", 
      filter:{ it.name.endsWith("*.js") }
 	)
 
    // Represents a single file
    new FileSet().file("App.js")

    // Represents the js files directly (not recursively) in the 
    // "web-client/src/main/resources/app/search/js" of the geonetwork project
    // files are sorted by lastmodified date
    new FileSet(project: "geonetwork").children(
      source: "web-client/src/main/resources/app/search/js",
      filter: {it.name.endsWith("*js")},
      sort: {o1, o2 -> o1.lastModified() - o2.lastModified}
    )

    // A fileset with first App.js then all js files in geonetwork directory
    new FileSet().
      file("App.js").
      children(
        source:"geonetwork", 
        filter: {it.name.endsWith(".js)}
      )

The each method can be used to iterate through all the files and perform an action on each file in the FileSet

### Minify

The Minify class is a useful class for minifying either Javascript or CSS files into a single file.

Example:

	new Minify(
			sources: [
				new FileSet().descendants(
					source:"$basedirFile/src/main/resources/georchestra/js", 
					filter:{ it.name.endsWith("*.js") }
				)
			],
			output: "$targetDir/classes/apps/georchestra/js/Minified.js")
	}
	  
### Execute an ant task

Groovy provides a class called the [AntBuilder](http://groovy.codehaus.org/Using+Ant+from+Groovy).  An instance is passed to the GenerateConfig class.  The following example copies the config/configurations/<target>/build_support/geonetwork-main directory to /target/generated

    class GenerateConfig {
      def generate(def project, def log, def ant, def basedirFile, 
							def target, def subTarget, def targetDir, 
							def buildSupportDir, def outputDir) {
        ant.copy(todir: outputDir+"/geonetwork-main") {
          fileset (dir: buildSupportDir+"/geonetwork-main")
        }
      }
    }
    
### Structured Scripting

If the GenerateConfig script is complex it would likely be a good idea to structure the script in several classes and have GenerateConfig call those classes to do the work.  One can even use packages like in java if one needs to.  Although that is probably more than is typically needed.

A common pattern used could be the following:

GeoserverConfig.groovy:

    class GeoserverConfig {
      def generate(Parameters params) {
        // generate geoserver configuration file
      }
    }

GeonetworkConfig.groovy:

    class GeonetworkConfig {
      def generate(Parameters params) {
        // generate geonetwork configuration file
      }
    }
    
GenerateConfig.groovy

    class GenerateConfig {
      def generate(def project, def log, def ant, def basedirFile, 
    					def target, def subTarget, def targetDir, 
    					def buildSupportDir, def outputDir) {
        def params = Parameters.get
        new GeoserverConfig().generate(params)
        new GeonetworkConfig().generate(params)
      }
    }

Property vs Profile
===================

When building the configuration module there are two Java system properties that are observed.  

 * server - this property defines the directory in config/configurations to use as the configuration
 * sub.target - this property is optional and is used if the same configuration is used for multiple target servers like test, integration, production.  This property is really only used by GenerateConfig.groovy scripts
 
One can specify them manually on the commandline:

    mvn install -Dserver=template -Dsub.target=test
    
Or one can add a profile to <root>/pom.xml that declares the properties when the profile is enabled. There are examples in the pom already that be be used as templates.  The following example enables a profile:
  
    mvn install -Ptemplate
    
See (http://maven.apache.org/guides/introduction/introduction-to-profiles.html) for more on maven profiles.

Post Treatment Script
=====================

Consider minification of javascript files in Geonetwork.  In geonetwork minification is done by Yui and the definitions are in the 
in the pom.xml.  As a result a configuration cannot add files to be minified because maven will not recognize the changes.  To overcome this limitation
the Georchestra build system has will run a PostTreatment script if it is defined for that project.

To declare a Post Treatment script, create a PostTreatment.groovy file in the project's configuration directory.  

For example, to define a Post Treatment script for geonetwork-client in a project "template". 
Create the file: config/configurations/template/geonetwork-client/PostTreatment.groovy.  This file should have the class:

    class PostTreatment {
	    def run(def project, def log, def ant, def basedirFile, def configDir,
						def target, def subTarget, def targetDir) {
				...
			}
	}

The file can also be generated and written to: conf/target/generated/geonetwork-client.

These scripts will have access to the same classes the GenerateConfig scripts do.  

*Note*: Not all projects support post treatment scripts.  Check the pom.xml for the project and check:

 * The gmaven plugin has been added to the project as follows:
	<plugin>
		<groupId>org.codehaus.groovy.maven</groupId>
		<artifactId>gmaven-plugin</artifactId>
		<dependencies>
			<dependency>
					<groupId>${project.groupId}</groupId>
					<artifactId>config</artifactId>
					<version>${project.version}</version>
			</dependency>
		</dependencies>
	</plugin>
 * The property _postTreatmentScript_ does not override the property defined in the root pom.xml.  (Defining this property is a way to disable the post treatment script for projects that need the gmaven plugin but don't need the post treatment script execution)
 
Since one of the more common tasks will be to add a minification step the following example illustrates how to do this.

	class PostTreatment {
		def run(def project, def log, def ant, def basedirFile, def configDir,
					def target, def subTarget, def targetDir) {
			new Minify(
				sources: [
					new FileSet().descendants(
						source:"$basedirFile/src/main/resources/georchestra/js", 
						filter:{ it.name.endsWith("*.js") }
					)
				],
				output: "$targetDir/classes/apps/georchestra/js/Minified.js")
		}
	}
