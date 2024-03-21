# Setting up CAS6

CAS6, as the former version, is a regular spring-boot based webapp which can be deployed into
a servlet container. Though, Java11 is now mandatory to make it run. If you followed the
current installation guide, make sure that the `tomcat-proxycas` is launched with a Java 11
runtime (see [tomcat.md](./tomcat.md)).

## Environment variables

The following environment variables have been added to the servlet container running CAS
(`/etc/default/tomcat-proxycas` if you followed the same convention as this guide):

```
	-DCAS_BANNER_SKIP=true \
	-Dcas.standalone.configurationDirectory=/etc/georchestra/cas/config  \
```

## Configuration

The geOrchestra  datadir in regards to the new version of CAS evolved slightly,
see [this commit](https://github.com/georchestra/datadir/commit/b1391ec69f4b60ea727e9c6623b419837323fa47)
from the official repository for more details.

## Oauth2

geOrchestra's CAS6 integrates an optional plugin by default, allowing CAS to act
as an Oauth2 provider. You can configure the client applications you would like to use against the geOrchestra CAS server by editing
[this file from the datadir](https://github.com/georchestra/datadir/blob/362b3d97d2d10c449889f2efd70c316c4ae45b71/cas/services/oauth2-2001.json).

Once configured, one can use the following `spring-boot` oauth2 configuration to consume our CAS instance as an
oauth2 endpoint:

```
spring:
  security:
    oauth2:
      client:
        registration:
          cas-oauth2:
            client-id: oauth2
            client-secret: oauth2
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/cas-oauth2"
        provider:
          cas-oauth2:
            authorization-uri: https://url.to/cas/oauth2.0/authorize
            token-uri: https://url.to/cas/oauth2.0/accessToken
            user-info-uri: https://url.to/cas/oauth2.0/profile
            userNameAttribute: id
```
