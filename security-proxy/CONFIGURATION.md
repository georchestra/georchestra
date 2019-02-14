# Configuration of security-proxy

## Properties files

Configuration properties are loaded at runtime from the following properties files (or only part of them - see [GeorchestraConfiguration](https://github.com/georchestra/georchestra/blob/master/commons/src/main/java/org/georchestra/commons/configuration/GeorchestraConfiguration.java) class):

### Hardcoded default value

### Datadir common context property file

https://github.com/georchestra/datadir/blob/master/default.properties

### Datadir application context property file

https://github.com/georchestra/datadir/blob/18.06/security-proxy/security-proxy.properties

## Elements of security-proxy that can be configured

The security-proxy module is defined by the [web.xml](./src/main/webapp/WEB-INF/web.xml) Spring configuration file. This file includes two other XMl configurations files:

- [applicationContext-security.xml](src/main/webapp/WEB-INF/applicationContext-security.xml)
- [proxy-servlet.xml](src/main/webapp/WEB-INF/proxy-servlet.xml)

These two XML files contain various placeholders (`${...}`) that can be replaced by Server properties (like for example `georchestra.datadir`) or by properties defined in properties files (see `<context:property-placeholder>` tags). Moreover, they define beans that are created using Java classes. In these classes there are direct calls to the datadir properties files. In this page we list all the properties that can be configured by the user, and the priority order between the configuration files.

## proxy-servlet.xml

Here we describe how to configure the properties related to the [proxy-servlet.xml](./src/main/webapp/WEB-INF/proxy-servlet.xml) configuration file.

### `proxy` bean

The `proxy` bean is created at [proxy-servlet.xml](./src/main/webapp/WEB-INF/proxy-servlet.xml#L21-L61) using the [org.georchestra.security.Proxy](./src/main/java/org/georchestra/security/Proxy.java) class.

#### `publicUrl` property

The [`publicUrl` property](./src/main/java/org/georchestra/security/Proxy.java#L157) can be configured as follows:

| Priority | From                                                                                                                                                           | Code                                                                                        | Value                              | Comments |
| -------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------- | ---------------------------------- | -------- |
| Lowest   | [Default value](./src/main/java/org/georchestra/security/Proxy.java#L157)                                                                                      | `private String publicUrl = "https://georchestra.mydomain.org";`                       | `https://georchestra.mydomain.org` |          |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L22) / default placeholder value                                                                   | `<property name="publicUrl" value="${publicUrl:https://georchestra.mydomain.org}"/>`   | `https://georchestra.mydomain.org` |          |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L22) / [Datadir common context property file](#datadir-common-context-property-file)               | `<property name="publicUrl" value="${publicUrl:https://georchestra.mydomain.org}"/>`   | `https://georchestra.mydomain.org` |          |
| Highest  | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L22) / [Datadir application context property file](#datadir-application-context-property-file)     | `<property name="publicUrl" value="${publicUrl:https://georchestra.mydomain.org}"/>`   |                                    | Not set  |

#### `defaultTarget` property

The [`defaultTarget` property](./src/main/java/org/georchestra/security/Proxy.java#L156) can be configured as follows:

| Priority | From                                                                                                                                                           | Code                                                                  | Value      | Comments |
| -------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------- | ---------- | -------- |
| Lowest   | [Default value](./src/main/java/org/georchestra/security/Proxy.java#L156)                                                                                      | `private String defaultTarget;`                                       |            | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L25) / default placeholder value                                                                   | `<property name="defaultTarget" value="${defaultTarget:/header/}" />` | `/header/` |          |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L25) / [Datadir common context property file](#datadir-common-context-property-file)               | `<property name="defaultTarget" value="${defaultTarget:/header/}" />` |            | Not set  |
| Highest  | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L25) / [Datadir application context property file](#datadir-application-context-property-file)     | `<property name="defaultTarget" value="${defaultTarget:/header/}" />` | `/header/` |          |

#### `proxyPermissions` property

The [`proxyPermissions` property](./src/main/java/org/georchestra/security/Proxy.java#L168) can be configured as follows:

| Priority | From                                                                                                                                                                             | Code                                                                                                                                                                          | Value                                                                                                                       | Comments |
| -------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------- | -------- |
| Lowest   | [Default value](./src/main/java/org/georchestra/security/Proxy.java#L168)                                                                                                        | `private Permissions proxyPermissions = null;`                                                                                                                                | `null`                                                                                                                      |          |
|          | [Bean init-method](./src/main/java/org/georchestra/security/Proxy.java#L241-L254) / loading the file defined in `proxyPermissionsFile` property (hardcoded to `permissions.xml`) | `InputStream inStream = closer.register(classLoader.getResourceAsStream(proxyPermissionsFile));` / `setProxyPermissions(Permissions.Create(inStream));`                       | Content of [permissions.xml](./src/main/webapp/WEB-INF/classes/permissions.xml)                                             |          |
| Highest  | [Bean init-method](./src/main/java/org/georchestra/security/Proxy.java#L210-L227) / loading the `proxy-permissions.xml` file from the application context in datadir             | `File datadirPermissionsFile = new File(String.format("%s%s%s", datadirContext, File.separator, "proxy-permissions.xml"));` / `setProxyPermissions(Permissions.Create(fis));` | Content of [proxy-permissions.xml](https://github.com/georchestra/datadir/blob/master/security-proxy/proxy-permissions.xml) |          |

#### `httpClientTimeout` property

The [`httpClientTimeout` property](./src/main/java/org/georchestra/security/Proxy.java#L172) can be configured as follows:

| Priority | From                                                                                                                                                               | Code                                                                                                                                      | Value     | Comments |
| -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------- | --------- | -------- |
| Lowest   | [Default value](./src/main/java/org/georchestra/security/Proxy.java#L172)                                                                                          | `private Integer httpClientTimeout = 300000;`                                                                                             | `300000`  |          |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L27-L31) / default placeholder value                                                                   | `<property name="httpClientTimeout"><bean class="java.lang.Integer"><constructor-arg value="${http_client_timeout}" /></bean></property>` |           | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L27-L31) / [Datadir common context property file](#datadir-common-context-property-file)               | `<property name="httpClientTimeout"><bean class="java.lang.Integer"><constructor-arg value="${http_client_timeout}" /></bean></property>` |           | Not set  |
| Highest  | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L27-L31) / [Datadir application context property file](#datadir-application-context-property-file)     | `<property name="httpClientTimeout"><bean class="java.lang.Integer"><constructor-arg value="${http_client_timeout}" /></bean></property>` | `1200000` |          |

#### `targets` property

The [`targets` property](./src/main/java/org/georchestra/security/Proxy.java#L159) can be configured as follows:

| Priority | From                                                                                                                                                                      | Code                                                                                                                                                      | Value                                                                                                                                               | Comments |
| -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------- | -------- |
| Lowest   | [Default value](./src/main/java/org/georchestra/security/Proxy.java#L159)                                                                                                 | `private Map<String, String> targets = Collections.emptyMap();`                                                                                           | Empty map                                                                                                                                           |          |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L33-L37) / default placeholder value                                                                          | `<property name="targets"><map>${proxy.mapping}</map></property>`                                                                                         |                                                                                                                                                     | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L33-L37) / [Datadir common context property file](#datadir-common-context-property-file)                      | `<property name="targets"><map>${proxy.mapping}</map></property>`                                                                                         |                                                                                                                                                     | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L33-L37) / [Datadir application context property file](#datadir-application-context-property-file)            | `<property name="targets"><map>${proxy.mapping}</map></property>`                                                                                         |                                                                                                                                                     | Not set  |
| Highest  | [Bean init-method](./src/main/java/org/georchestra/security/Proxy.java#L203-L208) / loading the `targets-mapping.properties` file from the application context in datadir | `Properties pTargets = georchestraConfiguration.loadCustomPropertiesFile("targets-mapping");` / `this.targets.put(target, pTargets.getProperty(target));` | Content of [targets-mapping.properties](https://github.com/georchestra/datadir/blob/master/security-proxy/targets-mapping.properties) cast to a map |          |

### `headerManagementBean` bean

The `headerManagementBean` bean is created at [proxy-servlet.xml](./src/main/webapp/WEB-INF/proxy-servlet.xml#L62-L110) using the [org.georchestra.security.HeadersManagementStrategy](./src/main/java/org/georchestra/security/HeadersManagementStrategy.java) class.

#### `referer` property

In [HeadersManagementStrategy.java](./src/main/java/org/georchestra/security/HeadersManagementStrategy.java), the [`referer` property](./src/main/java/org/georchestra/security/HeadersManagementStrategy.java#L73) can be configured as follows:

| Priority | From                                                                                                                                                                                      | Code                                                             | Value                              | Comments |
| -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------- | ---------------------------------- | -------- |
| Lowest   | [Default value](./src/main/java/org/georchestra/security/HeadersManagementStrategy.java#L73)                                                                                              | `private String referer = null;`                                 | `null`                             |          |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L68) / default placeholder value                                                                                              | `<property name="referer" value="${publicUrl}/"/>`               |                                    | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L68) / [Datadir common context property file](#datadir-common-context-property-file)                                          | `<property name="referer" value="${publicUrl}/"/>`               | `https://georchestra.mydomain.org` |          |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L68) / [Datadir application context property file](#datadir-application-context-property-file)                                | `<property name="referer" value="${publicUrl}/"/>`               |                                    | Not set  |
|          | [Bean init-method](./src/main/java/org/georchestra/security/HeadersManagementStrategy.java#L84) / [Datadir common context property file](#datadir-common-context-property-file)           | `referer = georchestraConfiguration.getProperty("publicUrl");`   | `https://georchestra.mydomain.org` |          |
| Highest  | [Bean init-method](./src/main/java/org/georchestra/security/HeadersManagementStrategy.java#L84) / [Datadir application context property file](#datadir-application-context-property-file) | `referer = georchestraConfiguration.getProperty("publicUrl");`   |                                    | Not set  |

#### `headerProviders` property

In [HeadersManagementStrategy.java](./src/main/java/org/georchestra/security/HeadersManagementStrategy.java), the [`headerProviders` property](./src/main/java/org/georchestra/security/HeadersManagementStrategy.java#L71) is a collection of "header providers". Some of them are hardcoded, other may be configured.

One provider is created with a bean from the [`org.georchestra.security.ImpersonateUserRequestHeaderProvider` class](./src/main/java/org/georchestra/security/ImpersonateUserRequestHeaderProvider.java), and its [`trustedUsers` property](src/main/java/org/georchestra/security/ImpersonateUserRequestHeaderProvider.java#L45) can be configured as follows:

| Priority | From                                                                                                                                                                                                 | Code                                                                                            | Value                                                                                                                                                         | Comments |
| -------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- |
| Lowest   | [Default value](./src/main/java/org/georchestra/security/ImpersonateUserRequestHeaderProvider.java#L45)                                                                                              | `private List<String> trustedUsers = new ArrayList<String>();`                                  | Empty String array list                                                                                                                                       |          |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L74-78) / default placeholder value                                                                                                      | `<property name="trustedUsers"><list><value>${privileged_admin_name}</value></list></property>` |                                                                                                                                                               | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L74-78) / [Datadir common context property file](#datadir-common-context-property-file)                                                  | `<property name="trustedUsers"><list><value>${privileged_admin_name}</value></list></property>` |                                                                                                                                                               | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L74-78) / [Datadir application context property file](#datadir-application-context-property-file)                                        | `<property name="trustedUsers"><list><value>${privileged_admin_name}</value></list></property>` |                                                                                                                                                               | Not set  |
| Highest  | [Bean init-method](./src/main/java/org/georchestra/security/ImpersonateUserRequestHeaderProvider.java#L52-L59) / loading the `trusted-users.properties` file from the application context in datadir | `Properties htTrustUsrs = georchestraConfiguration.loadCustomPropertiesFile("trusted-users");`  | Content of [trusted-users.properties](https://github.com/georchestra/datadir/blob/master/security-proxy/trusted-users.properties) cast to a String array list |          |

Another provider is created with a bean from the [`org.georchestra.security.LdapUserDetailsRequestHeaderProvider` class](./src/main/java/org/georchestra/security/LdapUserDetailsRequestHeaderProvider.java), and two of its properties can be configured

The first property is the [`orgSearchBaseDN` property](./src/main/java/org/georchestra/security/LdapUserDetailsRequestHeaderProvider.java#L64) and it can be configured as follows:

| Priority | From                                                                                                                                                           | Code                                                      | Value     | Comments |
| -------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------- | --------- | -------- |
| Lowest   | [Default value](./src/main/java/org/georchestra/security/LdapUserDetailsRequestHeaderProvider.java#L64)                                                        | `private String orgSearchBaseDN;`                         |           | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L82) / default placeholder value                                                                   | `<constructor-arg index="1" value="${orgSearchBaseDN}"/>` |           | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L82) / [Datadir common context property file](#datadir-common-context-property-file)               | `<constructor-arg index="1" value="${orgSearchBaseDN}"/>` |           | Not set  |
| Highest  | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L82) / [Datadir application context property file](#datadir-application-context-property-file)     | `<constructor-arg index="1" value="${orgSearchBaseDN}"/>` | `ou=orgs` |          |

The second property is the [`_headerMapping` property](./src/main/java/org/georchestra/security/LdapUserDetailsRequestHeaderProvider.java#L62) and it can be configured as follows:

| Priority | From                                                                                                                                                                                                   | Code                                                                                       | Value                                                                                                                                                                 | Comments |
| -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- |
| Lowest   | [Default value](./src/main/java/org/georchestra/security/LdapUserDetailsRequestHeaderProvider.java#L62)                                                                                                | `private Map<String, String> _headerMapping;`                                              |                                                                                                                                                                       | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L83-87) / default placeholder value                                                                                                        | `<constructor-arg index="2"><map>${header.mapping}</map></constructor-arg>`                |                                                                                                                                                                       | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L83-87) / [Datadir common context property file](#datadir-common-context-property-file)                                                    | `<constructor-arg index="2"><map>${header.mapping}</map></constructor-arg>`                |                                                                                                                                                                       | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L83-87) / [Datadir application context property file](#datadir-application-context-property-file)                                          | `<constructor-arg index="2"><map>${header.mapping}</map></constructor-arg>`                |                                                                                                                                                                       | Not set  |
| Highest  | [Bean init-method](./src/main/java/org/georchestra/security/LdapUserDetailsRequestHeaderProvider.java#L84-L88) / loading the `headers-mapping.properties` file from the application context in datadir | `Properties pHmap = georchestraConfiguration.loadCustomPropertiesFile("headers-mapping");` | Content of [headers-mapping.properties](https://github.com/georchestra/datadir/blob/master/security-proxy/headers-mapping.properties) cast to a `<String,String>` map |          |

#### `filters` property

In [HeadersManagementStrategy.java](./src/main/java/org/georchestra/security/HeadersManagementStrategy.java), the [`filters` property](./src/main/java/org/georchestra/security/HeadersManagementStrategy.java#L72) is a collection of "header filters". One of them is hardcoded, the other one may be configured.

The second filter is created with a bean from the [`org.georchestra.security.RemoveXForwardedHeaders` class](./src/main/java/org/georchestra/security/RemoveXForwardedHeaders.java), and its [`includes` property](src/main/java/org/georchestra/security/RemoveXForwardedHeaders.java#L52) can be configured as follows:

| Priority | From                                                                                                                                                                                                 | Code                                                                                                                | Value                                                                                                                                                                              | Comments |
| -------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- |
| Lowest   | [Default value](./src/main/java/org/georchestra/security/RemoveXForwardedHeaders.java#L52)                                                                                                           | `private List<Pattern> includes = Lists.newArrayList();`                                                            | Empty list                                                                                                                                                                         |          |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L96-L101) / default placeholder value                                                                                                    | `<property name="includes"><list>${remove.xforwarded.headers}</list></property>`                                    |                                                                                                                                                                                    | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L96-L101) / [Datadir common context property file](#datadir-common-context-property-file)                                                | `<property name="includes"><list>${remove.xforwarded.headers}</list></property>`                                    |                                                                                                                                                                                    | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L96-L101) / [Datadir application context property file](#datadir-application-context-property-file)                                      | `<property name="includes"><list>${remove.xforwarded.headers}</list></property>`                                    |                                                                                                                                                                                    | Not set  |
| Highest  | [Bean init-method](./src/main/java/org/georchestra/security/RemoveXForwardedHeaders.java#L66-L73) / loading the `removed-xforwarded-headers.properties` file from the application context in datadir | `Properties removedHeadersProps = georchestraConfiguration.loadCustomPropertiesFile("removed-xforwarded-headers");` | Content of [removed-xforwarded-headers.properties](https://github.com/georchestra/datadir/blob/master/security-proxy/removed-xforwarded-headers.properties) cast to a Pattern list |          |

## applicationContext-security.xml

Here we describe how to configure the properties related to the [applicationContext-security.xml](./src/main/webapp/WEB-INF/applicationContext-security.xml) configuration file.

### `s:http` configuration

The `s:http` configuration in [applicationContext-security.xml](./src/main/webapp/WEB-INF/applicationContext-security.xml#L25-L37) uses two placeholders.

#### `realm` attribute

The [`realm` attribute](./src/main/webapp/WEB-INF/applicationContext-security.xml#L25) can be configured as follows:

| Priority | From                                                                                                                                                                                | Code                                                                                                                                 | Value         | Comments |
| -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------ | ------------- | -------- |
| Lowest   | [s:http configuration](./src/main/webapp/WEB-INF/applicationContext-security.xml#L25) / default placeholder value                                                                   | `<s:http entry-point-ref="casProcessingFilterEntryPoint" request-matcher="regex" realm="${realmName}" disable-url-rewriting="true">` |               | Not set  |
|          | [s:http configuration](./src/main/webapp/WEB-INF/applicationContext-security.xml#L25) / [Datadir common context property file](#datadir-common-context-property-file)               | `<s:http entry-point-ref="casProcessingFilterEntryPoint" request-matcher="regex" realm="${realmName}" disable-url-rewriting="true">` |               | Not set  |
| Highest  | [s:http configuration](./src/main/webapp/WEB-INF/applicationContext-security.xml#L25) / [Datadir application context property file](#datadir-application-context-property-file)     | `<s:http entry-point-ref="casProcessingFilterEntryPoint" request-matcher="regex" realm="${realmName}" disable-url-rewriting="true">` | `georchestra` |          |

#### `logout-success-url` attribute

The [`logout-success-url` attribute](./src/main/webapp/WEB-INF/applicationContext-security.xml#L35) of the `logout` element can be configured as follows:

| Priority | From                                                                                                                                                                                | Code                                                      | Value                                                         | Comments |
| -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------- | ------------------------------------------------------------- | -------- |
| Lowest   | [s:http XSD](http://www.springframework.org/schema/security/spring-security.xsd) / default value                                                                                |                                                           | `<form-login-login-page>/?logout`                             |          |
|          | [s:http configuration](./src/main/webapp/WEB-INF/applicationContext-security.xml#L35) / default placeholder value                                                                   | `<s:logout logout-success-url="${logout-success-url}" />` |                                                               | Not set  |
|          | [s:http configuration](./src/main/webapp/WEB-INF/applicationContext-security.xml#L35) / [Datadir common context property file](#datadir-common-context-property-file)               | `<s:logout logout-success-url="${logout-success-url}" />` |                                                               | Not set  |
| Highest  | [s:http configuration](./src/main/webapp/WEB-INF/applicationContext-security.xml#L35) / [Datadir application context property file](#datadir-application-context-property-file)     | `<s:logout logout-success-url="${logout-success-url}" />` | `https://georchestra.mydomain.org/cas/logout?fromgeorchestra` |          |

### `filterSecurityInterceptor` bean

The `filterSecurityInterceptor` bean is created at [applicationContext-security.xml](./src/main/webapp/WEB-INF/applicationContext-security.xml#L39-L79) using the org.springframework.security.web.access.intercept.FilterSecurityInterceptor class.

#### `securityMetadataSource` property

The [`securityMetadataSource` property](./src/main/webapp/WEB-INF/applicationContext-security.xml#L51-L78) is created with a bean from the [`org.georchestra.security.SecurityProxyMetadataSource` class](./src/main/java/org/georchestra/security/SecurityProxyMetadataSource.java), and its [`requestMap` property](src/main/java/org/georchestra/security/SecurityProxyMetadataSource.java#L64) can be configured as follows:

| Priority | From                                                                                                                                                                                      | Code                                                                                                                                      | Value                                                                                                                                | Comments |
| -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------ | -------- |
| Lowest   | [Default value](./src/main/java/org/georchestra/security/SecurityProxyMetadataSource.java#L64)                                                                                            | `private Map<RequestMatcher, Collection<ConfigAttribute>> requestMap = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();` | Empty LinkedHashMap                                                                                                                  |          |
|          | [Bean creation](./src/main/webapp/WEB-INF/proxy-servlet.xml#L74-78) / hardcoded property at bean creation                                                                                 | see [the hardcoded map](./src/main/webapp/WEB-INF/applicationContext-security.xml#L54-L76)                                                | [hardcoded map](./src/main/webapp/WEB-INF/applicationContext-security.xml#L54-L76)                                                   |          |
| Highest  | [Bean init-method](./src/main/java/org/georchestra/security/SecurityProxyMetadataSource.java#L75-L101) / loading the `security-mappings.xml` file from the application context in datadir | `File securityMappings = new File(contextDatadir, "security-mappings.xml");` / `loadSecurityRules(securityMappings);`                     | Content of [security-mappings.xml](https://github.com/georchestra/datadir/blob/master/security-mappings.xml) cast to a LinkedHashMap |          |

### `basicAuthenticationEntryPoint` bean

The `basicAuthenticationEntryPoint` bean is created at [applicationContext-security.xml](./src/main/webapp/WEB-INF/applicationContext-security.xml#L83-L86) using the org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint class.

#### `realmName` property

The [`realmName` property](./src/main/webapp/WEB-INF/applicationContext-security.xml#L85) can be configured as follows:

| Priority | From                                                                                                                                                                         | Code                                                 | Value         | Comments |
| -------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------- | ------------- | -------- |
| Lowest   | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L85) / default placeholder value                                                                   | `<property name="realmName" value="${realmName}" />` |               | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L85) / [Datadir common context property file](#datadir-common-context-property-file)               | `<property name="realmName" value="${realmName}" />` |               | Not set  |
| Highest  | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L85) / [Datadir application context property file](#datadir-application-context-property-file)     | `<property name="realmName" value="${realmName}" />` | `georchestra` |          |

### `basicAuthChallengeByUserAgent` bean

The `basicAuthChallengeByUserAgent` bean is created at [applicationContext-security.xml](./src/main/webapp/WEB-INF/applicationContext-security.xml#L88-L102) using the [org.georchestra.security.BasicAuthChallengeByUserAgent](./src/main/java/org/georchestra/security/BasicAuthChallengeByUserAgent.java) class.

#### `_userAgents` property

The [`_userAgents` property](./src/main/java/org/georchestra/security/BasicAuthChallengeByUserAgent.java#L65) can be configured as follows:

| Priority | From                                                                                                                                                                                         | Code                                                                                                                           | Value                                                                                                                                              | Comments |
| -------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------- | -------- |
| Lowest   | [Default value](./src/main/java/org/georchestra/security/BasicAuthChallengeByUserAgent.java#L65)                                                                                             | `private final List<Pattern> _userAgents = new ArrayList<Pattern>();`                                                          | Empty ArrayList                                                                                                                                    |          |
| Highest  | [Bean init-method](./src/main/java/org/georchestra/security/BasicAuthChallengeByUserAgent.java#L71-L100) / loading the `user-agents.properties` file from the application context in datadir | `fisProp = new FileInputStream(new File(contextDatadir, "user-agents.properties"));` / `_userAgents.add(Pattern.compile(ua));` | Content of [user-agents.properties](https://github.com/georchestra/datadir/blob/master/security-proxy/user-agents.properties) cast to an ArrayList |          |

### `trustAnotherProxy` bean

The `trustAnotherProxy` bean is created at [applicationContext-security.xml](./src/main/webapp/WEB-INF/applicationContext-security.xml#L104-L107) using the [org.georchestra.security.ProxyTrustAnotherProxy](./src/main/java/org/georchestra/security/ProxyTrustAnotherProxy.java) class.

#### `trustedProxies` property

The [`trustedProxies` property](./src/main/java/org/georchestra/security/ProxyTrustAnotherProxy.java#L22) can be configured as follows:

| Priority | From                                                                                                                                                                                      | Code                                                                                                                              | Value                  | Comments |
| -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- | ---------------------- | -------- |
| Lowest   | [Default value](./src/main/java/org/georchestra/security/ProxyTrustAnotherProxy.java#L22)                                                                                                 | `private Set<InetAddress> trustedProxies = new HashSet<InetAddress>();`                                                           | Empty HashSet          |          |
|          | [Bean init-method](./src/main/java/org/georchestra/security/HeadersManagementStrategy.java#L84) / [Datadir common context property file](#datadir-common-context-property-file)           | `private static String CONFIG_KEY = "trustedProxy";` / `String rawProxyValue = georchestraConfiguration.getProperty(CONFIG_KEY);` |                        | Not set  |
| Highest  | [Bean init-method](./src/main/java/org/georchestra/security/HeadersManagementStrategy.java#L84) / [Datadir application context property file](#datadir-application-context-property-file) | `private static String CONFIG_KEY = "trustedProxy";` / `String rawProxyValue = georchestraConfiguration.getProperty(CONFIG_KEY);` | `127.0.0.1, localhost` |          |

### `contextSource` bean

The `contextSource` bean is created at [applicationContext-security.xml](./src/main/webapp/WEB-INF/applicationContext-security.xml#L130-L144) using the org.springframework.security.ldap.DefaultSpringSecurityContextSource class.

#### `providerUrl` constructor argument

The [`providerUrl` constructor argument](./src/main/webapp/WEB-INF/applicationContext-security.xml#L131) can be configured as follows:

| Priority | From                                                                                                                                                                          | Code                                              | Value                                    | Comments |
| -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------- | ---------------------------------------- | -------- |
| Lowest   | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L131) / default placeholder value                                                                   | `<constructor-arg value="${ldapUrl}/${baseDN}"/>` |                                          | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L131) / [Datadir common context property file](#datadir-common-context-property-file)               | `<constructor-arg value="${ldapUrl}/${baseDN}"/>` |                                          | Not set  |
| Highest  | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L131) / [Datadir application context property file](#datadir-application-context-property-file)     | `<constructor-arg value="${ldapUrl}/${baseDN}"/>` | `ldap://localhost/dc=georchestra,dc=org` |          |

#### `userDn` property

The [`userDn` property](./src/main/webapp/WEB-INF/applicationContext-security.xml#L132) can be configured as follows:

| Priority | From                                                                                                                                                                          | Code                                                | Value                            | Comments |
| -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------- | -------------------------------- | -------- |
| Lowest   | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L132) / default placeholder value                                                                   | `<property name="userDn" value="${ldapAdminDn}" />` |                                  | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L132) / [Datadir common context property file](#datadir-common-context-property-file)               | `<property name="userDn" value="${ldapAdminDn}" />` |                                  | Not set  |
| Highest  | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L132) / [Datadir application context property file](#datadir-application-context-property-file)     | `<property name="userDn" value="${ldapAdminDn}" />` | `cn=admin,dc=georchestra,dc=org` |          |

#### `password` property

The [`password` property](./src/main/webapp/WEB-INF/applicationContext-security.xml#L133) can be configured as follows:

| Priority | From                                                                                                                                                                          | Code                                                          | Value    | Comments |
| -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------- | -------- | -------- |
| Lowest   | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L133) / default placeholder value                                                                   | `<property name="password" value="${ldap.admin.password}" />` |          | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L133) / [Datadir common context property file](#datadir-common-context-property-file)               | `<property name="password" value="${ldap.admin.password}" />` |          | Not set  |
| Highest  | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L133) / [Datadir application context property file](#datadir-application-context-property-file)     | `<property name="password" value="${ldap.admin.password}" />` | `secret` |          |

### `ldapUserSearch` bean

The `ldapUserSearch` bean is created at [applicationContext-security.xml](./src/main/webapp/WEB-INF/applicationContext-security.xml#L146-L150) using the org.springframework.security.ldap.search.FilterBasedLdapUserSearch class.

#### `searchBase` constructor argument

The [`searchBase` constructor argument](./src/main/webapp/WEB-INF/applicationContext-security.xml#L147) can be configured as follows:

| Priority | From                                                                                                                                                                          | Code                                                       | Value      | Comments |
| -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------- | ---------- | -------- |
| Lowest   | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L147) / default placeholder value                                                                   | `<constructor-arg index="0" value="${userSearchBaseDN}"/>` |            | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L147) / [Datadir common context property file](#datadir-common-context-property-file)               | `<constructor-arg index="0" value="${userSearchBaseDN}"/>` |            | Not set  |
| Highest  | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L147) / [Datadir application context property file](#datadir-application-context-property-file)     | `<constructor-arg index="0" value="${userSearchBaseDN}"/>` | `ou=users` |          |

#### `searchFilter` constructor argument

The [`searchFilter` constructor argument](./src/main/webapp/WEB-INF/applicationContext-security.xml#L148) can be configured as follows:

| Priority | From                                                                                                                                                                          | Code                                                       | Value       | Comments |
| -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------- | ----------- | -------- |
| Lowest   | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L148) / default placeholder value                                                                   | `<constructor-arg index="1" value="${userSearchFilter}"/>` |             | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L148) / [Datadir common context property file](#datadir-common-context-property-file)               | `<constructor-arg index="1" value="${userSearchFilter}"/>` |             | Not set  |
| Highest  | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L148) / [Datadir application context property file](#datadir-application-context-property-file)     | `<constructor-arg index="1" value="${userSearchFilter}"/>` | `(uid={0})` |          |

### `LdapContextSource` bean

The `LdapContextSource` bean is created at [applicationContext-security.xml](./src/main/webapp/WEB-INF/applicationContext-security.xml#L152-L157) using the org.springframework.ldap.core.support.LdapContextSource class.

#### `url` property

The [`url` property](./src/main/webapp/WEB-INF/applicationContext-security.xml#L153) can be configured as follows:

| Priority | From                                                                                                                                                                          | Code                                         | Value              | Comments |
| -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------- | ------------------ | -------- |
| Lowest   | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L153) / default placeholder value                                                                   | `<property name="url" value="${ldapUrl}" />` |                    | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L153) / [Datadir common context property file](#datadir-common-context-property-file)               | `<property name="url" value="${ldapUrl}" />` |                    | Not set  |
| Highest  | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L153) / [Datadir application context property file](#datadir-application-context-property-file)     | `<property name="url" value="${ldapUrl}" />` | `ldap://localhost` |          |

#### `base` property

The [`base` property](./src/main/webapp/WEB-INF/applicationContext-security.xml#L154) can be configured as follows:

| Priority | From                                                                                                                                                                          | Code                                         | Value                   | Comments |
| -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------- | ----------------------- | -------- |
| Lowest   | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L154) / default placeholder value                                                                   | `<property name="base" value="${baseDN}" />` |                         | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L154) / [Datadir common context property file](#datadir-common-context-property-file)               | `<property name="base" value="${baseDN}" />` |                         | Not set  |
| Highest  | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L154) / [Datadir application context property file](#datadir-application-context-property-file)     | `<property name="base" value="${baseDN}" />` | `dc=georchestra,dc=org` |          |

#### `userDn` property

The [`userDn` property](./src/main/webapp/WEB-INF/applicationContext-security.xml#L155) can be configured as follows:

| Priority | From                                                                                                                                                                          | Code                                                | Value                            | Comments |
| -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------- | -------------------------------- | -------- |
| Lowest   | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L155) / default placeholder value                                                                   | `<property name="userDn" value="${ldapAdminDn}" />` |                                  | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L155) / [Datadir common context property file](#datadir-common-context-property-file)               | `<property name="userDn" value="${ldapAdminDn}" />` |                                  | Not set  |
| Highest  | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L155) / [Datadir application context property file](#datadir-application-context-property-file)     | `<property name="userDn" value="${ldapAdminDn}" />` | `cn=admin,dc=georchestra,dc=org` |          |

#### `password` property

The [`password` property](./src/main/webapp/WEB-INF/applicationContext-security.xml#L156) can be configured as follows:

| Priority | From                                                                                                                                                                          | Code                                                          | Value    | Comments |
| -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------- | -------- | -------- |
| Lowest   | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L156) / default placeholder value                                                                   | `<property name="password" value="${ldap.admin.password}" />` |          | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L156) / [Datadir common context property file](#datadir-common-context-property-file)               | `<property name="password" value="${ldap.admin.password}" />` |          | Not set  |
| Highest  | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L156) / [Datadir application context property file](#datadir-application-context-property-file)     | `<property name="password" value="${ldap.admin.password}" />` | `secret` |          |

### `ldapAuthoritiesPopulator` bean

The `ldapAuthoritiesPopulator` bean is created at [applicationContext-security.xml](./src/main/webapp/WEB-INF/applicationContext-security.xml#L163-L170) using the org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator class.

#### `groupSearchBase` constructor argument

The [`groupSearchBase` constructor argument](./src/main/webapp/WEB-INF/applicationContext-security.xml#L165) can be configured as follows:

| Priority | From                                                                                                                                                                          | Code                                               | Value      | Comments |
| -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------- | ---------- | -------- |
| Lowest   | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L165) / default placeholder value                                                                   | `<constructor-arg value="${authoritiesBaseDN}" />` |            | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L165) / [Datadir common context property file](#datadir-common-context-property-file)               | `<constructor-arg value="${authoritiesBaseDN}" />` |            | Not set  |
| Highest  | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L165) / [Datadir application context property file](#datadir-application-context-property-file)     | `<constructor-arg value="${authoritiesBaseDN}" />` | `ou=roles` |          |

#### `groupSearchFilter` property

The [`groupSearchFilter` property](./src/main/webapp/WEB-INF/applicationContext-security.xml#L166) can be configured as follows:

| Priority | From                                                                                                                                                                          | Code                                                               | Value                                             | Comments |
| -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------ | ------------------------------------------------- | -------- |
| Lowest   | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L166) / default placeholder value                                                                   | `<property name="groupSearchFilter" value="${roleSearchFilter}"/>` |                                                   | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L166) / [Datadir common context property file](#datadir-common-context-property-file)               | `<property name="groupSearchFilter" value="${roleSearchFilter}"/>` |                                                   | Not set  |
| Highest  | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L166) / [Datadir application context property file](#datadir-application-context-property-file)     | `<property name="groupSearchFilter" value="${roleSearchFilter}"/>` | `(member=uid={1},ou=users,dc=georchestra,dc=org)` |          |

### `casProcessingFilterEntryPoint` bean

The `casProcessingFilterEntryPoint` bean is created at [applicationContext-security.xml](./src/main/webapp/WEB-INF/applicationContext-security.xml#L206-L209) using the org.springframework.security.cas.web.CasAuthenticationEntryPoint class.

#### `loginUrl` property

The [`loginUrl` property](./src/main/webapp/WEB-INF/applicationContext-security.xml#L207) can be configured as follows:

| Priority | From                                                                                                                                                                          | Code                                                 | Value                                        | Comments |
| -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------- | -------------------------------------------- | -------- |
| Lowest   | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L207) / default placeholder value                                                                   | `<property name="loginUrl" value="${casLoginUrl}"/>` |                                              | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L207) / [Datadir common context property file](#datadir-common-context-property-file)               | `<property name="loginUrl" value="${casLoginUrl}"/>` |                                              | Not set  |
| Highest  | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L207) / [Datadir application context property file](#datadir-application-context-property-file)     | `<property name="loginUrl" value="${casLoginUrl}"/>` | `https://georchestra.mydomain.org/cas/login` |          |

### `casAuthenticationProvider` bean

The `casAuthenticationProvider` bean is created at [applicationContext-security.xml](./src/main/webapp/WEB-INF/applicationContext-security.xml#L211-L220) using the org.springframework.security.cas.authentication.CasAuthenticationProvider class.

#### `ticketValidator` property

The [`ticketValidator` property](./src/main/webapp/WEB-INF/applicationContext-security.xml#L214-L218) is created through a bean that uses the org.jasig.cas.client.validation.Cas20ServiceTicketValidator class. It can be configured as follow, through the first argument of the constructor of that class:

| Priority | From                                                                                                                                                                               | Code                                                                                                                                                                                        | Value                                  | Comments |
| -------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------- | -------- |
| Lowest   | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L214-L218) / default placeholder value                                                                   | `<property name="ticketValidator"><bean class="org.jasig.cas.client.validation.Cas20ServiceTicketValidator"><constructor-arg index="0" value="${casTicketValidation}" /></bean></property>` |                                        | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L214-L218) / [Datadir common context property file](#datadir-common-context-property-file)               | `<property name="ticketValidator"><bean class="org.jasig.cas.client.validation.Cas20ServiceTicketValidator"><constructor-arg index="0" value="${casTicketValidation}" /></bean></property>` |                                        | Not set  |
| Highest  | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L214-L218) / [Datadir application context property file](#datadir-application-context-property-file)     | `<property name="ticketValidator"><bean class="org.jasig.cas.client.validation.Cas20ServiceTicketValidator"><constructor-arg index="0" value="${casTicketValidation}" /></bean></property>` | `https://georchestra.mydomain.org/cas` |          |

### `serviceProperties` bean

The `serviceProperties` bean is created at [applicationContext-security.xml](./src/main/webapp/WEB-INF/applicationContext-security.xml#L222-L225) using the org.springframework.security.cas.ServiceProperties class.

#### `service` property

The [`service` property](./src/main/webapp/WEB-INF/applicationContext-security.xml#L223) can be configured as follows:

| Priority | From                                                                                                                                                                          | Code                                                  | Value                                        | Comments |
| -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------- | -------------------------------------------- | -------- |
| Lowest   | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L223) / default placeholder value                                                                   | `<property name="service" value="${proxyCallback}"/>` |                                              | Not set  |
|          | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L223) / [Datadir common context property file](#datadir-common-context-property-file)               | `<property name="service" value="${proxyCallback}"/>` |                                              | Not set  |
| Highest  | [Bean creation](./src/main/webapp/WEB-INF/applicationContext-security.xml#L223) / [Datadir application context property file](#datadir-application-context-property-file)     | `<property name="service" value="${proxyCallback}"/>` | `https://georchestra.mydomain.org/login/cas` |          |
