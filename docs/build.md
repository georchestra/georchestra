# Building geOrchestra

Building geOrchestra is generally **not recommended**, since [we provide binaries](https://packages.georchestra.org/).


## Getting the sources

At this stage, if you don't have the geOrchestra sources, you need to download them:
```
git clone --recurse-submodules https://github.com/georchestra/georchestra.git ~/georchestra
```
By default, this will always fetch the latest stable version.

Go grab some coffee in the mean time, or read on...

## Use a mirror to fetch dependencies

In your `~/.m2/settings.xml`, add a mirror:
```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
 http://maven.apache.org/xsd/settings-1.0.0.xsd">
 <mirrors>
   <mirror>
     <id>artifactory-georchestra</id>
     <mirrorOf>*</mirrorOf>
     <url>https://packages.georchestra.org/artifactory/maven</url>
   </mirror>
 </mirrors>
</settings>
```

## Install the dependencies

You should install the required packages:
```
sudo apt-get install python-virtualenv openjdk-8-jdk ant ant-optional
```

## Build the modules

Building your SDI is just few command-lines away.
```
cd ~/georchestra
```

Build Geonetwork
```
cd geonetwork
mvn -DskipTests clean install
cd ..
```

Build **all modules** (except GeoFence).
```
mvn -Dmaven.test.skip=true clean install
```

In case you only want to build one module or a collection, the syntax is a bit different:
```
mvn -Dmaven.test.skip=true -P-all,module1,module2 clean install
```
... where ```moduleX``` can be one of: ```analytics```, ```cas```, ```extractorapp```, ```geonetwork```, ```geofence```, ```geoserver```, ```geowebcache```, ```header```, ```console```, ```mapfishapp```, ```security-proxy```.

Alternately, if you want to build all projects but one (say ```geowebcache-webapp```):

```bash
mvn -Dmaven.test.skip=true --projects \!geowebcache-webapp clean install
```

As a result of the build process, you should find the geOrchestra artifacts into the subfolders of the ```~/.m2/repository/org/``` directory.
Now, let's [prepare the system](setup.md) to receive the webapps.

Are you having problems with the build ? Please ask on IRC [freenode#georchestra](https://kiwiirc.com/client/irc.freenode.net/georchestra).
Note that if you're connecting to the internet through proxies, you need to [tell maven how to reach public repositories](http://maven.apache.org/guides/mini/guide-proxies.html).


## Advanced build options

These options are not required to build geOrchestra but they can make your life easier.

### GeoServer extensions

To build GeoServer with one or several extensions, one can use the profiles defined in the [geoserver/extension](https://github.com/georchestra/geoserver/blob/2.3.2-georchestra/src/extension/pom.xml) and [geoserver/community](https://github.com/georchestra/geoserver/blob/2.3.2-georchestra/src/community/pom.xml) poms.

Example building geoserver only, with the control-flow, css, csw, gdal, inspire, pyramid and wps extensions:
```
mvn -P-all,geoserver -Pcontrol-flow,css,csw,gdal,inspire,pyramid,wps -Dmaven.test.skip=true clean install
```

### geoserver.war.excludes

As the name suggests, the ```geoserver.war.excludes``` property allows you to exclude files from the final GeoServer build.

Typically, you will have the native JAI installed, because it performs far better than the java version.
As a result, the JAI classes are useless for GeoServer.

Build GeoServer with:
```
mvn -P-all,geoserver '-Dgeoserver.war.excludes=WEB-INF/lib/jai_*.jar' -Dmaven.test.skip=true clean install
```

Another use of the property is when building GeoServer without the integrated GeoWebCache:
```
mvn -P-all,geoserver '-Dgeoserver.war.excludes=WEB-INF/lib/*gwc*.jar' -Dmaven.test.skip=true clean install
```

Both can be combined with:
```
mvn -P-all,geoserver '-Dgeoserver.war.excludes=WEB-INF/lib/*gwc*.jar,WEB-INF/lib/jai_*.jar' -Dmaven.test.skip=true clean install
```
