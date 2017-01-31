= About =

This webapp aims to generate an Atlas as a PDF document, given a WFS layer and
a base map, presenting one feature on each pages, and provides some basic page
setup options.

It makes use of Camptocamp's mapfishprint-v3 component. It queues the print
requests, generates the PDF file, then send a mail to the user with an URL
letting him finally get his document.


This project has been funded by Rennes-Métropole.

= Compilation =

As the other webapps from geOrchestra, you can build this one using the
following command, considering you already are in the current directory:


```
$ ../mvn clean install
```

= Binary installation =

A docker image is automatically built by the geOrchestra CI and is available here:
https://hub.docker.com/r/georchestra/atlas/

Unfortunately Debian and Yum packages are not yet available in the official
geOrchestra repositories.

= Datadir vs non-datadir =

This webapp is compatible with a datadir-mode, you can check the
`atlas.properties` in you `${georchestra.datadir}/atlas/` directory.

In case of the non-datadir mode, you can modify the `atlas.properties` in
`./src/main/webapp/WEB-INF/classes/` directory before building or use the
config.jar method to override it.

= Configuration =

You will need to configure several configuration options in the previous file:

* The PostGreSQL URL / credentials
* The SMTP server
* Some extra informations required by the email templates
* The temporary directory where to store the generated atlas.

= Client-side addon =

To use this webapp, a Mapfishapp addon has to be activated in the viewer, the
sources can be found here (relative to the current directory):

```
../mapfishapp/src/main/webapp/app/addon/atlas/
```

