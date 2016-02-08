# geOrchestra release procedure

This is an attempt to write down the procedure to make a release of the geOrchestra SDI.

The use case we describe here is the release of geOrchestra 13.12.

## Configuration template

Go to the master branch in your template config repository:

```
cd georchestra/template
git checkout master
git pull origin master
```

Let's create a new branch for the release:

```
git checkout -b 13.12
git push origin 13.12
```

There's nothing more to do here !

## LDAP

Same as above !
We have to create a dedicated branch for the new release.

## GeoNetwork

Go to the georchestra-13.12 branch in your geonetwork repository:

```
cd georchestra/geonetwork
git checkout georchestra-13.12
git pull origin georchestra-13.12
```

Batch change the version to the release version in every pom.xml:

```
find ./ -name pom.xml -exec sed -i 's/13.12-SNAPSHOT/13.12/' {} \;
```

Commit and propagate the changes:

```
git commit -am "13.12 release"
git push origin georchestra-13.12
```

Now is the time to create the development branch for the future version (14.06):

```
git checkout -b georchestra-14.06
```

In the ```georchestra-14.06``` branch, let's create a brand new ```db-migrate-default.sql``` migration file:

```
$ cat > web/src/main/webapp/WEB-INF/classes/setup/sql-georchestra/migrate/1406/db-migrate-default.sql << EOF
BEGIN;
UPDATE Settings SET value='14.06' WHERE name='version';
UPDATE Settings SET value='0' WHERE name='subVersion';
COMMIT;
EOF
```

Let's also update the pom's versions:

```
find ./ -name pom.xml -exec sed -i 's/13.12/14.06-SNAPSHOT/' {} \;
```

At this point, we should commit and propagate the branch:

```
git add web/src/main/webapp/WEB-INF/classes/setup/sql-georchestra/migrate/1406/db-migrate-default.sql
git commit -am "Branch georchestra-14.06 created"
git push origin georchestra-14.06
```

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
find ./ -name pom.xml -exec sed -i 's/13.12-SNAPSHOT/13.12/' {} \;
```

Reset changes in every submodule:

```
git submodule foreach 'git reset --hard'
```

Update the GeoNetwork submodule to the release commit:

```
cd geonetwork
git checkout georchestra-13.12
git pull origin georchestra-13.12
cd -
```

Manually update the files mentionning the current release version (```README.md``` and ```RELEASE_NOTES.md```).
Also update the branch name for the Travis status logo.

Commit and propagate the changes:

```
git add geonetwork
git commit -am "13.12 release"
git tag v13.12
git push origin master --tags
```

Now, let's create the maintenance branch for geOrchestra 13.12:

```
git checkout -b 13.12
git push origin 13.12
```

... and update the project version in master:

```
git checkout master
find ./ -name pom.xml -exec sed -i 's/13.12/14.06-SNAPSHOT/' {} \;
git submodule foreach 'git reset --hard'
git commit -am "updated project version in pom.xml"
```

Let's update GN submodule too:

```
cd geonetwork
git fetch origin
git checkout georchestra-14.06
git pull origin georchestra-14.06
cd -
```

Commit and propagate the changes:

```
git add geonetwork
git commit -m "updated GeoNetwork submodule"
git push origin master
```

geOrchestra 13.12 is now released, congrats !

Finally, change the default branch to latest stable in the [georchestra](https://github.com/georchestra/georchestra/settings), [geonetwork](https://github.com/georchestra/geonetwork/settings), [template](https://github.com/georchestra/template/settings) and [LDAP](https://github.com/georchestra/LDAP/settings) repositories.
... and eventually in the geoserver and geofence repositories too.
