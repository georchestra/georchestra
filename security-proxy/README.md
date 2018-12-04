# security-proxy

The security-proxy (aka SP) belongs to geOrchestra core, since it is the component which :
 * handles user sessions
 * routes requests to webapps (except CAS)

The behavior is controlled by the files from the `<datadir_root>/security-proxy` folder, which can be found [here](https://github.com/georchestra/datadir/tree/master/security-proxy)

## How-to integrate a new application in geOrchestra ?

The goal here is to benefit from the [SSO](https://en.wikipedia.org/wiki/Single_sign-on) feature for the new application without having to "CASify" it.

### Proxy configuration

It may sound obvious, but the new application has to be proxified by the security-proxy first !
It can be done in the [targets-mapping](https://github.com/georchestra/datadir/blob/master/security-proxy/targets-mapping.properties) file, which can be found in geOrchestra datadir. Remember: changes in this file requires to restart the security proxy.
This file maps public URLs to internal (private) URLs.
For instance, the `header=http://localhost:8280/header/` line means "requests hitting the /header path should be routed to http://localhost:8280/header".

Now, imagine your application has a public frontend (`/newapp/frontend`) and a private backend (`/newapp/backend`).
You probably would like to restrict backend access to administrators, or people having a specific role.

This can be done very easily with the [security-mappings](https://github.com/georchestra/datadir/blob/master/security-proxy/security-mappings.xml) file, also from the geOrchestra datadir.
In this file, the last line starting with `<intercept-url pattern=".*" access="IS_AUTHENTICATED_ANONYMOUSLY,ROLE_USER...` means "by default, grant access to every path for anonymous and authenticated users". This means the frontend app is already available to every user, once proxified.

To restrict access to the backend, one just has to insert a new line before the last one.
In the example below, we choose to restrict backend access to people having the `ADMINISTRATOR` role:
```xml
<intercept-url pattern="/newapp/backend" access="ROLE_ADMINISTRATOR" />
```

It is also possible to create a specific role which grants access to the backend, eg with role `NEWAPP_ADMIN`. The rule becomes:
```xml
<intercept-url pattern="/newapp/backend" access="ROLE_NEWAPP_ADMIN" />
```

### Application configuration

#### Headers

With every request, the proxy adds specific HTTP headers, allowing the application to know:
 * if the request comes from a registered user, or an anonymous one - this is `sec-username` (not provided if anonymous).
 * which roles the user bears - `sec-roles` is a semi-colon separated list of roles (not provided if anonymous).
 * which organisation the user belongs to - `sec-orgname` provides the human-readable organisation title while `sec-org` is mapped onto the organisation id (LDAP's `cn`).

Several other user properties are also provided as headers:
 * `sec-email` is the user email
 * `sec-firstname` is the first name (LDAP `givenName`)
 * `sec-lastname` is the second name (LDAP `sn`)
 * `sec-tel` is the user phone number (LDAP `telephoneNumber`)

Additional headers can be configured in the proxy with the [headers-mapping](https://github.com/georchestra/datadir/blob/master/security-proxy/headers-mapping.properties) file.

The application handles requests appropriately thanks to the headers received.
Some applications will require a direct connection to the LDAP (where users, roles and organisations objects are stored), for instance to list all organisations.

#### Entrypoints

The login entrypoint is `/cas/login` but more generally, one uses the `login` GET parameter in any querystring to force login into a given application.
As a result, the new application may generate links like these: `/newapp/frontend/?login`, for instance if some features in the frontend are only available when authenticated.

Logout entrypoint is `/logout`.
Password recovery form is available from `/console/account/passwordRecovery`.
Account creation form can be found at `/console/account/new`.

## SP-trust-SP feature

This feature is rather confidential, since it involves two SP instances, while a standard geOrchestra only requires one. It may prove useful in some corner cases.

### description

A SP (2) may now trust requests coming from another SP (1) :

```
Client --> SP1 --> SP2  --> console
                        |
                        |----> geoserver
                        |
                        -----> mapfishapp
```

With this setup, every request coming from SP1 is forwarded (with untouched `sec-*` headers) by SP2.

Authentication being already performed at the SP1 level, SP2 does not have to execute any additional checks (eg: test user exists in LDAP, has the required roles, ...).


### implementation

In the security-proxy configuration file, a '[trustedProxy](https://github.com/georchestra/datadir/blob/8d189b5ce7d7472c03325c2180eb5f7ccc0f54e4/security-proxy/security-proxy.properties#L17-L18)' property lists IP addresses from which requests should be trusted.
By default, this property is set to '127.0.0.1, localhost'. Be careful to only add trustworthy servers in here !

The SP2 `security-proxy.properties` file should have `trustedProxy` set to SP1 IP.

The SP1 `targets-mapping.properties` configuration file should target SP2, eg with:
```properties
geoserver=http://sp2:8080/geoserver/
console=http://sp2:8080/console/
mapfishapp=http://sp2:8080/mapfishapp/
```
