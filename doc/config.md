At the moment, geOrchestra requires that you prepare a configuration directory in order to build the webapps.  
Contrary to other projects such as GeoServer, we do not provide generic WARs.


Let's bootstrap your config directory from the template we provide:
```
cd ~
git clone https://github.com/georchestra/template.git myprofile
cd myprofile
git remote rename origin upstream
```

First thing to note is that you end up in the stable branch matching the lastest stable geOrchestra version (currently 14.06).  
This is exactly what we want, nice !


What is provided ?
==================

Let's see what the template configuration looks like:

```
user@computer:~/myprofile (14.06)$ tree -L 1
.
├── analytics
├── build_support
├── cas-server-webapp
├── catalogapp
├── DeployScript.groovy
├── excluded
├── extractorapp
├── geonetwork-client
├── geonetwork-main
├── header
├── mapfishapp
└── README.md
```

There is roughly one folder per geOrchestra module.

Several folders and files are special:
 * the **build_support** dir contains the two most important files to configure your instance,
 * the **excluded** dir is empty. It can be used for versioning useful files related to your configuration.
 * the **DeployScript.groovy** file is optional. It allows you to automate webapps deployment, but we'll see that later.

During the build process, files in the other folders will be copied into their respective webapps (or will overwrite the files that are already there).  
Eg: ~/myprofile/mapfishapp/app/js/GEOR_custom.js will be copied to ~/georchestra/mapfishapp/src/main/webapp/app/js/GEOR_custom.js


How to customize ?
==================

Let's focus on the **build_support** directory first.

The first file you'll have to edit is **shared.maven.filters**. This one contains all the configuration options which are shared between our modules.
To make it clear, we prefixed all variables with the ```shared``` keyword. Eg: ```shared.log.dir``` is the directory where **all** webapps will write their logs to.

This file has lots of comments: you should read them all.  
Once you've finished, we think you should **at least** update these shared maven filters:
 * ```shared.ldap.admin.password``` is the ldap administrative account password,
 * ```shared.privileged.geoserver.pass``` is the password for a special geOrchestra admin user which is internally used, (see the [setup/openldap.md](ldap setup documentation)),
 * ```shared.email.*``` are the settings related to email sending (used by ldapadmin and extractorapp)
 * ```shared.administrator.email``` is your email.


The second file you have to edit in build_support is **GenerateConfig.groovy**. This one is a groovy script which is used to customize application-specific configuration files.
Most of the server-side application config options can be customized by the way of this script.

The most important lines in this file are those matching this pattern:
```
properties['key'] = "value"
```

You should update the values for several properties, typically:
 * ```emailsubject``` in the ```updateExtractorappMavenFilters``` method,
 * ```proxy.mapping``` in the ```updateSecProxyMavenFilters``` method, (see also the [setup/tomcat.md](tomcat setup documentation))
 * ```publicKey```, ```privateKey``` and all the ```subject.*``` properties in the ```updateLDAPadminMavenFilters``` method.



Read more about the [configuration process](config/README.md).