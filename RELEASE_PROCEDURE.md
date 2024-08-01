# geOrchestra release procedure

This is an attempt to write down the procedure to make a release of the geOrchestra SDI.

## Major releases

### Datadir

From [master](https://github.com/georchestra/datadir/tree/master), create a new branch:
```
git checkout master
git pull origin master
git checkout -b 24.1
git push origin 24.1
```

Same has to be done for the `docker-master` branch:
```
git checkout docker-master
git pull origin docker-master
git checkout -b docker-24.1
git push origin docker-24.1
```

### Docker

From [master](https://github.com/georchestra/docker/tree/master), create a new branch:
```
git checkout master
git pull origin master
git checkout -b 24.1
```

Update the image tags:
```
sed -i 's/latest/24.1.x/g' docker-compose.yml
```

Make sure the config folder tracks the `docker-24.1` datadir branch:
```
$ cat .gitmodules
[submodule "config"]
	path = config
	url = https://github.com/georchestra/datadir.git
	branch = docker-24.1
```

Manually update the README and `.github/dependabot.yml`

```
git commit -am "24.1 branch"
git push origin 24.1
```

### GeoServer minimal datadir

From [master](https://github.com/georchestra/geoserver_minimal_datadir/tree/master), create a new branch:
```
git checkout master
git pull origin master
git checkout -b 24.1
git push origin 24.1
```

Same has to be done for the `geofence` branch:
```
git checkout geofence
git pull origin geofence
git checkout -b 24.1-geofence
git push origin 24.1-geofence
```

### GeoNetwork

```
cd geonetwork
git checkout georchestra-gn4.4.x
git pull origin georchestra-gn4.4.x
git tag 24.1.0
git push origin 24.1.0
```

Then [create a new release](https://github.com/georchestra/geonetwork/releases).

### CAS

[https://github.com/georchestra/georchestra-cas-server](https://github.com/georchestra/georchestra-cas-server)

```
cd georchestra-cas-server
git checkout master
git pull -r # to get lastest commits
git checkout -b 24.0.x

# edit gradle.properties to replace dockerTag and datadirRef by: dockerTag=24.0.x and datadirRef=23.0
git add gradle.properties
git commit -am "24.0.x branch"
git tag 24.0.0-georchestra
git push --set-upstream origin 24.0.x --tag

```

Then [create a new release](https://github.com/georchestra/georchestra-cas-server/releases).

### mapstore2-geOrchestra

[https://github.com/georchestra/mapstore2-georchestra](https://github.com/georchestra/mapstore2-georchestra)

Mapstore2-geOrchestra has its own [release
procedure](https://github.com/georchestra/mapstore2-georchestra#release-procedure)
and follow its own [release
train](https://github.com/georchestra/mapstore2-georchestra/releases).

It might not be necessary to make a release of mapstore2-georchestra when
releasing geOrchestra, but if it's really needed, just place a tag on the
latest commit in the stable branch (as of today,
[2023.02.xx](https://github.com/georchestra/mapstore2-georchestra/tree/2023.02.xx)
but will soon 2024.01.xx)

The versioning might not necessary match geOrchestra's, but follows mapstore's
stable branch used in the
[MapStore2)(https://github.com/georchestra/mapstore2-georchestra/blob/master/.gitmodules)
submodule.

### geOrchestra

From the master branch of the [georchestra](https://github.com/georchestra/georchestra/tree/master) repository, derive a `24.1.x` branch:
```
git checkout master
git pull origin master
git checkout -b 24.1.x
```

Update the GeoNetwork submodule to the release commit:
```
cd geonetwork
git fetch origin
git checkout 24.1.0
cd -
```

Other tasks:
 * Manually update the files mentionning the current release version (```README.md``` and ```RELEASE_NOTES.md```)
 * Update the branch name for the Travis status logo
 * Change the `packageDatadirScmVersion` parameter in the root `pom.xml` to `24.1`
 * Replace `99.master` by `${build.closest.tag.name}` in the root `pom.xml` so that debian packages have the right version
 * Change the `BTAG` variable in the Makefile to `24.1.x`
 * Check the submodule branches in `.gitmodules` are correct, since [dependabot](https://app.dependabot.com/accounts/georchestra/) depends on it to update submodules
 * Setup a [new dependabot job](https://app.dependabot.com/accounts/georchestra/) which takes care of updating the submodules for this new branch
 * Update the [github workflow](https://github.com/georchestra/georchestra/tree/master/.github/workflows) files to change the `refs/heads/22.` to `refs/heads/23.` to push on docker hub new images versions
 * Change the default branches in github repositories
 * clean and archive old/none used branches

Commit and propagate the changes:
```
git add geonetwork
git commit -am "24.1.x branch"
```

When the release is ready on branch `24.1.x`, push a tag:
```
git tag 24.1.0
git push origin 24.1.x --tags
```

The master branch requires some work too:
```
git checkout master
find ./ -name pom.xml -exec sed -i 's#<version>24.1-SNAPSHOT</version>#<version>24.2-SNAPSHOT</version>#' {} \;
git submodule foreach 'git reset --hard'
git commit -am "updated project version in pom.xml"
```

geOrchestra 24.1.0 is now released, congrats !

Do not forget to :
 * update dependabot files (like https://github.com/georchestra/docker/blob/master/.github/dependabot.yml)
 * update https://packages.georchestra.org/
 * update the [website](http://www.georchestra.org/software.html)
 * [tweet](https://twitter.com/georchestra) !

## Patch releases

We're taking example on the 24.0.3 release.

### geOrchestra & GeoNetwork submodule

Create the release commit:
```
cd georchestra
git checkout 24.0.x
git pull origin 24.0.x
git submodule update --init --recursive
cd geonetwork 
git checkout georchestra-gn4.4.x
git pull origin georchestra-gn4.4.x
cd ../
find ./ -name pom.xml -exec sed -i 's#version>24.0.3-SNAPSHOT</#version>24.0.3</#' {} \;
cd geonetwork
git add georchestra-integration/pom.xml
git commit -m "24.0.3 release"
git tag 24.0.3
git push origin 24.0.3
git push origin georchestra-gn4.4.x

```
Then [create a new release for GeoNetwork](https://github.com/georchestra/geonetwork/releases).

```
cd ../
git add geonetwork
git add -p
git commit -m "24.0.3 release"
git push origin 24.0.x
git tag 24.0.3
git push origin 24.0.3
```

Then [create a new release for geOrchestra](https://github.com/georchestra/georchestra/releases).

Finally, revert the maven version back to SNAPSHOT and new patch release:
```
find ./ -name pom.xml -exec sed -i 's#version>24.0.3</#version>24.0.4-SNAPSHOT</#' {} \; 
cd geonetwork
git add georchestra-integration/pom.xml
git commit -m "back to SNAPSHOT"
git push
cd ../
git add -p
git commit -m "back to SNAPSHOT"
git push origin 24.0.x
```

Create new milestones for [georchestra](https://github.com/georchestra/georchestra/milestones) and [geonetwork](https://github.com/georchestra/geonetwork/milestones).

[Tweet](https://twitter.com/georchestra) !

### About the repository split

At the project beginning, the developers wanted to keep every parts of geOrchestra into the same repository. For different reasons, across time, keeping
this big monolithic repository made less sense. For example, across releases, some of the webapps were evolving fast, and some other did not move for a long time.
What is the point of releasing the almost exact same version of an artifact if the code between 2 versions did not change significantly ?

*  When migrating to CAS6, the upstream procedure to customize our CAS instance has been followed, starting from the CAS overlay webapp project,
which is not based on maven, but on gradle. While mixing both build systems is probably possible, it does not make it necessarily desirable.

*  `Mapstore2-georchestra`, the new viewer which aims to replace mapfishapp, has been developped separately by an other team than the core one taking care of geOrchestra.
Technically speaking, it made more sense to create a separate repository and give the team in charge of the development access to it, instead of the whole geOrchestra
repository.

As a result, more and more components have now their own dedicated repository. This has obvisously an impact on the release deployment process.

Historically Geonetwork and GeoServer have their own forks of the upstream repository, and are still integrated using submodules though.

Releasing a new version of geOrchestra when it comes to these new repositories is not very different though: the process is to also set a tag and/or create
a branch so that an artifact can be generated.

**To summarize, when releasing a patch release, do not forget to:**
* `georchestra/mapstore2-georchestra`: Add a tag on top of the stable branch. As of today, `24.0.0-georchestra`, in branch `2023.02.xx`.
* `georchestra/georchestra-cas-server`: Add a tag on top of the appropriate branch (`24.0.x`)

### Packaging

The CICD processes provide 3 main types of artifacts:

*  (generic) web archives (WAR) - from a self-hosted buildbot
*  debian packages - from a self hosted buildbot
*  docker images - via the Github Actions

The generic WARS as well as the debian packages are built following a branch (master, 24.x, ...). The docker images are following the same rules,
but are also creating an image for each tag (e.g. "releases"). The main difference here is that generic wars & debian packages do not have an artifact for tags / releases,
as they follow the evolution of each branches.

This also means that there is a working branch in each repositories (georchestra, masptore, cas), usually named "master", then another branch for stable release.
Following the release conventions, we can still set a tag on these branches to "materialize" the releases.
