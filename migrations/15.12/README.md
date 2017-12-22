# UPGRADING from 15.06 to 15.12

When upgrading your instance, you need to choose between the following alternatives:
 * you can go on compiling from the sources, using a "config directory" deriving from the [georchestra/template](https://github.com/georchestra/template) repository we provide (using branch 15.12 !),
 * you can use the generic WARs we provide on [build.georchestra.org/wars](http://build.georchestra.org/wars/),
 * you can use the packages we provide for Debian, CentOS or RedHat on [build.georchestra.org](http://build.georchestra.org/),
 * you can build on the docker images we provide on [hub.docker.com](https://hub.docker.com/r/georchestra/).

Generic WARs expect to find their configuration in a folder, typically bootstrapped from the content of the [georchestra/datadir](https://github.com/georchestra/datadir/) repository (branch 15.12 !).
This folder will generally be `/etc/georchestra` and the webapps will be aware of this location through the use of the tomcat additional parameter `-Dgeorchestra.datadir=/etc/georchestra`.

Packages provide:
 * the WAR files, typically in `/usr/share/lib/georchestra-MODULENAME/`,
 * their own copy of the `/etc/georchestra` folder.
If using packages, it is your responsibility to symlink WAR files in your tomcat `webapps` folder, for automatic deployment of the webapps.

Keep in mind that the default configurations (either "template config" or "data dir") consider that geOrchestra runs on a SSL-enabled server. If this is not the case, please check carefully your datadir with [#1123](https://github.com/georchestra/georchestra/issues/1123) in mind.

 * Mapfishapp has been revamped to allow dynamic customization of addons and contexts. This means that 2 new controllers are now responsible of the JSON blocks that were previously present in the GEOR_custom.js file. As a result, it introduced some stricter conventions that have to be respected so that the controllers can function correctly:

   *  Contexts: in "datadir-mode" (ie with the `-Dgeorchestra.datadir` parameter), contexts should be uploaded to `<georchestra.datadir>/mapfishapp/contexts/context.wmc` along with a picture (`<georchestra.datadir>/mapfishapp/contexts/images/context.jpg` or `.png`). Contexts belonging to the webapp (as a result of compilation, for instance) are also taken into account by the controller (in the contexts/ subdirectory).

Imagine you had one context referenced in your GEOR_custom.js as such:
```js
     CONTEXTS: [{
         label: "My context",
         thumbnail: "app/img/contexts/osm.png",
         wmc: "default.wmc",
         tip: "A unique OSM layer",
         keywords: ["background"]
     },{
         ...
```
The `label`, `tip` and `keywords` fields are now dynamically extracted from the context file.
As a result, the `default.wmc` file should be edited to integrate the `Title` (matches `label`), `Abstract` (matches `tip`) and `Keywords` (matches `keywords`) strings, eg:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<ViewContext xmlns="http://www.opengis.net/context" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.1.0" id="default" xsi:schemaLocation="http://www.opengis.net/context http://schemas.opengis.net/context/1.1.0/context.xsd">
  <General>
    <Window width="1373" height="709"/>
    <BoundingBox minx="455462.822367389977" miny="6838526.51230099984" maxx="875255.821295570000" maxy="7055302.35806940030" SRS="EPSG:2154"/>
    <Title>My context</Title>
    <Abstract>A unique OSM layer</Abstract>
    <KeywordList>
      <Keyword>background</Keyword>
    </KeywordList>
```

   *  Addons: in "datadir-mode", they need to be stored either in `<georchestra.datadir>/mapfishapp/addons/` (recommended) or in the `app/addons/` subdirectory of the webapp. Without the georchestra.datadir parameter, only the ones belonging to the webapp are taken into account.

 * As a result of [#1040](https://github.com/georchestra/georchestra/pull/1040), LDAP groups are now ```groupOfMembers``` instances rather than ```groupOfNames``` instances. In addition, the ```PENDING_USERS``` group was renamed to ```PENDING```. You have to migrate your LDAP tree, according to the following procedure (please change the ```dc=georchestra,dc=org``` string for your own base DN and provide a suitable password):
   * dump your ldap **groups** with:
   ```
   ldapsearch -H ldap://localhost:389 -xLLL -D "cn=admin,dc=georchestra,dc=org" -w your_ldap_password -b "ou=groups,dc=georchestra,dc=org" > /tmp/groups.ldif
   ```
   * migration:

```
sed -i 's/PENDING_USERS/PENDING/' /tmp/groups.ldif
sed -i 's/groupOfNames/groupOfMembers/' /tmp/groups.ldif
sed -i '/fakeuser/d' /tmp/groups.ldif
```

   * load the [groupOfMembers](ldap/groupofmembers.ldif) definition:
   ```
    sudo ldapadd -Y EXTERNAL -H ldapi:/// -f groupofmembers.ldif
   ```
   * drop your groups organizationalUnit (```ou```)
   * import the updated groups.ldif file.

 * As a result of [#1108](https://github.com/georchestra/georchestra/issues/1108) and [#556](https://github.com/georchestra/georchestra/issues/556), the ogc-server-statistics model has been changed. To upgrade your database, you should follow this procedure:

```
wget https://raw.githubusercontent.com/georchestra/georchestra/master/migrations/15.12/populate_stats_roles.py
```
In this script, change the values of the following variables according to your configuration: LDAP_URI, BIND_WITH_CREDENTIALS, LDAP_BINDDN, LDAP_PASSWD, GROUPS_DN, GROUP_OBJECT_CLASS.

Create a virtual env for python :
```
virtualenv migr-ogcstatistics
cd migr-ogcstatistics/
source bin/activate
easy_install ldap3
```

Then run the script:
```
python populate_stats_roles.py > /tmp/ogc-server-statistics-migration.sql
```
Check the sql migration file looks good.

Next step is to execute the two migration scripts:
```
wget https://raw.githubusercontent.com/georchestra/georchestra/master/migrations/15.12/update_to_1512.sql -O /tmp/update_to_1512.sql
psql -d georchestra -f /tmp/update_to_1512.sql
psql -d georchestra -f /tmp/ogc-server-statistics-migration.sql
```
Please note that the `ogc-server-statistics-migration.sql` script might take a very long time, depending on your database size.

Finally, ensure geOrchestra database user is owner of database. If your database is dedicated to geOrchestra (no other
apps are running in same database), you can use following procedure to reset ownership of all objects to selected user, for
example ```www-data``` :

```
wget https://raw.githubusercontent.com/georchestra/georchestra/15.12/postgresql/fix-owner.sql -O /tmp/fix-owner.sql
psql -d georchestra -f /tmp/fix-owner.sql
psql -d georchestra -c "SELECT change_owner('mapfishapp', 'www-data');";
psql -d georchestra -c "SELECT change_owner('downloadform', 'www-data');";
psql -d georchestra -c "SELECT change_owner('ldapadmin', 'www-data');";
psql -d georchestra -c "SELECT change_owner('ogcstatistics', 'www-data');";
psql -d georchestra -c "SELECT change_owner('public', 'www-data');";
# if you deploy geonetwork :
psql -d georchestra -c "SELECT change_owner('geonetwork', 'geonetwork');";
```

And if you deploy geofence :
```
psql -d georchestra -c "SELECT change_owner('geofence', 'www-data');";
```

Finally, you can drop maintenance function :
```
 psql -d georchestra -c "DROP FUNCTION change_owner(text, text);";
```
