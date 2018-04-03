# UPGRADING from 14.01 to 14.06

The way geOrchestra is configured has been streamlined:
 - there are **default parameters which are shared by several modules**, in [config/shared.maven.filters](config/shared.maven.filters). A "standard" install should not require you to bother about them. But if your setup is different from the default one, you may have to copy one or more of theses properties into your own shared maven filters (read on), in order to be able to customize them.
 - there are also **shared parameters which have to be customized** for your own instance. These can be found in the **build_support/shared.maven.filters** file inside your own configuration directory. As your config dir inherits from the template config dir, it should be very similar to [georchestra/template:build_support/shared.maven.filters](https://github.com/georchestra/template/blob/master/build_support/shared.maven.filters). The shared maven filters from this file override those from [config/shared.maven.filters](config/shared.maven.filters)
 - finally, there are **parameters for every individual geOrchestra module** (geoserver, geofence, mapfishapp, extractorapp, proxy, ldapadmin), which can be customized via the **build_support/GenerateConfig.groovy** file in your own config dir. Have a look at the one provided by the template config for an example:  [georchestra/template:build_support/GenerateConfig.groovy](https://github.com/georchestra/template/blob/master/build_support/GenerateConfig.groovy).

As a result, a "standard geOrchestra configuration" should not require you to edit more than 2 files: one for the shared parameters, and an other one for module-specific parameters.  
Note: copying maven.filter files in your own configuration dir (which was an older practice) is not anymore recommended because it is very difficult to maintain when upgrading version.
=> you should remove any maven.filter file from your own configuration, and eventually copy the values you had customized into build_support/GenerateConfig.groovy


**Build:**

The "Jetty Maven2" Repository provided by oss.sonatype.org requires HTTPS now. The fix was pushed to the georchestra and geonetwork repositories, but in the mean time, your /home/$USER/.m2 local repository might have gotten corrupted with html files rather than jars. If you experience compilation errors, you might need to clean your local maven repo, by running ```rm -rf ~/.m2/repository/org/apache/maven```

Upgrading your build is a 2-steps process.  
Let's assume your local geOrchestra source code repository is located in ```/path_to_georchestra_root/```.

First you have to update the sources from the remote origin and update yours:
```
cd /path_to_georchestra_root/
git fetch origin
git checkout 14.06
git submodule sync
git submodule update --init
```

Then you need to update your configuration directory to align it with the template configuration, [branch 14.06](https://github.com/georchestra/template/tree/14.06).

In the following, we assume that your configuration directory is versioned with git, and has the geOrchestra template config set as "upstream" remote repository:
```
cd /path_to_georchestra_root/config/configuration/<yours>
git fetch upstream
git merge upstream/14.06
```
Then you'll probably have to fix some conflicts.  
Note: if you do not know how to fix these conflicts, you're probably better off starting again with a fresh fork of the template config directory.


The build process remains unchanged, but there is a difference at the end.  
In the previous releases, the artifacts included a "-private" suffix in their name. We were making the assumption that all WARs would be deployed to the same tomcat.
This is not the case anymore. This implies that the security proxy now resides in a different servlet container than the proxied webapps. If this is not your case, juste rename them back with the "-private" suffix.


**LDAP:**

The [LDAP repository](https://github.com/georchestra/LDAP) holds branches for the 13.09, 14.01 and 14.06 releases.
In 13.09, groups are instances of posixGroup. From 14.01 to 15.06, they are instances of groupOfNames.
Since 15.12, they are instances of groupOfMembers which allows to have empty groups.

Between 14.01 and 14.06 branches, here are the differences:
 - the ```MOD_EXTRACTORAPP``` group was created. You should assign to it the users which already had access to extractorapp (typically members of ```SV_USER```)
 - the privileged geoserver user ("shared.privileged.geoserver.user") was renamed from ```extractorapp_privileged_admin``` to ```geoserver_privileged_user``` because it is no more used only for the extractor.


**Databases:**

In this release, the GeoNetwork database was merged into the unique default georchestra database.
It is now a schema inside the main database.

This change requires that a "geonetwork" db user is created and granted rights:
```
createuser -SDRIP geonetwork (the expected password for the default config is "georchestra")
psql -d georchestra -c 'CREATE SCHEMA geonetwork;'
psql -d georchestra -c 'GRANT ALL PRIVILEGES ON SCHEMA geonetwork TO "geonetwork";'
```

The full migration process is described in [#601/comment](https://github.com/georchestra/georchestra/issues/601#issuecomment-36889319).
This [link](https://github.com/georchestra/georchestra/issues/601#issuecomment-36890670) also provides hints if you prefer to keep your existing 2-databases setup.


**Native libs:**

The GDAL java bindings (gdal.jar) are no more provided by webapps, because this can lead to issues when the native libs are loaded several times in the same servlet container.
Instead, the bindings should be installed once and for all in a folder accessible by the servlet container.

Please refer to the documentation relative to [native libs handling in geOrchestra](https://github.com/georchestra/georchestra/blob/master/geoserver/NATIVE_LIBS.md), section "Tomcat configuration"


**Shared maven filters:**

Several previous shared maven filters have been removed because they were not truly shared between modules:
 * ```shared.mapfishapp.docTempDir```
 * ```shared.geofence.*```
 * ```shared.checkhealth.*```

You'll find the exact same settings in build_support/GenerateConfig.groovy

```shared.geonetwork.dir``` was removed because it was useless

Seevral shared maven filters were homogeneized:
 * shared.psql.ogc.statistics.db becomes shared.ogc.statistics.db
 * shared.psql.download_form.db -> shared.download_form.db
 * shared.psql.geonetwork.db -> shared.geonetwork.db
 * shared.ldapadmin.db
 * shared.geofence.db

Finally, ```geonetwork.language``` was renamed into ```shared.geonetwork.language```


**Miscellaneous:**

If you're using ldapadmin, make sure you've setup [ReCaptcha](http://www.google.com/recaptcha/) keys for your own domain.  
Hint: search for privateKey in your GenerateConfig.groovy.

French projections (typically EPSG:2154) have been removed from the extractorapp and mapfishapp config files.  
Be sure to check your GEOR_custom.js files if you need them.

mapfishapp: ```GEONETWORK_URL``` was renamed into ```GEONETWORK_BASE_URL```.  
Caution: the expected string for this config option has changed too: from eg. "http://geobretagne.fr/geonetwork/srv/fre" to: "http://geobretagne.fr/geonetwork".
