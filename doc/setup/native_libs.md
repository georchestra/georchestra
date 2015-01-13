# Native libraries


## GDAL for GeoServer, Extractorapp & Mapfishapp

Extractorapp **requires** GDAL and GDAL Java bindings libraries installed on the server.

GeoServer uses them to access more data formats, read http://docs.geoserver.org/latest/en/user/data/raster/gdal.html

Mapfishapp also optionally uses them for the file upload functionality, that allows to upload a vectorial data file to mapfishapp in order to display it as a layer. This functionnality in Mapfishapp relies normally on GeoTools, however, the supported file formats are limited (at 2013-10-17: shp, mif, gml and kml). If GDAL and GDAL Java bindings libraries are installed, the number of supported file formats is increased. This would give access, for example, to extra formats such as GPX and TAB.

The key element for calling the GDAL native library from mapfishapp is the **imageio-ext library** (see https://github.com/geosolutions-it/imageio-ext/wiki). It relies on:
 * jar files,
 * a GDAL Java native binding library, based on the JNI framework, named gdaljni, or ogrjni,
 * and the GDAL library.

The latter can be installed, on Debian-based distributions, with the libgdal1 package:

    sudo apt-get install libgdal1

Some more work is needed for installing the GDAL Java binding library, as there is still no deb package for it (note that packages exist for ruby and perl bindings, hopefully the Java's one will be released soon - see a recent proposal http://ftp-master.debian.org/new/gdal_1.10.0-0%7Eexp3.html).

To quickly install the GDAL Java binding library on the server, download and extract the library and its data. To do so, you can use 2 repositories providing binary packages for given distribution:

 * GeoSolutions http://demo.geo-solutions.it/share/github/imageio-ext/releases/1.1.X/1.1.7/native/gdal/ which provides a package with ECW support (which can cause some licensing issues, and some of the packages are outdated, since the distributions evolved and updated their Glibc version, see #409) ;
 * geOrchestra-provided packages http://sdi.georchestra.org/~pmauduit/gdalogr-java-bindings/ which provides "vanilla" GDAL packages as well as a specific one (mifmid-patched) which allows the use of MIF/MID format across GDAL/OGR via GeoTools (see #409)


Example for Debian Wheezy on amd64:

    sudo mkdir -p /var/sig/gdal/NativeLibs/
    sudo wget http://demo.geo-solutions.it/share/github/imageio-ext/releases/1.1.X/1.1.7/native/gdal/linux/gdal192-Ubuntu12-gcc4.6.3-x86_64.tar.gz -O /var/sig/gdal/NativeLibs/gdal_libs.tgz
    cd /var/sig/gdal/NativeLibs/ && sudo tar xvzf gdal_libs.tgz
    
    sudo wget http://demo.geo-solutions.it/share/github/imageio-ext/releases/1.1.X/1.1.7/native/gdal/gdal-data.zip -O /var/sig/gdal/data.zip
    cd /var/sig/gdal/ && sudo unzip data.zip

Next, you have to:
 - include the newly created directory /var/sig/gdal/NativeLibs/ in the `LD_LIBRARY_PATH` environment variable
 - create a GDAL_DATA environment variable (eg: export GDAL_DATA="/var/sig/gdal/data")

```
sudo nano /etc/default/tomcat6
```

```
LD_LIBRARY_PATH=/usr/lib/jni:/var/sig/gdal/NativeLibs/:$LD_LIBRARY_PATH
```
Then you will have to make sure that the Tomcat will share the `gdal.jar` across the different webapps ; you can do this by creating a file in your `${catalina.base}/conf` directory, named `catalina.properties`, containing:

```
common.loader=${catalina.base}/lib,${catalina.base}/lib/*.jar,${catalina.home}/lib,${catalina.home}/lib/*.jar
server.loader=
shared.loader=${catalina.base}/lib/*.jar
```

Then, ensure that the installed `gdal.jar` is reachable by the classloader by copying it or creating a symlink to it into `${catalina.base}/lib`. See https://groups.google.com/forum/#!topic/georchestra-dev/K7GK_cLeAyk for more informations. The main motivation by doing so is to avoid that the different webapp contexts have to load the same native libraries more than once. THis is the reason why we have to share the classes ensuring the native bindings role.


If you do not want to use the precompiled binaries, another way to install the GDAL Java binding is building it from sources. See http://trac.osgeo.org/gdal/wiki/GdalOgrInJavaBuildInstructionsUnix.
