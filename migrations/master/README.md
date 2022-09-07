# From 22.X to master

## Geoserver

In Docker context we've set new java option GEOWEBCACHE_CONFIG_DIR to /mnt/geoserver_datadir/gwc.

So you may need to move following files from `geowebcache_tiles` to `geoserver_datadir/gwc`:
- geowebcache.xml
- geowebcache-diskquota.xml
- geowebcache-diskquota-jdbc.xml
