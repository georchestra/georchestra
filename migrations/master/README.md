# Postgresql docker image

⚠️ If you use postgresql in docker, there's a change since https://github.com/georchestra/georchestra/pull/4574

The volume used by postgres to store data is now `/var/lib/postgresql/` instead of `/var/lib/postgresql/data`.

# Usage of logabck

All librairies and console are now using logback (which is embedded with spring parent).

Folders use in georchestra datadir now use <georchestra.datadir>/<context>/logback/logback.xml. E.g: /etc/georchestra/console/logback/logback.xml

