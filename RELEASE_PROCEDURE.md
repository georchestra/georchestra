# geOrchestra release procedure

This is an attempt to write down the procedure to make a release of the geOrchestra SDI.

## Major releases

### Datadir

From [master](https://github.com/georchestra/datadir/tree/master), create a new branch:
```
git checkout master
git pull origin master
git checkout -b 20.1
git push origin 20.1
```

Same has to be done for the `docker` branch:
```
git checkout docker-master
git pull origin docker-master
git checkout -b docker-20.1
git push origin docker-20.1
```

### GeoServer minimal datadir

From [master](https://github.com/georchestra/geoserver_minimal_datadir/tree/master), create a new branch:
```
git checkout master
git pull origin master
git checkout -b 20.1
git push origin 20.1
```

Same has to be done for the `geofence` branch:
```
git checkout geofence
git pull origin geofence
git checkout -b 20.1-geofence
git push origin 20.1-geofence
```

### GeoNetwork

```
cd geonetwork
git checkout georchestra-gn3.10.2
git pull origin georchestra-gn3.10.2
git tag 20.1.0
git push origin 20.1.0
```

Then [create a new release](https://github.com/georchestra/geonetwork/releases).

### geOrchestra

From the master branch of the [georchestra](https://github.com/georchestra/georchestra/tree/master) repository, derive a `20.1.x` branch:
```
git checkout master
git pull origin master
git checkout -b 20.1.x
```

Update the GeoNetwork submodule to the release commit:
```
cd geonetwork
git fetch origin
git checkout 20.1.0
cd -
```

Other tasks:
 * Manually update the files mentionning the current release version (```README.md``` and ```RELEASE_NOTES.md```)
 * Update the branch name for the Travis status logo
 * Change the `packageDatadirScmVersion` parameter in the root `pom.xml` to `20.1`
 * Change the `BTAG` variable in the Makefile to `20.1.x`
 * Check the submodule branches in `.gitmodules` are correct, since [dependabot](https://app.dependabot.com/accounts/georchestra/) depends on it to update submodules
 * Setup a [new dependabot job](https://app.dependabot.com/accounts/georchestra/) which takes care of updating the submodules for this new branch
 * Change the default branches in github repositories

Commit and propagate the changes:
```
git add geonetwork
git commit -am "20.1.x branch"
```

When the release is ready on branch `20.1.x`, push a tag:
```
git tag 20.1.0
git push origin 20.1.x --tags
```

The master branch requires some work too:
```
git checkout master
find ./ -name pom.xml -exec sed -i 's#<version>20.1-SNAPSHOT</version>#<version>20.2-SNAPSHOT</version>#' {} \;
git submodule foreach 'git reset --hard'
git commit -am "updated project version in pom.xml"
```

geOrchestra 20.1.0 is now released, congrats !

Do not forget to :
 * update the [website](http://www.georchestra.org/software.html)
 * [tweet](https://twitter.com/georchestra) !

## Patch releases

We're taking example on the 20.0.2 release.

### GeoNetwork

```
cd geonetwork
git checkout georchestra-gn3.8.2
git pull origin georchestra-gn3.8.2
git tag 20.0.2
git push origin 20.0.2
```

Then [create a new release](https://github.com/georchestra/geonetwork/releases).


### geOrchestra

Create the release commit:
```
cd georchestra
git checkout 20.0.x
git pull origin 20.0.x
find ./ -name pom.xml -exec sed -i 's#<version>20.0-SNAPSHOT</version>#<version>20.0.2</version>#' {} \;
cd geonetwork
git fetch origin
git checkout 20.0.2
cd ..
git add geonetwork
git add -p
git commit -m "20.0.2 release"
git push origin 20.0.x
git tag 20.0.2
git push origin 20.0.2
```

Then [create a new release](https://github.com/georchestra/georchestra/releases).

Finally, revert the maven version back to SNAPSHOT:
```
find ./ -name pom.xml -exec sed -i 's#<version>20.0.2</version>#<version>20.0-SNAPSHOT</version>#' {} \;
git add -p
git commit -m "back to SNAPSHOT"
git push origin 20.0.x
```

Create new milestones for [georchestra](https://github.com/georchestra/georchestra/milestones) and [geonetwork](https://github.com/georchestra/geonetwork/milestones).

[Tweet](https://twitter.com/georchestra) !
