# Building and installing GeoServer support for JPEG2000 grid coverages with Kakadu 7.10

## Build Kakadu libraries

We're going to build the Kakadu 7.10 native libraries from sources, contained in a file called `v7_A_6-01900E.zip`.

The kakadu libraries need to be built with a libc version compatible (equal or older) than the one where the geoserver runs, otherwise will get an error message like the following when starting up geoserver:
```
geoserver_1                 | WARNING: Failed to load the Kakadu native libs. This is not a problem unless you need to use the Kakadu plugin: it won't be enabled.
java.lang.UnsatisfiedLinkError: /mnt/geoserver_native_libs/libkdu_jni.so: /lib/x86_64-linux-gnu/libm.so.6: version
`GLIBC_2.27' not found (required by /mnt/geoserver_native_libs/libkdu_jni.so)
```

For this reason we'll use the `openjdk:11-jdk` docker image to build the native kakadu libraries.

Let's create a directory on the local machine with the uncompressed source files and mount it to a running container:

```bash
$ sudo mkdir -m 0777 /opt/kakadu_build && cd /opt/kakadu_build
$ unzip ~/Downloads/kakadu/v7_A_6-01900E.zip -d sources
$ docker run -it -h kdu -v /opt/kakadu_build/sources:/opt/kakadu openjdk:8-jdk
root@kdu:/# cd /opt/kakadu/v7_A_6-01900E/
```
Now install the necessary build tools:
```bash
root@kdu:/opt/kakadu/v7_A_6-01900E# apt-get update
root@kdu:/opt/kakadu/v7_A_6-01900E# apt-get install -y g++ make
```

And actually build kakadu:
```
root@kdu:/opt/kakadu/v7_A_6-01900E# cd make
root@kdu:/opt/kakadu/v7_A_6-01900E/make# make -f Makefile-Linux-x86-64-gcc
root@kdu:/opt/kakadu/v7_A_6-01900E/make# cd ..
root@kdu:/opt/kakadu/v7_A_6-01900E/# ls -l ./lib/Linux-x86-64-gcc
total 13860
-rw-r--r-- 1 root root 2071092 Apr 18 16:30 libkdu.a
-rwxr-xr-x 1 root root 3402168 Apr 18 16:31 libkdu_a7AR.so
-rw-r--r-- 1 root root 2595024 Apr 18 16:32 libkdu_aux.a
-rwxr-xr-x 1 root root 4626840 Apr 18 16:32 libkdu_jni.so
-rwxr-xr-x 1 root root 1488736 Apr 18 16:30 libkdu_v7AR.so
root@kdu:/opt/kakadu/v7_A_6-01900E/# ldd lib/Linux-x86-64-gcc/libkdu_jni.so
        linux-vdso.so.1 (0x00007fff89110000)
        libpthread.so.0 => /lib/x86_64-linux-gnu/libpthread.so.0 (0x00007f04b46bb000)
        libstdc++.so.6 => /usr/lib/x86_64-linux-gnu/libstdc++.so.6 (0x00007f04b4339000)
        libm.so.6 => /lib/x86_64-linux-gnu/libm.so.6 (0x00007f04b4035000)
        libgcc_s.so.1 => /lib/x86_64-linux-gnu/libgcc_s.so.1 (0x00007f04b3e1e000)
        libc.so.6 => /lib/x86_64-linux-gnu/libc.so.6 (0x00007f04b3a7f000)
        /lib64/ld-linux-x86-64.so.2 (0x00007f04b4ec7000)
```

That got us the native libraries, now build the generated java JNI wrapper library:
```bash
root@kdu:/opt/kakadu/v7_A_6-01900E# cd ../java
root@kdu:/opt/kakadu/java# ls -F
kdu_jni/
root@kdu:/opt/kakadu/java# cd kdu_jni && javac *.java && cd ..
root@kdu:/opt/kakadu/java# jar cvf kdu_jni.jar kdu_jni/
root@kdu:/opt/kakadu/java# ls -lF
total 104
drwxr-xr-x 2 root root   4096 Apr 18 16:31 kdu_jni/
-rw-r--r-- 1 root root 232263 Apr 18 16:35 kdu_jni.jar
root@kdu:/opt/kakadu/java# exit
exit
groldan@lilith:/opt/kakadu_build/sources$
```

Finally, save the built libs for posterity somewhere, I keep them in `/opt/kakadu/lib`:

```bash
groldan@lilith:/opt/kakadu_build/sources$ sudo mkdir -p /opt/kakadu/lib
groldan@lilith:/opt/kakadu_build/sources$ sudo cp java/kdu_jni.jar ./v7_A_6-01900E/lib/Linux-x86-64-gcc/libkdu_jni.so ./v7_A_6-01900E/lib/Linux-x86-64-gcc/libkdu_v7AR.so ./v7_A_6-01900E/lib/Linux-x86-64-gcc/libkdu_a7AR.so /opt/kakadu/lib/
groldan@lilith:/opt/kakadu_build/sources$
```

## Install on geOrchestra's GeoServer

Now that both the native libraries and the JNI wrapper jar file are built, we need to make them available to GeoServer.

```
$ cd ~/git/georchestra/docker
$ docker-compose up -d
$ docker cp /opt/kakadu/lib/libkdu_a7AR.so docker_geoserver_1:/mnt/geoserver_native_libs/
$ docker cp /opt/kakadu/lib/libkdu_v7AR.so docker_geoserver_1:/mnt/geoserver_native_libs/
$ docker cp /opt/kakadu/lib/libkdu_jni.so docker_geoserver_1:/mnt/geoserver_native_libs/
$ docker-compose exec geoserver ls -l /mnt/geoserver_native_libs
total 9300
-rwxr-xr-x 1 root root 3402168 Apr 18 17:06 libkdu_a7AR.so
-rwxr-xr-x 1 root root 4626840 Apr 18 17:06 libkdu_jni.so
-rwxr-xr-x 1 root root 1488736 Apr 18 17:06 libkdu_v7AR.so
$ docker-compose restart geoserver
```

## Test with sample data

If you have GDAL installed it's easy to create a sample Jpeg2000 sample coverage:

```bash
$ wget https://eoimages.gsfc.nasa.gov/images/imagerecords/57000/57752/land_shallow_topo_21600.tif
$ gdal_translate -of JP2OpenJPEG -a_srs WGS84 -a_ullr -180 90 180 -90 land_shallow_topo_21600.tif land_shallow_topo_21600.jp2
Input file size is 21600, 10800
0...10...20...30...40...50...60...70...80...90...100 - done.
$
$ ls -lh land_shallow_topo_21600.*
-rw-rw-r-- 1 groldan groldan  73M abr 18 15:35 land_shallow_topo_21600.jp2
-rw-rw-r-- 1 groldan groldan 174M oct  5  2011 land_shallow_topo_21600.tif
$
$ gdalinfo land_shallow_topo_21600.jp2
Driver: JP2OpenJPEG/JPEG-2000 driver based on OpenJPEG library
Files: land_shallow_topo_21600.jp2
Size is 21600, 10800
Coordinate System is:
GEOGCS["WGS 84",
    DATUM["WGS_1984",
        SPHEROID["WGS 84",6378137,298.257223563,
            AUTHORITY["EPSG","7030"]],
        AUTHORITY["EPSG","6326"]],
    PRIMEM["Greenwich",0],
    UNIT["degree",0.0174532925199433],
    AUTHORITY["EPSG","4326"]]
Origin = (-180.000000000000000,90.000000000000000)
Pixel Size = (0.016666666666667,-0.016666666666667)
Metadata:
  TIFFTAG_RESOLUTIONUNIT=3 (pixels/cm)
  TIFFTAG_XRESOLUTION=28.3462
  TIFFTAG_YRESOLUTION=28.3462
Image Structure Metadata:
  INTERLEAVE=PIXEL
Corner Coordinates:
Upper Left  (-180.0000000,  90.0000000) (180d 0' 0.00"W, 90d 0' 0.00"N)
Lower Left  (-180.0000000, -90.0000000) (180d 0' 0.00"W, 90d 0' 0.00"S)
Upper Right ( 180.0000000,  90.0000000) (180d 0' 0.00"E, 90d 0' 0.00"N)
Lower Right ( 180.0000000, -90.0000000) (180d 0' 0.00"E, 90d 0' 0.00"S)
Center      (   0.0000000,   0.0000000) (  0d 0' 0.01"E,  0d 0' 0.01"N)
Band 1 Block=1024x1024 Type=Byte, ColorInterp=Red
  Overviews: 10800x5400, 5400x2700, 2700x1350
  Overviews: arbitrary
  Image Structure Metadata:
    COMPRESSION=JPEG2000
Band 2 Block=1024x1024 Type=Byte, ColorInterp=Green
  Overviews: 10800x5400, 5400x2700, 2700x1350
  Overviews: arbitrary
  Image Structure Metadata:
    COMPRESSION=JPEG2000
Band 3 Block=1024x1024 Type=Byte, ColorInterp=Blue
  Overviews: 10800x5400, 5400x2700, 2700x1350
  Overviews: arbitrary
  Image Structure Metadata:
    COMPRESSION=JPEG2000
```

And copy the file to the `geoserver_geodata` volume:

```bash
$ docker cp land_shallow_topo_21600.jp2 docker_geoserver_1:/mnt/geoserver_geodata/
```

To set up a layer out of this coverage:

* Browse to geoserver's admin UI at https://georchestra.mydomain.org/geoserver
* Select `Stores` -> `Add new Store` -> `JP2K (Direct)`
* Fill "Data source name" with `land_shallow_topo_21600` and "Connection parameters/URL" with `file:/mnt/geoserver_geodata/land_shallow_topo_21600.jp2`
* Hit the "Save" button, the "New Layer" page will show up listing the `land_shallow_topo_21600` coverage
* Hit the "publish" link besides the layer name, scroll down to the bottom of the "Edit layer" form page and hit **Save**
* Finally check the layer is working through the WMS: https://georchestra.mydomain.org/geoserver/wms/reflect?layers=land_shallow_topo_21600&format=application/openlayers
