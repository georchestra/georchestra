downloadform
============

When deployed, this module exposes web services which allow geonetwork and extractorapp to record information on the current user and the expected data usage upon file download or data extraction.

geOrchestra's GeoNetwork and extractorapp each have a custom UI for that, but the web services belong to this module.

This behavior can be (de)activated by setting the shared.download_form.activated variable in your profile's shared.maven.filter file.
This setting affects both geonetwork and extractorapp.

The submitted data is recorded in a database schema (called "download") which has to be created through the use of the samples/sample.sql file.
The expected data usage is stored in the download.data_use table, and published through the downloadform/data_usage web service. You are free to customize it to feel your needs.
