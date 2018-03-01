# security-proxy

The security-proxy (aka SP) belongs to geOrchestra core, since it is the component which :
 * handles user sessions
 * routes requests to webapps (except CAS)
 
The behavior is controlled by the files from the `<datadir_root>/security-proxy` folder, which can be found [here](https://github.com/georchestra/datadir/tree/master/security-proxy)

## SP-trust-SP feature

This feature is rather confidential, since it involves two SP instances, while a standard geOrchestra only requires one. It may prove useful in some corner cases...

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
