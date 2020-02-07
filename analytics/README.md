Analytics
=========

![analytics](https://github.com/georchestra/georchestra/workflows/analytics/badge.svg)

Analytics offers
 * services which are used by the console
 * a GUI which displays **monthly** and **global** statistics on platform usage, through OGC web services monitoring.

It relies on the [ogc-server-statistics](ogc-server-statistics/README.md) module (which is embedded into the security-proxy) to collect figures in a database:
 * service type, layer name, request type (getmap/getfeature/getcapabilities/...), hits
 * username, number of requests
 * organisation, number of requests

Each table can be exported to CSV for easy offline use.

