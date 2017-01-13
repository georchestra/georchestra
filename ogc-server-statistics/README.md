ogc-server-statistics
=====================

This README provides details to configure this module.


Creating the Logging table
==========================

This module requires a logging table called "ogc_services_log" in a postgres database.

Structure of "ogc_services_log" table have changed between 15.06 and 15.12. So, for a 
new installation you should use database.sql file. If you upgrade from a previous 
version of georchestra, you should use database upgrade procedure (see ../RELEASE_NOTES.md)

Configure log4j.properties
==========================

The test cases are using the appender configuration present in the src/test/resources/org/georchestra/ogcservstatistics/log4j.properties

You should adjust the key values taking into account your postgres configuration.

    log4j.appender.OGCSERVICES.jdbcURL=jdbc:postgresql://[host]:[port]/[database]
    log4j.appender.OGCSERVICES.databaseUser=[user]
    log4j.appender.OGCSERVICES.databasePassword=[pwd]

Example:  

    log4j.appender.OGCSERVICES.jdbcURL=jdbc:postgresql://localhost:5432/testdb
    log4j.appender.OGCSERVICES.databaseUser=postgres
    log4j.appender.OGCSERVICES.databasePassword=postgres


Debugging
=========

The following Java VM parameters could be useful:
 * Enable assertions: -ea 
 * Print the loaded values: -Dlog4j.debug














 
