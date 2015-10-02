# Native libraries


## GDAL for GeoServer, Extractorapp & Mapfishapp

Extractorapp **requires** GDAL and GDAL Java bindings libraries installed on the server.

GeoServer uses them to access more data formats, read http://docs.geoserver.org/latest/en/user/data/raster/gdal.html

Mapfishapp also optionally uses them for the file upload functionality, that allows to upload a vectorial data file to mapfishapp in order to display it as a layer. This functionnality in Mapfishapp relies normally on GeoTools, however, the supported file formats are limited (at 2013-10-17: shp, mif, gml and kml). If GDAL and GDAL Java bindings libraries are installed, the number of supported file formats is increased. This would give access, for example, to extra formats such as GPX and TAB.

The key element for calling the GDAL native library from mapfishapp is the **imageio-ext library** (see https://github.com/geosolutions-it/imageio-ext/wiki). It relies on:
 * jar files,
 * a GDAL Java native binding library, based on the JNI framework, named gdaljni, or ogrjni,
 * and the GDAL library.

### Install GDAL from Debian Jessie packages : 

The latter can be installed, from Debian Jessie distributions, with some libgdal packages:
```
    apt-get install libgdal1h libgdal-java

```

Then you will have to make sure that the webapps access to `gdal.jar` (/usr/share/java/gdal.jar).
To do that, when your app is deployed, copy your `gdal.jar` in the **WEB-INF/lib** folder of the webapp, and delete `imageio-ext-gdal-bindings-*.jar`. Think about include this in your deploy script.

Notice : this method doesn't allow to have multiple apps with gdal in the same tomcat, but actually we can't get shared classloader to work because gdal load the native and imageio also load them, there is a conflict.

And finally edit your `/etc/default/tomcat8` file to have something like :
```
LD_LIBRARY_PATH="/usr/local/lib:${LD_LIBRARY_PATH}"
JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=$LD_LIBRARY_PATH"
```

### Install GDAL from sources

If you do not want to use the precompiled binaries, another way to install the GDAL Java binding is building it from sources.

The advantage to compile GDAL is to allow more formats to be supported (ecw, jp2K, ...), you can choose the ones which suits your needs.

In order to compile we'll need some compiler packages :
```
apt-get install build-essential
```

#### Enable ECW support (optional)

Note : a carefully crafted GeoTiff pyramid is as fast as ECW.

For example, in order to enable the support of ECW format, we need to install it. We will explain how to install the version 2-3.3 of the ECW SDK, but this isn't the newer version and you can choose one more recent if you need but remember there are limits for the uses of this SDK with no licence.

First search `libecwj2-3.3-2006-09-06.zip` on any search engine in order to found the SDK. (no link because it always change)

After you get this zip simply, we'll need to apply some patch to correct the jp2K support and som other bugs and then compile the sources and install it : 
```
unzip libecwj2-3.3-2006-09-06.zip
rm libecwj2-3.3-2006-09-06.zip
git clone https://gist.github.com/2935204d4d8a2a77b4a9.git patches
patch -p0 < patches/libecwj2-3.3-msvc90-fixes.patch
cd libecwj2-3.3/
patch -p0 < ../patches/libecwj2-3.3-NCSPhysicalMemorySize-Linux.patch
patch -p1 < ../patches/libecwj2-3.3-wcharfix.patch
./configure
make
make install
```

#### Compile GDAL

First we need the latest version of GDAL (2.0.1 when written) : https://trac.osgeo.org/gdal/wiki/DownloadSource

Then unzip the sources and get in the extracted folder : 
```
wget http://download.osgeo.org/gdal/2.0.1/gdal201.zip
unzip gdal201.zip
rm gdal201.zip
cd gdal-2.0.1
```

Install the build dependencies : 
```
apt-get build-dep gdal
```

And configure the GDAL compilation : 
```
./configure --with-threads --with-grass=no --with-libtiff=internal --with-geotiff=internal \
--with-java=/usr/lib/jvm/default-java --with-jasper --with-netcdf --with-xerces --with-geos --with-sqlite3 \
--with-curl --with-pg --with-ogdi --with-mysql --with-perl --with-ruby --with-python --with-odbc \
--with-dods-root=/usr --with-static-proj4=yes --with-spatialite=/usr --with-cfitsio=no --with-ecw=/usr/local \
--with-mrsid=no --with-poppler=yes --with-openjpeg=yes --with-freexl=yes --with-libkml=yes --with-armadillo=yes \
--with-liblzma=yes --with-epsilon=/usr;
```

**/!\** Adapt the **--with-java**, **--with-ecw**, and other configs to your own configuration.
After configure is done you have a summary of the format which will be supported after compilation, if the ones you want are set to "No", that's you have a false path in the `./configure` arguments. Search on the web how to set path for differents parts of this string, probably someone asked it before you on some forum.

Then when all formats you want are supported, compile and install GDAL : 
```
make
make install
```

In order to run GDAL after installing it is necessary for the shared library to be findable. This can often be accomplished by setting **LD_LIBRARY_PATH** to include /usr/local/lib in `/etc/default/tomcat8`.

After that, GDAL is installed but not the JAVA bindings, for get them : 
```
cd swig/java
apt-get install ant
```

In the `java.opt` file set **JAVA_HOME** to your Java SDK path and run the compilation : 
```
export JAVA_HOME=<your Java SDK path>
make
make install
```

Then you will have to make sure that the webapps access to `gdal.jar`.
To do that, when your app is deployed, copy your `gdal.jar` in the **WEB-INF/lib** folder of the webapp, and delete `imageio-ext-gdal-bindings-*.jar`. Think about include this in your deploy script.

Notice : this method doesn't allow to have multiple apps with gdal in the same tomcat, but actually we can't get shared classloader to work because gdal load the native and imageio also load them, there is a conflict.

And finally edit your `/etc/default/tomcat8` file to have something like :
```
LD_LIBRARY_PATH="/usr/local/lib:${LD_LIBRARY_PATH}"
JAVA_OPTS="${JAVA_OPTS} -Djava.library.path=$LD_LIBRARY_PATH"
```
