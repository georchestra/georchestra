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
git clone git@gitlab.com:USER/myprofile.git
```

Now, you should have your own configuration in ```~/georchestra/config/configurations/myprofile```.  
Let's see if it builds:
```
cd ~/georchestra/config
../mvn -Dserver=myprofile install
```

SUCCESS ? Good, you're ready for the next step.  
If not, you should review carefully the error messages and [ask for help](http://www.georchestra.org/community.html) if you don't understand what happens.


## Build the modules

Building your SDI is just a command-line away:
```
cd ~/georchestra
./mvn -Dmaven.test.skip=true -Dserver=myprofile clean install
```

Note: this will build **all modules**.  
In case you only want to build one module or a collection, the syntax is a bit different:
```
./mvn -Dmaven.test.skip=true -Dserver=myprofile -P-all,module1,module2 clean install
```
... where ```moduleX``` can be one of: ```analytics```, ```cas```, ```catalogapp```, ```downloadform```, ```extractorapp```, ```geonetwork```, ```geofence```, ```geoserver```, ```geowebcache```, ```header```, ```ldapadmin```, ```mapfishapp```, ```proxy```.

As a result of the build process, your artifacts should be copied into the subfolders of the ```~/.m2/repository/org/georchestra/``` directory.

Are you having problems with the build ?  
Please have a look at our [continuous integration](https://sdi.georchestra.org/ci/job/georchestra-template/). If it builds and yours doesn't, the error is probably on your side.
