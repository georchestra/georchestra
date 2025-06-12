# From 24.0 to 25.0

## Header

[Georchestra WebComponent Header](https://github.com/georchestra/header) can now be [configured](https://github.com/georchestra/header/blob/main/CONFIG.md) using a file at runtime.

The default configuration is provided in [`default-config.json`](https://github.com/georchestra/header/blob/main/src/default-config.json).
Here is [an example](https://github.com/georchestra/header/blob/main/public/sample-config.json) of file which can be used to configure the header.

Each object configuration is set [here](https://github.com/georchestra/header/blob/main/src/config-interfaces.ts#L32-L53).

The file must be provided by a web server (e.g. nginx) and must be configured in several files of the datadir:
- `default.properties` -> [headerConfigFile](https://github.com/georchestra/datadir/blob/25.0/default.properties#L49)
- `mapstore/config/localConfig.json` -> [header.configFile](https://github.com/georchestra/datadir/blob/docker-25.0/mapstore/configs/localConfig.json#L29)

### Header in Metadata Editor

The Metadata-editor, which comes from the Geonetwork-UI suite, needs a small adaptation to work with the geOrchestra header.

Before the injection of the `<geor-header>` tag, we need to inject a CSS script (replace `<your-header-height>` with the height of your header):

```css
body {display: flex; flex-direction: column} 
body md-editor-root, .h-screen {height: calc(100vh - <your-header-height>px);}
main {max-height: calc(100vh - <your-header-height>px - 65px) !important}
```

## Elasticsearch and Kibana

Since Geonetwork [4.4.3](https://docs.geonetwork-opensource.org/4.4/overview/change-log/version-4.4.3/#index-changes), Elasticsearch 8.x is supported. 

### ES v7 to v8 upgrade

Elasticsearch can be upgraded from v7 to v8.

If so, you may need to set two environment variables for Elasticsearch to work properly:
```
xpack.security.enabled: false
xpack.security.enrollment.enabled: false
```

And deactivate the `kibana.index` in [kibana.yaml](https://github.com/georchestra/docker/blob/25.0/resources/kibana/kibana.yml#L3).

Example for docker: [here](https://github.com/georchestra/docker/blob/25.0/docker-compose.yml#L365-L366)

## GeoNetwork 4.2.8 to 4.4.8 migration notes

After the upgrade (of Geonetwork and Elasticsearch):
- Delete index and reindex.
- JS and CSS cache must be cleared.

You can follow [those `upgrade_geonetwork` steps](https://github.com/georchestra/georchestra/blob/master/docsv1/upgrade_geonetwork.md) to update Geonetwork. 

### Xlinks

> Previously on 4.0.6, it was possible to add contacts to an ISO19139 MD using xlinks, after upgrading to 4.2.2 it wasn't possible anymore.

Since this is merge <https://github.com/georchestra/geonetwork/pull/320>

Xlink contacts are enabled by default in geonetwork config-editors. It allows to "reuse" contacts in different metadata records, and to keep the contact information in a single place.

In order to migrate the metadata you can use the following documentation: <https://github.com/georchestra/geonetwork/blob/6ee9f9d357eb2c6c26d4b02827e0c24fa75aa0a8/georchestra-migration/about-xlinks.md>


## LDAP migration
### orgUniqueId attribute on the georchestraOrg schema

The `orgUniqueId` organization attribute was added to the `georchestraOrg` LDAP schema.

This attribute will contain a unique organization identifier. The difference from existing identifiers is that this is not a system identifier.

This attribute is filled in when an organization is created, or via the console form like other organization attributes. This attribute will also contain the OAuth2 organization identifier when a user is connected using an external identity provider.
* Adapt georchestra schema definition

To upgrade the ldap, you need first to find the geOrchestra schema definition using the following command:

```
ldapsearch -H ldap://localhost:389 -D cn=admin,dc=georchestra,dc=org -w secret -b cn=schema,cn=config '(cn=*georchestra)' dn
```

Commands provided in [ldap_migration.ldif](ldap_migration.ldif) assume that required `dn` is:

`dn: cn={5}georchestra,cn=schema,cn=config`

If you find a different `dn` (please update Commands provided in [ldap_migration.ldif](ldap_migration.ldif) file.

* Verify georchestraOrg `olcObjectClasses`

Commands provided in [ldap_migration.ldif](ldap_migration.ldif) assume that georchestraOrg `olcObjectClasses` schema is:

```
olcObjectClasses: ( 1.3.6.1.4.1.53611.1.1.2
    NAME 'georchestraOrg'
    DESC 'georchestra org'
    SUP top
    AUXILIARY
    MAY (jpegphoto $ labeledURI $ knowledgeInformation $ georchestraObjectIdentifier))
```
Please, verify this schema with the following commands (adapt `cn={5}georchestra,cn=schema,cn=config`): 

```
ldapsearch -Y EXTERNAL -H ldapi:/// -b cn={5}georchestra,cn=schema,cn=config '(objectClass=olcSchemaConfig)' olcObjectClasses
```

If you find a different olcObjectClasses, please update commands provided in [ldap_migration.ldif](ldap_migration.ldif) file with the correct schema.
 

* Apply changes

Please follow previous steps with precaution before running final script.

Finally, use the following command with the [ldap_migration.ldif](ldap_migration.ldif) file:

```
ldapmodify -H "ldap://ldap:389" -D "cn=admin,dc=georchestra,dc=org" -w "secret" -f ldap_migration.ldif
```

