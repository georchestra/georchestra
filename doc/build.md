# Building geOrchestra

Here are the steps...

## Get the sources

At this stage, if you don't have the geOrchestra sources, you need to download them:
```
git clone --recursive https://github.com/georchestra/georchestra.git ~/georchestra
```
(go grab some coffee in the mean time, or read on)


## Install the dependencies

You should install the required packages: 
```
sudo apt-get install python-virtualenv openjdk-7-jdk ant ant-optional
```

Notes: 
 * openjdk-6-jdk works too 
 * GeoServer is [known](http://research.geodan.nl/2012/10/openjdk7-vs-oracle-jdk7-with-geoserver/) to perform better with Oracle JDK.


## Install your configuration

By default, cloning the geOrchestra repository will also fetch the template configuration in ```~/georchestra/config/configurations/template```.
Since you want to build against your configuration rather than the template one, you'll have to copy it into the ```~/georchestra/config/configurations/myprofile``` directory.

Let's do this with git instead:
```
cd ~/georchestra/config/configurations/
git clone https://gitlab.com/user/myprofile.git
```

Now, you should have your own configuration in ```~/georchestra/config/configurations/myprofile```.  
Let's see if it builds:
```
cd ~/georchestra/config
../mvn -Dserver=myprofile install
```

SUCCESS ? Good, you're ready for the next step.  
If not, you should review carefully the error messages and [ask for help](http://www.georchestra.org/community.html) if you don't understand what happens.

Note that if you're connecting to the internet through proxies, you need to [tell maven how to reach public repositories](http://maven.apache.org/guides/mini/guide-proxies.html).

## Build the modules

Building your SDI is just a command-line away:
```
cd ~/georchestra
export MAVEN_OPTS="-XX:MaxPermSize=256M"
./mvn -Dmaven.test.skip=true -Dserver=myprofile clean install
```

Note: this will build **all modules** (except GeoFence).
In case you only want to build one module or a collection, the syntax is a bit different:
```
./mvn -Dmaven.test.skip=true -Dserver=myprofile -P-all,module1,module2 clean install
```
... where ```moduleX``` can be one of: ```analytics```, ```cas```, ```catalogapp```, ```downloadform```, ```extractorapp```, ```geonetwork```, ```geofence```, ```geoserver```, ```geowebcache```, ```header```, ```ldapadmin```, ```mapfishapp```, ```security-proxy```.

As a result of the build process, you should find the geOrchestra artifacts into the subfolders of the ```~/.m2/repository/org/``` directory.
Now, let's [prepare the system](setup.md) to receive the webapps.

Are you having problems with the build ?  
Please have a look at our [continuous integration](https://sdi.georchestra.org/ci/job/georchestra-template/). If it builds and yours doesn't, the error is probably on your side.



## Advanced build options

These options are not required to build geOrchestra but they can make your life easier.

### sub.target

With the same config directory, it is possible to manage several environments (typically a production and a test server).
This is achieved through the use of the ```sub.target``` property in the maven command line.

Example:
```
./mvn -Dserver=myprofile -Dsub.target=prod -Dmaven.test.skip=true clean install
```

Depending on the ```sub.target``` value, it is possible to alter one or several config options (typically: shared maven filters, but not exclusively). 
The magic happens in your profile's ```build_support/GenerateConfig.groovy``` script, eg with:

```groovy
    def generate(def project, def log, def ant, def basedirFile,
      def target, def subTarget, def targetDir,
      def buildSupportDir, def outputDir) {

        // method added here:
        updateSharedMavenFilters(subTarget)
        
        updateGeoServerProperties()
        updateGeoFenceProperties()
        updateMapfishappMavenFilters()
        updateExtractorappMavenFilters()
        updateSecProxyMavenFilters()
        updateLDAPadminMavenFilters()
    } 

    /**
     * updateSharedMavenFilters
     */
    def updateSharedMavenFilters(subTarget) {
        switch (subTarget) {
            case "test":
                new PropertyUpdate(
                    to: 'shared.maven.filters').update { properties ->
                        properties['shared.server.name'] = "test.georchestra.org"
                        properties['shared.default.log.level'] = "DEBUG"
                        properties['shared.instance.name'] = "geOrchestra demo - TEST"
                }
                break
            case "prod":
                new PropertyUpdate(
                    to: 'shared.maven.filters').update { properties ->
                        properties['shared.server.name'] = "sdi.georchestra.org"
                        properties['shared.default.log.level'] = "WARN"
                }
                break
        }
    }
```

### GeoServer extensions

To build GeoServer with one or several extensions, one can use the profiles defined in the [geoserver/extension](https://github.com/georchestra/geoserver/blob/2.3.2-georchestra/src/extension/pom.xml) and [geoserver/community](https://github.com/georchestra/geoserver/blob/2.3.2-georchestra/src/community/pom.xml) poms.

Example building geoserver only, with the control-flow, css, csw, gdal, inspire, pyramid and wps extensions:
```
./mvn -P-all,geoserver -Pcontrol-flow,css,csw,gdal,inspire,pyramid,wps -Dserver=myprofile -Dmaven.test.skip=true clean install 
```

### geoserver.war.excludes

As the name suggests, the ```geoserver.war.excludes``` property allows you to exclude files from the final GeoServer build.

Typically, you will have the native JAI installed, because it performs far better than the java version. 
As a result, the JAI classes are useless for GeoServer.

Build GeoServer with:
```
./mvn -P-all,geoserver '-Dgeoserver.war.excludes=WEB-INF/lib/jai_*.jar' -Dserver=myprofile -Dmaven.test.skip=true clean install
```

Another use of the property is when building GeoServer without the integrated GeoWebCache:
```
./mvn -P-all,geoserver '-Dgeoserver.war.excludes=WEB-INF/lib/*gwc*.jar' -Dserver=myprofile -Dmaven.test.skip=true clean install
```

Both can be combined with:
```
./mvn -P-all,geoserver '-Dgeoserver.war.excludes=WEB-INF/lib/*gwc*.jar,WEB-INF/lib/jai_*.jar' -Dserver=myprofile -Dmaven.test.skip=true clean install
```
