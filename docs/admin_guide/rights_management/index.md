# Managing global access rights in geOrchestra

Please read first the section of the admin guide called 
 "[Managing users and access rights in geOrchestra](../users_management/index.md)" to understand how geOrchestra uses the concepts of user, organization and roles.

 Some applications can leverage new roles automatically or from the graphical user interface. Those are documented in the admin guide: [Configure an application to use a new role](../users_management/roles.md#3-configure-an-application-to-use-a-new-role)

 TODO: use an anchor to open the proper paragraph

 Other applications can leverage new roles, but require access to the configuration files. Those belong to sysadmin users:

- [Security-proxy](acl-sp.md)
- [Gateway](acl-gateway.md)

## Behind the scenes, how does it work ?

Users, organizations and roles are typically stored in the LDAP instance of your platform.

Some modules, like GeoNetwork, synchronize this information, on a regular basis, with its own internal database. 

When you are logged in the platform, the Single Sign-On (SSO) system remembers it and provides the downstream modules, in each http request, with the information of who you are, what organization you belong to and which roles you have been given. Then the access management is delegated to the downstream module (GeoNetwork, GeoServer etc) according to their own logic.

