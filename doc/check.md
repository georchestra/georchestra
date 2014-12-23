# Checking your setup

## GeoServer

Check that this returns a well formed capabilities document filled with public layers:
```
curl "http://private:8380/geoserver/wms?SERVICE=WMS&REQUEST=GetCapabilities"
```

Now, we want to check the http headers authentication.
This should return a well formed capabilities document filled with public and protected layers:
```
curl -H "sec-user: CURL" -H "sec-roles: ROLE_ADMINISTRATOR" "http://private:8380/geoserver/wms?SERVICE=WMS&REQUEST=GetCapabilities"
```

## Proxy

We change the TCP port and use the "insecure" flag. This should return the public capabilities:
```
curl --insecure "https://private:8443/geoserver/wms?SERVICE=WMS&REQUEST=GetCapabilities"
```
If it's not and the previous steps did work, your security proxy does not work.  
First thing to check is the [proxy mapping](https://github.com/georchestra/template/blob/master/build_support/GenerateConfig.groovy) in your config.


Now testing the security proxy Basic Authentication.  
The following command shall return the public and protected layers:
```
curl --insecure --user "geoserver_privileged_user:password" "https://private:8443/geoserver/wms?SERVICE=WMS&REQUEST=GetCapabilities"
```

Several potential issues if it's not :
 * the security proxy can't query the LDAP,
 * geoserver_privileged_user is not a valid LDAP account,
 * geoserver_privileged_user does not belong to the ```ADMINISTRATOR``` LDAP group,
 * geoserver_privileged_user's password is incorrect.


## Apache

If all the above steps are OK, go to the public interface and query the WMS service using curl:
```
curl "http://mysdi.org/geoserver/wms?SERVICE=WMS&REQUEST=GetCapabilities"
```

Then...
```
curl --insecure --user "geoserver_privileged_user:password" "http://mysdi.org/geoserver/wms?SERVICE=WMS&REQUEST=GetCapabilities"
```

Finally, open QGIS and register the service with and without the privileged account.
