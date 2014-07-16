About the GDAL/OGR native libraries
===========================================

# Introduction

A lot of features in geOrchestra depend on the use of native libraries - GDAL/OGR and JAI. When JAI can be used as a fallback with a pure java implementation, it is by nature impossible for GDAL/OGR. The main problem is that when several webapps try to use the library via GeoTools, the native library can be only loaded once by the current Java environment (to put it simplier: one by tomcat instance). As a result, only the first webapp which initializes the library gains access to GDAL/OGR native code.

To fix this problem, the main idea is to use what is called the "shared classloader" from tomcat. But it is actually not so simple: for convenience, the Geotools and ImageIO-ext projects provided their own version of the gdal.jar as a dependency, even if this file should be used against the corresponding native `gdal*.so/gdal*.dll`.

To sum it up:

* the GeoTools & ImageIO-ext projects are pulling a dependency which can be incompatible with the installed GDAL version, or which requires the administrator to install compiled binaries without letting him compiling by himself (provided by GeoSolutions) ;
* The library corresponding to the gdal.jar binding contains some extra softwares which can cause some licensing issues (ECW SDK) ;
* Since the multiplication of Java `System.loadLibrary()` calls, even with a supposedly "good" setup, only the first initialized webapp can gain access to the native GDAL libraries

The complete fix chosen by the geOrchestra community has been to:

1. Exclude the problematic dependency from the webapps (mapfishapp and extractorapp, it has been assumed that geoserver has always been in its own tomcat)
2. Use either a distribution-provided package or recompile gdal from sources (see below)
3. Configure tomcat hosting mapfishapp and extractorapp


# Using distribution packages

It is important to use a gdal.jar binding which comes along with the underlying GDAL/OGR library (i.e. compiled with the same sources), but Geotools and ImageIO-ext provide their own version of the gdal.jar as a dependency. After having several troubles with classloading and webapp isolation, it has been decided in geOrchestra to blacklist the bindings when building the webapps, but this introduced some extra work needed to have a correctly configured tomcat.

Before this change, the solution was to isolate each webapp into their own tomcat, but 

Some Linux distributions, as well as Windows precompiled GDAL binaries, do provide the GDAL JAVA bindings. Next debian version should include the package `libgdal-java`, but since it is not available yet, Camptocamp provided a one into their own debian repository. You can add the following line to your `sources.list`:

```
deb http://pkg.camptocamp.net/apt wheezy/staging sig-georchestra
```

This will provide the `libgdal-java` package, which contains the following two files:

```
/usr/lib/jni/libgdaljni.so
/usr/lib/jni/libogrjni.so
/usr/share/java/gdal.jar
```

The advantage of using a package is that the /usr/lib/jni is in the default library path, as a result, it does not require extra definition into the tomcat configuration (no `JAVA_LIBRARY_PATH` in the `setenv.sh` script), so that the Java VM is able to find them (basically while calling `System.loadLibrary("gdaljni")`).


# Building GDAL from sources

In order to be able to produce the same kind of binaries provided by GeoSolutions for enabling extra format support (via GDAL/OGR native libraries), it might necessary to recompile gdal from scratch. In order to imitate the target server, and to avoid polluting my development environment, it could be easier to work into a vagrant box. I personally have an Ubuntu 13.04, where the target server was a debian 7.

Once the virtual machine has been set up, you have to fetch the latest version of GDAL.


Prepare the source environment as follows:
```
wget http://download.osgeo.org/gdal/1.10.1/gdal-1.10.1.tar.gz
tar xvzf gdal-1.10.1.tar.gz
cd gdal-1.10.1
```

Then fetch the necessary dependencies, it can be done as follows, under debian-based distributions:
```
apt-get build-dep gdal
```
Next, I decided to imitate what was done in term of configuration for the official debian package, so I used the following configure options (to check, you can get the official gdal source package, then study the debian/rules content):

```
--prefix=/usr \
                        --mandir=\$$\{prefix\}/share/man \
                        --includedir=\$$\{prefix\}/include/gdal \
                        --with-threads \
                        --with-grass=no \
                        --with-libtiff=internal \
                        --with-geotiff=internal \
                        --with-jasper \
                        --with-netcdf \
                        --with-xerces \
                        --with-geos \
                        --with-sqlite3 \
                        --with-curl \
                        --with-pg \
                        --with-ogdi \
                        --with-mysql \
                        --with-perl \
                        --with-ruby \
                        --with-python \
                        --with-odbc \
                        --with-dods-root=/usr \
                        --with-static-proj4=yes \
                        --with-spatialite=/usr \
                        --with-cfitsio=no \
                        --with-ecw=no \
                        --with-mrsid=no \
                        --with-poppler=yes \
                        --with-openjpeg=yes \
                        --with-freexl=yes \
                        --with-libkml=yes \
                        --with-armadillo=yes \
                        --with-liblzma=yes \
                        --with-epsilon=/usr;

```

As you can see, the official debian package misses the Java bindings option. Let's launch the ./configure command-line program, adding the `--with-java` option, and a customized prefix to avoid tainting our virtual environment:

```
./configure --prefix=/opt/gdal-georchestra \
                        --mandir=\$$\{prefix\}/share/man \
                        --includedir=\$$\{prefix\}/include/gdal \
                        --with-threads \
                        --with-grass=no \
                        --with-libtiff=internal \
                        --with-geotiff=internal \
                        --with-java \
                        --with-jasper \
                        --with-netcdf \
                        --with-xerces \
                        --with-geos \
                        --with-sqlite3 \
                        --with-curl \
                        --with-pg \
                        --with-ogdi \
                        --with-mysql \
                        --with-perl \
                        --with-ruby \
                        --with-python \
                        --with-odbc \
                        --with-dods-root=/usr \
                        --with-static-proj4=yes \
                        --with-spatialite=/usr \
                        --with-cfitsio=no \
                        --with-ecw=no \
                        --with-mrsid=no \
                        --with-poppler=yes \
                        --with-openjpeg=yes \
                        --with-freexl=yes \
                        --with-libkml=yes \
                        --with-armadillo=yes \
                        --with-liblzma=yes \
                        --with-epsilon=/usr;

```
The generated makefile could not potentially lack of some necessary compilation flags. Make sure that the compilation process (running `make`) does include `-fPIC` flags. This can be done scanning the file GDALmake.opt, around line 65 / 66:

```
CFLAGS          = -g -O2 -DHAVE_SSE_AT_COMPILE_TIME  -Wall -Wdeclaration-after-statement $(USER_DEFS) -fPIC
CXXFLAGS        = -g -O2 -DHAVE_SSE_AT_COMPILE_TIME  -Wall $(USER_DEFS) -fPIC

```

Then, launch the compilation process:

```
make && sudo make install
```

This should produce a /opt/gdal-georchestra/ directory with several subdir (bin,lib,share).

## GDAL Java bindings

The bindings are autogenerated by a software called swig. you will have to enter the `swig/java` subdirectory of GDAL sources:

```
cd swig/java
```

Check the file `java.opt` to ensure that the directory defined for your JDK is correctly defined (I used openjdk-6 to ensure the generated bytecode should be able to run with later versions of Java):

```
JAVA_HOME = /usr/lib/jvm/java-6-openjdk-amd64/
```


Check also that `ant` (a Java build system) is currently installed:

```
sudo apt-get install openjdk-6-jdk ant
```

Then, launch the compilation:

```
...gdal-1.10.1/swig/java$ make
```

This should generate a gdal.jar, as well as a .lib subdirectory with the underlying native libraries ; I copied them by hand into my previoulsy created `/opt/gdal-georchestra/`:

```
cp gdal.jar .libs/gdal-ogr-bindings.jar
mkdir /opt/gdal-georchestra/java
cp ./libs/* /opt/gdal-georchestra/java/
```

## Tomcat configuration

### The shared classloader

The default configuration of the shared classloader differs if you are using a tomcat from a debian/distro package or from an original tarball ; you need to check the value of the property `shared.loader` defined in the `conf/catalina.properties` file from your tomcat installation path, and ensure it is defined so that it would be able to find the `gdal.jar` file.

### Native libraries

If you are using distro packages for GDAL, this should not be necessary to modify the `LD_LIBRARY_PATH` / `java.library.path` environment variable, since `/usr/lib/jni` should already be considered as a library path.

The webapps requiring GDAL/OGR bindings (GeoServer, but potentially mapfishapp) will be placed under a Tomcat server which will need to be configured in order to resolve our specific version of GDAL. The two key parameters of the JVM that requires configuration are`java.library.path` and `classpath`. I chose to configure my tomcat using a setenv.sh-like script accordingly as follows:

```
export GDAL_DATA="/opt/gdal-georchestra/share/gdal"
export PATH=/opt/gdal-georchestra/bin:$PATH
export LD_LIBRARY_PATH=/lib:/opt/gdal-georchestra/lib:/opt/gdal-georchestra/java:$LD_LIBRARY_PAT

CATALINA_OPTS="[...]
 -DGDAL_DATA=$GDAL_DATA \
 -Djava.library.path=$LD_LIBRARY_PATH \
  [...]
"

```

# Last manual checks

As mentionned above, some webapps could still include the `gdal-ogr-bindings.jar` package even with the blacklisting made at a compilation level. These can be outdated and/or not compatible with our version of GDAL. If you have some files into your `webapps/*/WEB-INF/lib` named `imageio-ext-gdal-bindings-1.9.2.jar` or simply `gdal-x.y.z.jar`, they should be redundant with our jar file and should be excluded for further builds. To put it simply, each duplicated jars will try to load the same native library, and will fail if the library has already been loaded.

The proper way to package and provide GDAL native support would be at a tomcat-level, not webapp-level. We should consider building the geOrchestra webapps without this jar, and install the `gdal-ogr-bindings.jar` (`gdal.jar` in the zip file) into the `lib/` subdirectory of considered tomcats.
