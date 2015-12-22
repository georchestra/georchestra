Catalogapp
==========

Catalogapp is a simple and easy to use CSW client.


Parameters
==========

The application accepts several GET parameters :
 * **lang** can be set to any of the following : fr, en, es
 * **debug** when set to true, the application loads unminified javascript files
 * **noheader** when set to true, the application does not load the static header


How to run catalogapp without Tomcat ?
================================================

This mode is useful for **demo** or **development** purposes.

The *first* time, you need to previously compile catalogapp and all its dependencies

    $ ./mvn -Dmaven.test.skip=true -Ptemplate -P-all,catalogapp install;

then, each time you want to test a change in the configuration or the catalogapp module:

    $ cd catalogapp
    $ ../mvn -Ptemplate jetty:run

Point your browser to [http://localhost:8281/catalogapp/?noheader=true](http://localhost:8281/catalogapp/?noheader=true)
