Console
=======

A webapp with a public interface and a private one, which allows to manage users and roles.

All strings and templates should take into account i18n (3 langs by default: en/es/fr)

Public UI
---------

The public UIs will be accessed:
 * from the CAS login page, by the way of text links: "lost password ?" and "create account".
 * from every SDI page, by clicking on the username: "Edit user details"

These pages should be light (no need to ship ExtJS).

### Lost Password

The page asks for user email. An optional `email` parameter can be passed to preset the email field (eg: /console/account/passwordRecovery?email=user@domain.tld).

If the given email matches one of the LDAP users:
 * an email is sent to this user with a unique https URL to reset his password (eg: /console/account/changePassword?token=54f23f27f6c5f23c68b9b5f9650839dc)
 * the page displays "an email was sent".
 * On the /console/account/changePassword page:
   * server-side check that token is valid (postgresql storage). If not, HTTP 400.
   * two fields ask for new password (client-side check for equality)
   * on form submission:
     * check that token is valid. If not, HTTP 400.
     * the old password is discarded and replaced with the new one
     * the new page shows "password updated".

If the given email does not match one from the LDAP:
 * nothing happens server side
 * the page displays "an email was sent" (even if no email was sent).

### Create account

The page shows a form with typical fields: name, org, title, email, phone nb, details.
The user will be able to pick a **strong** password (must have at least one of: special char, letters and numbers).
Password field will be repeated 2 times (client-side check for equality).

There's also a captcha (for instance based on http://www.google.com/recaptcha) to prevent batch form submissions.

Once submitted, the form disappears and a (configurable) message says something like "Your request has been submitted and should be processed in the next hours. Watch your email."

What happens here ?
 * Depending on a "MODERATED_SIGNUP" config option, new users will be recorded in the LDAP and affected to :
   * the PENDING role if MODERATED_SIGNUP = true. An admin will then be able to move them to USERS role.
   * the USERS role if MODERATED_SIGNUP = false.
 * An email will be sent to one email address (configurable), saying that new users need an account.

### Edit user details

Two pages:
 * First one (default one): users should be able to edit a subset of LDAP fields, namely: sn, givenName, phone, facsimile, o, title, postalAddress (**not mail**). On this page, there will be a link to the userPassword change page.
 * userPassword change UI: will display 3 fields. The first one is the current user password, the two other ones are for the new one. If the two latest fields do not match (client-side check), the user won't be able to submit the form and the "new password mismatch" message will be displayed. If the current password is wrong (server side check), the form will be redisplayed with clean fields, and a message will display "invalid password".


### System Requirements

 * LDAP Server
 * Postgresql

For the web container: Tomcat 7, or Maven Jetty (no need to install)


### Get a pair of ReCaptcha keys

By default, geOrchestra uses global keys.

To fight spam robots, it may be safer to get a proper pair of keys for your site. Go to https://www.google.com/recaptcha/admin/create and create a pair of private/public keys. Quoting the [documentation](https://developers.google.com/recaptcha/intro), *unless you select the "global key" option, the keys are unique to your domain and sub-domains.*

Once created, set the following `console` parameters with the value of the keys:
* `privateKey`
* `publicKey`

See [the configuration guide](../config/README.md) for details on how to configure these two parameters.

### Set of required fields

The console configuration, in the config module, contains a `requiredFields` parameter that defaults to `firstName,surname,email,uid,password,confirmPassword`. Adding other fields, separated by commas, will make them mandatory in new account and edit forms. Note that this parameter only affects public UI.

For example, to impose the "Organisation" and "Title" fields to be not empty, set the following parameter in console config

```
requiredFields=firstName,surname,email,uid,password,confirmPassword,org,title
```

The possible values are: `firstName`, `surname`, `phone`, `facsimile`, `org`, `title`, `description`, `postalAddress`.

Note that email, uid, password and confirmPassword are always required.

### Build

Build:

```
mvn install -Dmaven.test.skip=true
```

Create the eclipse project

```
mvn eclipse:eclipse
```


### Manual Testing

Testing purpose:

 * deploy in Tomcat7
 * Then add the following url in your Internet navigator:
   http://localhost:8286/console/manager/

Alternatively, run with jetty:

* the *first* time, you need to previously compile console and all its dependencies

  ```
  $ mvn -Dmaven.test.skip=true -Ptemplate -P-all,console install;
  ```

* then, each time you want to test a change in the configuration or the console module:

  ```
  $ cd config
  $ mvn -Ptemplate install
  $ cd ../console
  $ mvn -Dmaven.test.skip=true -Ptemplate jetty:run
  ```
 * Then point your navigator to the following address :
   http://localhost:8286/console/manager/

 Running console with jetty will change web server port to *8286* (in order to integrate with others georchestra
 instance : CAS, security proxy, ...)

### Integration Testing

> TL;DR: run integration tests with `mvn verify -Pit`. The `it` maven profile enables the tests and starts the `ldap` and `database` docker containers at the `pre-integration-test` phase and kills them on `post-integration-test`.


While `mvn test` will run the unit tests but not the integration tests, `mvn verify` (or any goal past that, like `mvn install`) is used to run (also) integration tests.

A convenient `skipUT` property was added to allow ignoring the unit tests and to run only the integration tests. e.g.:
`mvn -DskipUT=true verify`. The standard `-DskipTests` flag can't be used becuase it will make maven also ignore the integration tests.

Integration test sources and resources are under `src/it/java` and `src/it/resources` respectively, and follow the convention of calling test classes `**IT.java`.

Running integration tests and require connecting to geOrchestra's LDAP and Postgres instances.

In order to automate these external resources, the [docker-maven-plugin](fabric8io/docker-maven-plugin) is used, which engages in the regular maven build lifecycle, launching the `georchestra/ldap` and `georchestra/database` docker images on the `pre-integration-test` phase and shuting them down in `post-integration-test`.

Integration tests are run after `test` and `package`, and consists of the `pre-integration-test` -> `integration-test` -> `post-integration-test` -> `verify` phases.

**Do not** run `mvn integration-test` directly as it won't have a chance to properly shut down the external resources (the docker containers in this case).

See the `pom.xml` file to check how the `docker-maven-plugin` is configured. It essentially launches the two mentioned containers and uses dynamic port mapping on port `389` for `georchestra/ldap` and port `5432` for `georchestra/database`. These mapped ports are then exposed by the `maven-failsafe-plugin` (the one used to run integration tests) as environment variables `ldap_port` and `psql_port` to the test JVM, which in turn are picked up by Spring while resolving `src/it/resources/console-it.properties` property source:

```
ldapUrl=ldap://localhost:${ldap_port}
psql.url=jdbc:postgresql://localhost:${psql_port}/georchestra
```

#### Running from an IDE while developing

Whilre writing integration tests, it could be cumbersome to wait for the launch and shut down of test containers.

In this case it's better to have `georchestra/ldap` and `georchestra/database` containers already running and instructing the test run to use them directly.

To do so launch them externally mapping the ports 389 and 5432 as appropriate,  and create a launch configuration for the test class being working on, and set the `-Dpsql_port=<db mapped port> -Dldap_port=<ldap mapped port>`.

When the test case is finished, make sure to run `mvn verify` to check it works properly within the maven build cycle.

Finally, when writing integration tests, make sure they're self contained and would not be affected by any existing data in the external resources.

### Protected Users

You can specify several user accounts that you want to protect against deletion or modification. For this purpose, you
have 'listOfprotectedUsers' property. This property holds a comma separated list of uid corresponding to users accounts
that should be protected.

Default value is : 'geoserver_privileged_user' (which is a privileged user, internally used) but you can override this
in config template with key : "protectedUserList"

Example :

    protectedUserList=geoserver_privileged_user,hidden_admin_user,hidden_admin_user_trash,hidden_admin_user_backup

(Note that there is no space around comma !)

This will add following users to default protected list of users :
  * hidden_admin_user
  * hidden_admin_user_trash
  * hidden_admin_user_backup

So final list of protected users will be :
  * geoserver_privileged_user
  * hidden_admin_user
  * hidden_admin_user_trash
  * hidden_admin_user_backup

Console UI
----------

The console UI will be available at /console to users having the SUPERUSER role.

See the wireframe in the current folder.

### Center pane

Dedicated to users:
 * a toolbar with a "new user" button, and a "selected users" menu item (disabled when no user is selected)
 * a scrollable grid, with a checkbox selection model, showing a subset of user attributes
 * on grid item double click, a window pops up (same as "new user" window), which allows user details editing (password change not allowed)

### New User/Edit User window

Featuring:
 * a form for user details,
 * a tree view of roles the user belongs to, with a checkbox selection model to edit.

When creating a new user (and only in this case), a **strong** password will be generated and sent to the new user by email.


#### Left pane

Dedicated to roles:
 * tree view of roles, with intermediate nodes for role types (GN_*, EL_*, ...) - role types should be configurable
 * ability to filter users list by one role (on role name click)
 * button to add a new role
 * button to remove a role (users will **not** be deleted)



Notes
-----

All emails sent by the application should be configurable by the way of templates, as for extractorapp.

The application should be able to find roles and users by the way of filters such as the ones used by the cas (have a look at the [cas maven filters](../config/defaults/cas-server-webapp/maven.filter#L4) and defined by the way of the variables shared.ldap.userSearchBaseDN and shared.ldap.roleSearchBaseDN defined in [config/shared.maven.filters](../config/shared.maven.filters#L10)

The userPassword LDAP field should be SSHA encrypted on creation/update.
