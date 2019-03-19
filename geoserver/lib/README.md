This directory is used to contain jars
that could not otherwise be added to 
geoserver/WEB-INF/lib, or that need
to replace geoserver dependencies that
cannot be fetched from a repository (e.g.,
a library with fixes that didn't make it
to the geoserver release in use).

It should contain a directory for the
actual geoserver release number being used,
and the libraries that are to be added
to geoserver/WEB-INF/lib on it.

For example:
2.12.5/
	gt-s3-geotiff-18.5.jar
	something-else.jar

Will override gt-s3-geotiff.5.jar and add something-else.jar
when gs.version == 2.12.5 in webapp/pom.xml

