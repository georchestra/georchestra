# Releases

## georchestra/georchestra repo

> Since geOrchestra 26, most modules are now in their own repos and releasing core is simpler.

### Todo on GitHub

After updating files for a minor or major release:

- Create a new release on the tag created: <https://github.com/georchestra/georchestra/releases>
    - Fill the compatibility matrix with the latest stable, compatible versions of known modules (you can often copy and edit it from a previous release), e.g.:

| App                  | Release link                                                                                                                                                                     | Docker tag                                             | Upstream version |
|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------|------------------|
| georchestra/console  |                                                                                                                                                                                  | 25.0.3                                                 | -                |
| georchestra/gateway  | [georchestra/gateway@2.0.5](https://github.com/georchestra/georchestra-gateway/releases/tag/2.0.5)                                                                              | 2.0.5                                                  | -                |
| georchestra/mapstore | [georchestra/mapstore@2024.02.00-geOrchestra-headerConfig-6abf34](https://github.com/georchestra/mapstore2-georchestra/releases/tag/2024.02.00-geOrchestra-headerConfig-6abf34) | 2024.02.00-geOrchestra-headerConfig-6abf34             | 2024.02.00       |

- Make an announcement: <https://github.com/orgs/georchestra/discussions/categories/announcements>

---

### Minor release

#### Steps on the repo

**Easy method:**

1. Execute `./minor-release.sh 26.0.1` to release version 26.0.1 (you can set a second argument to trigger it on another branch, e.g. `27.0.x`)
2. Push after verification of commits and tag: `git push --tags`

**Manual method:**

1. Checkout and pull the necessary branch: `git checkout 26.0.x && git pull`
2. Replace the version in poms (root, console, commons…), e.g.: replace `<version>26.0.1-SNAPSHOT</version>` with `<version>26.0.1</version>`
3. Commit those poms
4. Add a tag: `git tag 26.0.1`
5. Set back the version to the next snapshot, e.g.: replace `<version>26.0.1</version>` with `<version>26.0.2-SNAPSHOT</version>`
6. Commit again and push with tags: `git push --tags`
7. Follow the [Todo on GitHub](#todo-on-github) steps

---

### Major release

For a major release, you'll have to update multiple repos:

1. `georchestra/datadir`
2. `georchestra/georchestra`
3. `georchestra/docker`

#### 1. georchestra/datadir

From the `master` and `docker-master` branches, create two branches `26.0` and `docker-26.0` accordingly.

#### 2. georchestra/georchestra

1. Checkout master, pull, and launch `./update-licence-headers.sh` to add licence headers and update dates if necessary on files, then commit.
2. Create a branch finishing with `.x`: `26.0.x`
3. Ensure that all `migrations/master` notes are up to date:
    - Move them to a new folder `migrations/26.0`
    - Remove everything in `migrations/master`
4. Update `packageDatadirScmVersion` in `pom.xml`
5. Follow the same manual method as a minor release but with these versions:
    - Version will be `26.0.0`
    - Next snapshot will be `26.0.1-SNAPSHOT`
6. After pushing the newest branch, checkout master again and set the version to the next major release: `26.1-SNAPSHOT`
7. Follow the [Todo on GitHub](#todo-on-github) steps

#### 3. georchestra/docker

1. From `master`, create a branch `26.0` and update the necessary versions.
2. Update the `.gitmodules` file to point to the `docker-26.0` branch.

## Other repos

When releasing a module:

1. Create the release on the corresponding repo following its own release process.
2. Follow the [Todo on GitHub](#todo-on-github) steps.