LDAPADMIN
=========

A webapp with a public interface and a private one, which allows to manage users and groups.

All strings and templates should take into account i18n (3 langs by default: en/es/fr)

Public UI
---------

The public UIs will be accessed:
 * from the CAS login page, by the way of text links: "lost password ?" and "create account".
 * from every SDI page, by clicking on the username: "Edit user details"

These pages should be light (no need to ship ExtJS).

### Lost Password

The page asks for user email.

If the given email matches one of the LDAP users:
 * an email is sent to this user with a unique https URL to reset his password (eg: /ldapadmin/public/changePassword?token=54f23f27f6c5f23c68b9b5f9650839dc)
 * the page displays "an email was sent".
 * On the /ldapadmin/public/changePassword page:
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

The page shows a form with typical fields: name, org, role, geographic area, email, phone nb, details. 
The user will be able to pick a **strong** password (must have at least one of: special char, letters and numbers). 
Password field will be repeated 2 times (client-side check for equality).

There's also a captcha (for instance based on http://www.google.com/recaptcha) to prevent batch form submissions.

Once submitted, the form disappears and a (configurable) message says something like "Your request has been submitted and should be processed in the next hours. Watch your email."

What happens here ? 
 * Depending on a "MODERATED_SIGNUP" config option, new users will be recorded in the LDAP and affected to :
   * the PENDING_USERS group if MODERATED_SIGNUP = true. An admin will then be able to move them to SV_USERS group.
   * the SV_USERS group if MODERATED_SIGNUP = false.
 * An email will be sent to one email address (configurable), saying that new users need an account.

### Edit user details

Two pages: 
 * First one (default one): users should be able to edit a subset of LDAP fields, namely: sn, givenName, o, title, postalAddress, postalCode, registeredAddress, postOfficeBox, physicalDeliveryOfficeName (**not mail**). On this page, there will be a link to the userPassword change page.
 * userPassword change UI: will display 3 fields. The first one is the current user password, the two other ones are for the new one. If the two latest fields do not match (client-side check), the user won't be able to submit the form and the "new password mismatch" message will be displayed. If the current password is wrong (server side check), the form will be redisplayed with clean fields, and a message will display "invalid password".


### System Requirements

 * WEB Containger (Tomcat 6)
 * LDAP Server
 * Postgresql


### Install Postgresql

To create the database use the following script:

```
[georchestra]/ldapadmin/ldapAdminDB.sql
```

Note: because this is a work in progress right now the the following postgresql parameters are not used. 
To configure the connection, for testing purpose, change the `UserTokenDao.getConnection()` method. For example:

```
this.databaseName = "postgres";
this.databaseUser = "admin";
this.databasePassword = "postgres";
```

### Install LDAP

The connection to the LDAP server is configurated in the following file:

```
[georchestra]/ldapadmin/src/main/webapp/WEB-INF/spring/webmvc-config.xml
```

For exemple:

```
<!-- LDAP connection -->
<bean id="contextSource" class="org.springframework.ldap.core.support.LdapContextSource">
  <property name="url" value="ldap://localhost:389" />
  <property name="base" value="dc=georchestra,dc=org" />
  <property name="userDn" value="cn=admin,dc=georchestra,dc=org" />
  <property name="password" value="secret" />
</bean>
```

If no LDAP server is installed, follow instructions at https://github.com/georchestra/georchestra/blob/master/INSTALL.md#ldap.
The LDAP server will be installed and an example directory database will be populated and accessible using the above default parameters.

### Build

Build:

```
../mvn install -Dmaven.test.skip=true
```

Create the eclipse project

```
../mvn eclipse:eclipse
```


### Run (Testing)

Testing purpose: 

 * deploy in Tomcat6
 * Then add the following url in your Internet navigator:
   http://localhost:8080/ldapadmin/public/

Alternatively, run with jetty:

```
../mvn -Dmaven.test.skip=true -Ptemplate jetty:run
```


Private UI
----------

The private UI will be available at /ldapadmin for members of the SV_ADMIN and ADMIN_USERS groups.

See the wireframe in the current folder.

### Center pane 

Dedicated to users:
 * a toolbar with a "new user" button, and a "selected users" menu item (disabled when no user is selected)
 * a scrollable grid, with a checkbox selection model, showing a subset of user attributes
 * on grid item double click, a window pops up (same as "new user" window), which allows user details editing (password change not allowed)

### New User/Edit User window

Featuring:
 * a form for user details,
 * a tree view of groups the user belongs to, with a checkbox selection model to edit. 

When creating a new user (and only in this case), a **strong** password will be generated and sent to the new user by email.


#### Left pane 

Dedicated to groups:
 * tree view of groups, with intermediate nodes for group types (SV_*, EL_*, ...) - group types should be configurable
 * ability to filter users list by one group (on group name click)
 * button to add a new group 
 * button to remove a group (users will **not** be deleted)


Members of the ADMIN_USERS group will have the same UI, but some buttons will not be shown (or disabled) : group add/remove, "selected users" > "add/remove from group"
They will have the right to create/read/update/delete users only from the same EL_* groups they belong to.

Notes
-----

All emails sent by the application should be configurable by the way of templates, as for extractorapp.

The application should be able to find groups and users by the way of filters such as the ones used by the cas (see https://github.com/georchestra/georchestra/blob/master/config/defaults/cas-server-webapp/maven.filter#L4) and defined by the way of the variables shared.ldap.userSearchBaseDN and shared.ldap.groupSearchBaseDN defined in https://github.com/georchestra/georchestra/blob/master/config/shared.maven.filters#L10

The userPassword LDAP field should be SSHA encrypted on creation/update.


