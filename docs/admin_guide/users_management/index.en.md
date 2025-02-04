# Managing users and access rights in geOrchestra

geOrchestra is composed of several modules, some of which have their own separate life (GeoNetwork, GeoServer, etc). As such the access rights management have their own logic in each of those apps.

geOrchestra tries to make its best in order to provide you a more unified experience: the console provides you with a user management interface extended by a system of *roles*, that are used to define which actions and rights you are allowed to. What each module will do of those roles is at least partially delegated to the modules' own logic. At least for now.

Users and access management relates to the following concepts: 

- [Users](users.md)
- [Organizations](organizations.md)
- [Roles](roles.md)
- [Rights management](../users_rights_management/index.md)


## Behind the scenes, how does it work ?

Users, organizations and roles are typically stored in the LDAP instance of your platform.

Some modules, like GeoNetwork, synchronize this information, on a regular basis, with its own internal database. 

When you are logged in the platform, the Single Sign-On (SSO) system remembers it and provides the downstream modules, in each http request, with the information of who you are, what organization you belong to and which roles you have been given. Then the access management is delegated to the downstream module (GeoNetwork, GeoServer etc) according to their own logic.
