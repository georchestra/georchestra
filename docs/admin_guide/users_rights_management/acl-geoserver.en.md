# GeoServer 

## Managing the access to layers and services using geOrchestra roles

TODO: this is a draft. Check and review everything

GeoServer natively supports a very similar system for its security policies, relying on users, groups and roles.  
Please read the [GeoServer's documentation about Security management](https://docs.geoserver.org/latest/en/user/security/index.html#security) for reference.

With geOrchestra, on GeoServer, users are matched against the LDAP registry and will be available, but you cannot set access rules based on user. You have to rely on roles.

The roles from the geOrchestra console will not be synchronized automatically in the roles list. You will have to create corresponding roles in GeoServer for the role-matching to work. Compared to the console's role names, they will have to be prefixed by `ROLE_`.

Then you can use those roles in the security policy rules.

## Example
*I want, for my `psc` workspace, to grant people matching the `GS_PSC` role to access to the administration web UI.*

1. I create the `GS_PSC` role in the geOrchestra console, see 
2. In GeoServer, I create a `ROLE_GS_PSC` role
3. Still in GeoServer, in security->data policies, I add a rule `psc.*.a` and give it to `ROLE_GS_PSC`
4. 

TODO: add screenshots