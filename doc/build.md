How to build ?
==============

First, install the required packages: 

    sudo apt-get install ant ant-optional openjdk-7-jdk python-virtualenv

Notes: 
 * openjdk-6-jdk works too 
 * GeoServer is [known](http://research.geodan.nl/2012/10/openjdk7-vs-oracle-jdk7-with-geoserver/) to perform better with Oracle JDK.

Then clone the repository (either the stable branch or master if you're feeling lucky):

    git clone -b 14.06 --recursive https://github.com/georchestra/georchestra.git

...and build:

    cd georchestra
    ./mvn -Dmaven.test.skip=true -Dserver=myprofile clean install

Are you having problems with the build ?  
Take a look at our [Jenkins CI](https://sdi.georchestra.org/ci/job/georchestra-template/)


