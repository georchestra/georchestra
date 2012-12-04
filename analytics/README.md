Analytics
=========

Analytics is a GUI which displays monthly and global statistics on platform usage: GeoNetwork downloads, custom data extractions & OGC web services consumption.

It relies on two other modules to collect figures in a database:
 * [downloadform](../downloadform/README.md) for GeoNetwork downloads & custom data extractions,
 * [ogc-server-statistics](../ogc-server-statistics/README.md) for OGC web services consumption

With regards to GeoNetwork downloads, you get a nice view of:
 * metadata id, filename, number of hits
 * username, number of downloads
 * organisation, number of downloads

For custom data extractions:
 * service type, service URL, layer name, hits
 * username, number of requests
 * organisation, number of requests
 
For OGC web services consumption:
 * service type, layer name, request type (getmap/getfeature/getcapabilities/...), hits
 * username, number of requests
 * organisation, number of requests

Each table can be exported to CSV for easy offline use.