# How to upgrade versions ?

First of all, read the [release notes](../RELEASE_NOTES.md) matching the version you plan to install.

## Pre-requisites

We suppose that your configuration is versioned with git, and that it has two remotes:
 * **origin** points to your own private repository
 * **upstream** points to the template georchestra configuration

When typing ```git remote -v``` in your config folder (either ```~/myprofile``` or ```~/georchestra/config/configurations/myprofile```), you should see something like this:
```
origin	https://gitlab.com/user/myprofile.git (fetch)
origin	https://gitlab.com/user/myprofile.git (push)
upstream	https://github.com/georchestra/template.git (fetch)
upstream	https://github.com/georchestra/template.git (push)
```

If one remote is missing, you may add it with, eg ```git remote add upstream https://github.com/georchestra/template.git```.


## Upgrade your configuration directory

Say we want to upgrade from version 14.06 to version 14.12. This means that your config is currently in the branch 14.06 of your repository.

First, fetch the remote branches from upstream:
```
git fetch upstream
```

Then, create a new 14.12 local branch mirroring the remote one:
```
git checkout -b 14.12 upstream/14.12
```

Finally, merge into your new "14.12" local branch the changes you made in the 14.06 branch:
```
git merge 14.06
```

You may have to solve some conflicts, but this should not be very difficult.  
When you're done, commit and push your new branch:
```
git commit -am "now on 14.12"
git push origin 14.12
```


## Build

In your georchestra sources directory, update to the new "14.12" branch:

```
cd ~/georchestra
git fetch origin
git checkout 14.12
git submodule sync
git submodule update --init
```

Then, make sure that your configuration is also pointing to the same branch:
```
cd ~/georchestra/config/configurations/myprofile
git fetch origin
git checkout 14.12
```

Then, you have to [build the webapps](build.md) again and deploy them in a test server. 
