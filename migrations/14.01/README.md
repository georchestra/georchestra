# UPGRADING from 13.09 to 14.01

 * As a result of [#569](https://github.com/georchestra/georchestra/issues/569), LDAP groups are now ```groupOfNames``` instances rather than ```posixGroup``` instances. You have to migrate your LDAP tree, according to the following procedure (please change the ```dc=georchestra,dc=org``` string for your own base DN):
   * dump your ldap **groups** with:

   ```
   ldapsearch -H ldap://localhost:389 -xLLL -D "cn=admin,dc=georchestra,dc=org" -w your_ldap_password -b "ou=groups,dc=georchestra,dc=org" > /tmp/groups.ldif
   ```

   * migration:

   ```
   sed -i 's/\(memberUid: \)\(.*\)/member: uid=\2,ou=users,dc=georchestra,dc=org/' /tmp/groups.ldif
   sed -i 's/posixGroup/groupOfNames/' /tmp/groups.ldif
   sed -i '/gidNumber/d' /tmp/groups.ldif OR sed -i 's/gidNumber/ou/' /tmp/groups.ldif if geofence is deployed
   sed -i 's/objectClass: groupOfNames/objectClass: groupOfNames\nmember: uid=fakeuser/' /tmp/groups.ldif
   ```

   * drop your groups organizationalUnit (```ou```)
   * optionally, have a look at the provided [georchestra-memberof.ldif](https://github.com/georchestra/LDAP/blob/master/georchestra-memberof.ldif) file, which creates & configures the [memberOf overlay](http://www.openldap.org/doc/admin24/overlays.html). As root, and after checking that the file targets the correct database (```olcDatabase={1}hdb``` by default): ```ldapadd -Y EXTERNAL -H ldapi:// < georchestra-memberof.ldif```
   * import the updated groups.ldif file.
 * analytics: the ExtJS submodule path has changed, be sure to run ```git submodule update --init``` when you switch branches.
 * databases: the downloadform, ogcstatistics and ldapadmin databases are now merged into a single one named "georchestra". Each webapp expects to find its tables in a dedicated schema ("downloadform" for the downloadform module, "ogcstatistics" for ogc-server-statistics, and "ldapadmin" for ldapadmin). See [#535](https://github.com/georchestra/georchestra/pull/535) for the complete patch. If you currently have one dedicated database for each module, you can keep your setup, provided you customize the ```shared.psql.ogc.statistics.db```, ```shared.psql.download_form.db``` & ```shared.ldapadmin.db``` maven filters in your own config. In any case, you'll have to rename the ```download``` schema (of the previous ```downloadform``` database) into ```downloadform```, and migrate the tables which were in the public schema of the databases ```ogcstatistics``` and ```ldapadmin``` into the newly created schemas.

Example migration script:

```
psql -d downloadform -c 'alter schema download rename to downloadform;'

wget --no-check-certificate https://raw.githubusercontent.com/georchestra/georchestra/14.01/ldapadmin/database.sql -O /tmp/ldapadmin.sql
psql -d ldapadmin -f /tmp/ldapadmin.sql
psql -d ldapadmin -c 'GRANT ALL PRIVILEGES ON SCHEMA ldapadmin TO "www-data";'
psql -d ldapadmin -c 'GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA ldapadmin TO "www-data";'
psql -d ldapadmin -c 'GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA ldapadmin TO "www-data";'
psql -d ldapadmin -c 'insert into ldapadmin.user_token (uid, token, creation_date) select uid, token, creation_date from public.user_token;'
psql -d ldapadmin -c 'drop table public.user_token;'

wget --no-check-certificate https://raw.githubusercontent.com/georchestra/georchestra/14.01/ogc-server-statistics/database.sql -O /tmp/ogcstatistics.sql
psql -d ogcstatistics -f /tmp/ogcstatistics.sql
psql -d ogcstatistics -c 'GRANT ALL PRIVILEGES ON SCHEMA ogcstatistics TO "www-data";'
psql -d ogcstatistics -c 'GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA ogcstatistics TO "www-data";'
psql -d ogcstatistics -c 'GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA ogcstatistics TO "www-data";'
psql -d ogcstatistics -c 'insert into ogcstatistics.ogc_services_log (id, user_name, date, service, layer, request, org) select id, user_name, date, service, layer, request, org from public.ogc_services_log;'
psql -d ogcstatistics -c 'drop table public.ogc_services_log;'
```

 * download form: the module is disabled by default (```shared.download_form.activated=false```). Be sure to set the value you want in your shared.maven.filters file.
 * extractorapp:
   * ```BUFFER_VALUES``` has changed. If you had a custom value in your GEOR_custom.js file, you have to modify it according to the new syntax.
   * the ```geobretagne_production``` env variable has been removed - see [#97](https://github.com/georchestra/georchestra/pull/97)
 * geoserver: be sure to set the ```file.encoding``` tomcat option for geoserver to interpret correctly UTF-8 SLDs (read [how](INSTALL.md#geoserver)).
 * ldapadmin:
   * accessing ```/ldapadmin/privateui/``` is now restricted to members of the ```MOD_LDAPADMIN``` group. It is recommended that only members of the ```ADMINISTRATOR``` or ```SV_ADMIN``` administrative groups belong to ```MOD_LDAPADMIN```, since this group allows privileges escalation.
   * new ```shared.ldapadmin.db``` parameter to specify the ldapadmin database name (defaults to "georchestra").
   * the ldapadmin private app is now accessed via /ldapadmin/privateui/ rather than /ldapadmin/privateui/index.html
 * mapfishapp:
   * geonames now require you to create an account in order to enable queries on their free web services (see [#563](https://github.com/georchestra/georchestra/issues/563)). Please change the default account in your profile's GEOR_custom.js ```GEONAMES_FILTERS``` variable.
   * addons: custom addons relying on local web services should no longer assume that the application path is ```/mapfishapp```. Instead, they should use the new ```GEOR.config.PATHNAME``` constant, eg [here](https://github.com/georchestra/georchestra/blob/04017309f3880a0c558537235c92f70a269722d1/mapfishapp/src/main/webapp/app/addons/annotation/js/Annotation.js#L486).
   * the app now requires a dedicated database schema, please refer to the [INSTALL.md](INSTALL.md#postgresql) documentation.
   * new config option: ```SEND_MAP_TO``` for [#443](https://github.com/georchestra/georchestra/issues/443), please read the [doc](https://github.com/georchestra/template/blob/34496d62701e809c80235275a9e2a0b4b46f1123/mapfishapp/app/js/GEOR_custom.js#L583).
   * new config option: ```FORCE_LOGIN_IN_TOOLBAR```
   * the ```NS_EDIT``` config option has been removed, and mapfishapp/edit is no longer routed. By default, all layers served by the platform geoserver are editable (see ```GEOR.custom.EDITABLE_LAYERS```), provided the user has the rights to (defaults to members of ```ROLE_ADMINISTRATOR```, see ```GEOR.custom.ROLES_FOR_EDIT```).
   * the contexts referenced in your ```GEOR.custom.CONTEXTS``` array are now able to reference layers with their full attribution information (text, logo & link). Have a look at the provided [default.wmc](https://github.com/georchestra/template/blob/55f24c8625e737d0b4567db92966c98502578766/mapfishapp/default.wmc#L39).
   * print: some parameters have changed when the print module was updated: ```maxIconWidth``` -> ```iconMaxWidth```, ```maxIconHeight``` -> ```iconMaxHeight``` (see [e6231c](https://github.com/georchestra/template/commit/e6231c8cbf325dfa2bf96fcaa14096fc0c64ab89)).
 * ogcservstatistics - disabled by default: ```shared.ogc.statistics.activated=false```. Be sure to set the value you want in your shared.maven.filters file.
 * static: the "static" module has been renamed into "header": your deployment scripts *must* be adapted, as well as your apache2 configuration (or any other reverse proxy).
 