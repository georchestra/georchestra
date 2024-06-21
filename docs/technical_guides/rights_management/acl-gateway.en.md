# Filtering the access to a path, using roles

The security-proxy is able to limit the access to a given path only for people belonging to a set of roles.
This is configured in the geOrchestra datadir, in gateway/gateway.yaml

The `services` block declares, for each service (application) a list of path (`intercept-url`) and an associated list of roles allowed to access them (`allowed-roles`)

Example: 
```yaml
     import:
        target: ${georchestra.gateway.services.import.target}
        access-rules:
        - intercept-url: /import/**
          anonymous: false
          allowed-roles: SUPERUSER,IMPORT
```
