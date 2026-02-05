# Security-Proxy

## Filtering the access to a path, using roles

The security-proxy is able to limit the access to a given path only for people belonging to a set of roles.
This is configured in the geOrchestra datadir, in security-proxy/security-mappings.xml.

Each line takes 2 parameters:
- `pattern` is a path or a regular expression matching some paths,
- `access` is a role or a list of roles that are granted access.

The lines are checked in their order of appearance. This has some importance since, for instance, the last line defines access to `.*` (all paths).

Some sample lines : 
```xml
  <intercept-url pattern="/console/manager/public/.*" access="IS_AUTHENTICATED_ANONYMOUSLY" />
  <intercept-url pattern="/console/manager/.*" access="ROLE_SUPERUSER,ROLE_ORGADMIN" />
  ...
  <intercept-url pattern="/testPage" access="IS_AUTHENTICATED_FULLY" />
  ...
  <intercept-url pattern="/import/.*" access="ROLE_SUPERUSER,ROLE_IMPORT" />
  <intercept-url pattern=".*" access="IS_AUTHENTICATED_ANONYMOUSLY,ROLE_USER,ROLE_GN_EDITOR,ROLE_GN_REVIEWER,ROLE_GN_ADMIN,ROLE_ADMINISTRATOR,ROLE_SUPERUSER,ROLE_ORGADMIN" />
```

## Pattern examples

- `/console/manager/.*` is a regular expression. It configures the access to all the paths starting by `/console/manager/`
- `/console/account/new` is a simple path, configuring access to this exact same path, and only that
- `.*` is a regular expression matching everything

## Access: list of roles

You might have noticed a few weird things on the sample lines above:

- roles tags don't exactly match those defined in [the roles documentation](../../admin_guide/users_management/roles.md)
- there are some additional roles not listed in the above-mentioned doc.

### `ROLE_` prefix

The standard roles (ADMINISTRATOR, GN_ADMIN, IMPORT, etc) are prefixed with `ROLE_`. This is due to the way the Single Sign On (SSO) system passes along the list of roles, in the http headers.

### Meta-roles

There are some additional meta-roles:

- `IS_AUTHENTICATED_ANONYMOUSLY`: matches people that are not logged in
- `IS_AUTHENTICATED_FULLY`: matches people that are logged in