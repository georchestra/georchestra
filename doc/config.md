# Creating a custom configuration

At the moment, building geOrchestra requires that you prepare a configuration directory.  
Contrary to other projects such as GeoServer, we do not provide generic WARs.


Let's bootstrap your config directory from the template we provide:
```
git clone https://github.com/georchestra/template.git ~/myprofile
cd ~/myprofile
git remote rename origin upstream
```

First thing to note is that you end up in the stable branch (14.06) matching the latest stable geOrchestra version (also 14.06). 
This is exactly what we want, nice !

Remember: every stable geOrchestra version has its own template configuration, hosted in a branch named by the release version.
This is something to keep in mind when [upgrading](how_to_upgrade.md).


## What is provided ?

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
 * the **DeployScript.groovy** file is optional. It allows you to [automate webapps deployment](deploy.md#automating-the-deploy-to-remote-hosts), but we'll see that later.

During the build process, files in the other folders will be copied into their respective webapps (or will overwrite the files that are already there).  
Eg: ```~/myprofile/mapfishapp/app/js/GEOR_custom.js``` will be copied to ```~/georchestra/mapfishapp/src/main/webapp/app/js/GEOR_custom.js```


## How to customize ?

### Shared parameters

Let's focus on the ```build_support``` directory first.

The first file you'll have to edit is **shared.maven.filters**. This one contains all the configuration options which are shared between our modules.
To make it clear, we prefixed all variables with the ```shared``` keyword. Eg: ```shared.log.dir``` is the directory where **all** webapps will write their logs to.

This file has lots of comments: you should read them all.
Once you've finished, we think you should **at least** update these shared maven filters:
 * ```shared.server.name```, is the server Fully Qualified Domain Name,
 * ```shared.ldap.admin.password``` is the ldap administrative account password,
 * ```shared.privileged.geoserver.pass``` is the password for a special geOrchestra admin user which is internally used, (see the [ldap setup documentation](setup/openldap.md) and the [ldap repository readme](https://github.com/georchestra/LDAP/blob/master/README.md)),
 * ```shared.email.*``` are the settings related to email sending (used by ldapadmin and extractorapp)
 * ```shared.administrator.email``` is your email.

### Module-specific parameters

The second file you have to edit in ```build_support``` is **GenerateConfig.groovy**. This one is a groovy script which is used to customize application-specific configuration files.
Most of the server-side application config options can be customized by the way of this script.

The most important lines in this file are those matching this pattern:
```
properties['key'] = "value"
```

You should update the values for several properties, typically:
 * ```proxy.mapping``` in the ```updateSecProxyMavenFilters``` method, (see also the [tomcat setup documentation](setup/tomcat.md)), especially if you have modified the ports for the tomcat HTTP connectors, or the webapp names,
 * ```emailsubject``` in the ```updateExtractorappMavenFilters``` method,
 * ```publicKey```, ```privateKey``` and all the ```subject.*``` properties in the ```updateLDAPadminMavenFilters``` method.

### Webapp overrides

The files from the ```analytics```, ```cas-server-webapp```, ```catalogapp```, ```extractorapp```, ```geonetwork-client```, ```geonetwork-main```, ```header``` and ```mapfishapp``` directories are just overriding those from the webapps.

This provides an easy way to customize stylesheets and images, but also to pass custom options to the javascript webapps.

The most important files to customize are probably:
 * ```~/myprofile/extractorapp/app/js/GEOR_custom.js``` - client-side parameters for the extractor
 * ```~/myprofile/mapfishapp/app/js/GEOR_custom.js``` - client-side parameters for the viewer
 * ```~/myprofile/mapfishapp/WEB-INF/print/config.yaml``` - print layouts for the viewer
 * ```~/myprofile/geonetwork-client/apps/georchestra/js/Settings.js``` - client-side parameters for the catalog
 * ```~/myprofile/geonetwork-client/apps/georchestra/js/map/Settings.js``` - client-side parameters for the catalog mini-map
 * ```~/myprofile/header/img/logo.png``` - your SDI logo
 
The above javascript files are rather lengthy, but they're full of interesting details: you should read them all too !
Feel free to edit the options to match your needs.

Example:

In ```~/myprofile/mapfishapp/app/js/GEOR_custom.js```, one can read:

``` js
    /**
     * Constant: ANIMATE_WINDOWS
     * {Boolean} Display animations on windows opening/closing
     * Defaults to true
     */
    //ANIMATE_WINDOWS: true,
```

The option is commented out, which means that it is useless as long as its value is true. 
Let's imagine you don't want animations on window openings ... you would have to uncomment the line, and change the value to false:

``` js
    /**
     * Constant: ANIMATE_WINDOWS
     * {Boolean} Display animations on windows opening/closing
     * Defaults to true
     */
    ANIMATE_WINDOWS: false,
```

## Versioning the configuration directory

The good news is that, configuration being mostly text files, it can be versionned very easily !  
Let's take advantage of this right now.

Since the configuration folder is already a git repository, you just have to commit regularly the changes you make:

```
cd ~/myprofile
<... editing files ...>
git commit -am "mapfishapp: ANIMATE_WINDOWS set to false"
```

Now, let's backup your config in the cloud. This is an optional but recommended step.  
In the following example, we're using [GitLab](https://gitlab.com/) hosting, but it could be any other provider such as [GitHub](https://github.com/) or [Bitbucket](https://bitbucket.org/), **provided the repository is private**. 

Here are the steps:
 * create an account, 
 * upload your public SSH key,
 * create a private repository - for instance one named "myprofile"
 
Ready ?  
Let's add the remote to your config directory:  
```
cd ~/myprofile
git remote add origin git@gitlab.com:USER/myprofile.git (where USER stands for your GitLab username)
```

... and push the changes:
```
git push origin master
```

Congrats !  
Your config is safe, and you are now ready to [build your own geOrchestra](build.md) !

## More

For the most curious of you, we have interesting reading about the [geOrchestra configuration process](../config/README.md) here.
