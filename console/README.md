# geOrchestra's Console

![console](https://github.com/georchestra/georchestra/workflows/console/badge.svg)

The webapp is made of three parts:
  * on the public side, it gives the ability to:
    * create an account (`/console/account/new`)
    * reset your password (`/console/account/passwordRecovery`)
  * on the private side known as "the manager" (`/console/manager/`), it allows:
    * **administrators** to
        * manage users, organisations, roles with a neat user interface
        * check platform stats
    * **delegated administrators** (on a given set of organisations) to
       * create, update, delete users belonging to these organisations
       * grant or remove roles (chosen by the administrator) to users belonging to this same set of organisations
       * check platform stats
  * the user's profile page (`/console/account/userdetails`) allows:
    * **referents** to modify their organization details
    * **users** to
      * modify their details
      * view their organisation's detail
      * download all the data they created on the platform (as required by the EU General Data Protection Regulation)
      * delete their account

To clarify roles:
 * **administrators** are users holding the `SUPERUSER` role.
 * **delegated administrators** are special users having the `ORGADMIN` role.
An administrator has set up a delegation for them
(on page `/console/manager/#!/users/${user_id}/delegations`). They can access
the manager, where they can grant a subset of roles to users belonging to a
collection of organisations.
 * **referents** are users holding the `REFERENT` role. They are allowed to
modify several of their organisation fields, namely: name, address, logo,
description, website.
 * **users** are regular geOrchestra users, obviously having the `USER` role.


## Configuration options

As for every other geOrchestra webapp, its configuration resides in the
(ill-named) [data directory](https://github.com/georchestra/datadir/),
typically something like `/etc/georchestra`, where it expects to find a
`console` sub-directory.

The most important configuration file is probably
[console.properties](https://github.com/georchestra/datadir/blob/master/console/console.properties),
where the majority of options are commented out, either because there are
default, common values already provided by the datadir's
[default.properties](https://github.com/georchestra/datadir/blob/master/default.properties),
or because the console code offers sensible, built-in, values.

Below, we go through the most interesting options.

### Moderated signup

If the `moderatedSignup` config option is set to `true` (which is the default),
each time a new user requests an account:
  * an email is sent to all users having the SUPERUSER role and also to those
which hold an admin delegation for the declared organisation (if any)
  * the newly created account is stored inside the "ou=pendingusers" LDAP
organizational unit (which grants zero rights on the SDI).

If set to `false`, the user is immediately considered as registered, and is
stored inside the "ou=users" LDAP organizational unit. An email is also sent to
all SUPERUSER users and delegated admins if any.

### Read-Only user identifier

The `readonlyUid` option, when set to `true`, prevents the user from choosing
its own username on the registration page. It is set to `false` by default.

If `true`, the username is crafted with the first letter of firstname
concatenated with the user lastname.

### Required fields

The `requiredFields` parameter defaults to
`firstName,surname,email,uid,password,confirmPassword`.

Adding other fields, separated by commas, will make them mandatory in the user
account forms. Note that this parameter does not affect the manager.

Example: to make the "Organisation" and "Title" fields mandatory, set it to:
```
requiredFields=firstName,surname,email,uid,password,confirmPassword,org,title
```
The possible values are: `firstName`, `surname`, `phone`, `facsimile`, `org`,
`title`, `description`, `postalAddress`. **email**, **uid** and **password**
are always required.

### Organisation types

Organisations are classified. By default, the console offers the choice between
Association, Company, NGO, Individual, Other.

This can be set to any other values with:
```
orgTypeValues=Association,Company,NGO,Individual,Other
```

### Custom areas

Organisations may have an area of competence, built from a collection of
features.

Features are loaded from a custom endpoint, `/public/area.geojson` which will:
 * serve a GeoJSON file from the datadir
    * From the filesystem (beware of rights on file)
    * from the datadir roots folder
    * from the console directory in datadir. 
 * redirect to a URL if `AreaUrl` starts with `http`.

The geojson FeatureCollection has to be in EPSG:4326 projection. The native SRS of the layer MUST be in EPSG:4326 
 (no on-the-fly transformation).

Area default url is:
```
AreasUrl=area.geojson
```

Note that it can be set to a custom WFS request like this one:
```
AreasUrl=https://my.server.org/geoserver/ows?SERVICE=WFS&REQUEST=GetFeature&typeName=gadm:gadm_for_countries&outputFormat=json&cql_filter=ISO='FRA'
```

Please pay attention to these options too: `AreasKey`, `AreasValue`, `AreasGroup`
`AreaMapZoom`, `AreaMapCenter`.

### ReCaptcha

To fight robots, get a pair of ReCaptcha keys for your site:
 * head to [https://www.google.com/recaptcha/admin/create](https://www.google.com/recaptcha/admin/create)
 * choose reCAPTCHA version 2 with the "I'm not a robot" checkbox
 * fill in your domain

Once created, set the following parameters:
* `privateKey`
* `publicKey`


### Protected Users

Several user accounts can be protected against deletion or modification, with
the `protectedUsersList` property, which holds a comma separated list of user
accounts `uid`. These users also do not show up in the console.

## Developer's corner

### Integration Testing

> TL;DR: run integration tests with `mvn verify`. 

While `mvn test` will run the unit tests but not the integration tests, `mvn verify` (or any goal past that, like `mvn install`) is used to run (also) integration tests.

Integration tests that require an LDAP and/or PostgreSQL database will set them up
using georchestra's "testcontainers".

To skip integration tests, use the standard `maven-failsafe-plugin`'s flag `-DskipITs`. `maven-surefire-plugin`'s `-DskipTests` 
skips both unit and integration tests.

#### Running from an IDE while developing

While writing integration tests, it could be cumbersome to wait for the launch and shut down of test containers.

In this case it's better to have `georchestra/ldap` and `georchestra/database` containers already running and instructing the test run to use them directly.

To do so launch them externally mapping the ports 389 and 5432 as appropriate,  and create a launch configuration for the test class being working on, and set the `-DpgsqlPort=<db mapped port> -DldapPort=<ldap mapped port>`.

When the test case is finished, make sure to run `mvn verify` to check it works properly within the maven build cycle.

Finally, when writing integration tests, make sure they're self contained and would not be affected by any existing data in the external resources.
