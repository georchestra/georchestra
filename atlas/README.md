# Atlas

This webapp aims to generate an Atlas as a PDF document, given a WFS layer and
a base map, presenting one feature on each page, and provides some basic page
setup options.

It makes use of Camptocamp's `mapfishprint-v3` component. It queues the print
requests, generates the PDF file, then sends an email to the user with an URL
letting him finally get his document.


This project was funded by [Rennes-Métropole](https://github.com/sigrennesmetropole/).

## Compilation

As the other webapps from geOrchestra, you can build this one using the
following command, considering you already are in the current directory:

```
$ mvn clean install
```

## Binary installation

A docker image is automatically built by the geOrchestra CI and is available here:
https://hub.docker.com/r/georchestra/atlas/

Unfortunately Debian and Yum packages are not yet available in the official
geOrchestra repositories.

## Datadir

This webapp is compatible with the geOrchestra "datadir-mode".  
It expects to run in a servlet container having the `georchestra.datadir` java option. Read more on the [georchestra/datadir](https://github.com/georchestra/datadir/tree/master) repository, and check the files from the [atlas sub-directory](https://github.com/georchestra/datadir/tree/master/atlas).

## Configuration

You will have to configure several configuration options in the [atlas.properties](https://github.com/georchestra/datadir/blob/master/atlas/atlas.properties) file:
 * The PostGreSQL URL / credentials
 * The SMTP server
 * Some extra informations required by the email templates
 * The temporary directory where to store the generated atlas.

## Client-side addon

To use this webapp, a mapfishapp (= geOrchestra viewer) addon has to be activated in the viewer.

This is achieved by editing the [client-side atlas config file](https://github.com/georchestra/datadir/tree/atlas/mapfishapp/addons/atlas) in your datadir.
