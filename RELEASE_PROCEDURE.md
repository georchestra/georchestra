# geOrchestra release procedure

This is an attempt to write down the procedure to make a release of the geOrchestra SDI.

The use case we describe here is the release of geOrchestra 16.12.

## Configuration template

Go to the master branch in your template config repository:

```
cd georchestra/template
git checkout master
git pull origin master
```

Let's create a new branch for the release:

```
git checkout -b 16.12
git push origin 16.12
```

There's nothing more to do here !

## Datadir

Same as above !
We have to create a dedicated branch in https://github.com/georchestra/datadir for the new release.


## GeoNetwork 3

Create a new branch deriving from `georchestra-gn3-master`, eg `georchestra-gn3-16.12`.

In `georchestra-gn3-16.12`:
 * in `web/pom.xml` change `<dockerDatadirScmVersion>docker-master</dockerDatadirScmVersion>` into `<dockerDatadirScmVersion>docker-16.12</dockerDatadirScmVersion>`
 * 


We're done for GeoNetwork !


## geOrchestra

Go to the master branch in your georchestra repository:

```
cd georchestra/georchestra
git checkout master
git pull origin master
git submodule update --init
```

Batch change the version to the release version in every pom.xml:

```
find ./ -name pom.xml -exec sed -i 's/16.12-SNAPSHOT/16.12/' {} \;
```

Reset changes in every submodule:

```
git submodule foreach 'git reset --hard'
```

Update the GeoNetwork submodule to the release commit:

```
cd geonetwork
git checkout georchestra-gn3-16.12
git pull origin georchestra-gn3-16.12
cd -
```

Manually update the files mentionning the current release version (```README.md``` and ```RELEASE_NOTES.md```).
Also update the branch name for the Travis status logo.

Commit and propagate the changes:

```
git add geonetwork
git commit -am "16.12 release"
git tag v16.12
git push origin master --tags
```

Now, let's create the maintenance branch for geOrchestra 16.12:

```
git checkout -b 16.12
git push origin 16.12
```

The first commit in this branch should update the `scmVersion` parameter in every `pom.xml` to `docker-16.12`.

... and update the project version in master:

```
git checkout master
find ./ -name pom.xml -exec sed -i 's/16.12/17.06-SNAPSHOT/' {} \;
git submodule foreach 'git reset --hard'
git commit -am "updated project version in pom.xml"
```

Let's update GN submodule too:

```
cd geonetwork
git fetch origin
git checkout georchestra-17.06
git pull origin georchestra-17.06
cd -
```

Commit and propagate the changes:

```
git add geonetwork
git commit -m "updated GeoNetwork submodule"
git push origin master
```

geOrchestra 16.12 is now released, congrats !

Finally, change the default branch to latest stable in the [georchestra](https://github.com/georchestra/georchestra/settings), [geonetwork](https://github.com/georchestra/geonetwork/settings), [template](https://github.com/georchestra/template/settings) and [datadir](https://github.com/georchestra/datadir/settings) repositories.
... and eventually in the geoserver and geofence repositories too.
