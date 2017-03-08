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


## GeoServer minimal datadir

In https://github.com/georchestra/geoserver_minimal_datadir create 2 new branches:
 * `16.12` from `master`
 * `16.12-geofence` from `geofence`

Update the default branch to `16.12`.

## GeoNetwork 3

Create a new branch deriving from `georchestra-gn3-master`, eg `georchestra-gn3-16.12`.

In branch `georchestra-gn3-16.12`, in file `web/pom.xml` change `<dockerDatadirScmVersion>docker-master</dockerDatadirScmVersion>` into `<dockerDatadirScmVersion>docker-16.12</dockerDatadirScmVersion>`

Commit and push `georchestra-gn3-16.12`.

Merge `georchestra-gn3-16.12` into `georchestra-gn3-master`, restore `<dockerDatadirScmVersion>docker-master</dockerDatadirScmVersion>` in file `web/pom.xml`.
Commit and push `georchestra-gn3-master`.

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
Also update the branch name for the Travis status logo, and change the `dockerDatadirScmVersion` parameter in every `pom.xml` to `docker-16.12`:
```
find ./ -name pom.xml -exec sed -i 's#<dockerDatadirScmVersion>docker-master</dockerDatadirScmVersion>#<dockerDatadirScmVersion>docker-16.12</dockerDatadirScmVersion>#' {} \;
```

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

... and update the project version in master:

```
git checkout master
find ./ -name pom.xml -exec sed -i 's#<dockerDatadirScmVersion>docker-16.12</dockerDatadirScmVersion>#<dockerDatadirScmVersion>docker-master</dockerDatadirScmVersion>#' {} \;
find ./ -name pom.xml -exec sed -i 's#<version>16.12</version>#<version>17.06-SNAPSHOT</version>#' {} \;
git submodule foreach 'git reset --hard'
git commit -am "updated project version in pom.xml"
```

Let's update GN submodule too:

```
cd geonetwork
git fetch origin
git checkout georchestra-gn3-master
git pull origin georchestra-gn3-master
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
