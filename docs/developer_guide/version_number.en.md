# Releases

## Version Numbering Scheme

geOrchestra releases are named `YY.V.P` where:
 * `YY` are the latest two digits of the year (eg `20` for 2020)
 * `V` is an integer which represents the release index in the year (eg `0` for the first one, `1` for the second one)
 * `P` stands for the patch number

Upgrading from a version to another, which only differ by the patch number, does not require any configuration change, and is considered safe.

Upgrading from a major version `YY.V` to another one, like `YY.W` or `ZZ.*` requires a [migration process](https://github.com/georchestra/georchestra/blob/master/migrations).

## Branches, versions, tags

![branches](images/branches.jpg)

Patch releases use the same datadir branch name. For instance, versions 20.0.0 and 20.0.1 expect a [datadir branch 20.0](https://github.com/georchestra/datadir/tree/20.0)

## Applications that use georchestra core librairies

Some applications use georchestra core libraries, and are versioned with the following scheme.

If the application is a fork of an existing application, it should contain another docker tag containing the version of the forked application.

| Change                                                | Core geOrchestra Image Tag versions | War/Jar/Deb | Example Tag of application | Second tag (with upstream's version) | Description                                | App Branch |
|-------------------------------------------------------|-------------------------------------|-------------|----------------------------|--------------------------------------|--------------------------------------------|------------|
| Legacy (if app was in core before splitting the repo) | 24.0.1                              | 24.0.1      | 24.0.1                     | 24.0.1                               | keep legacy versionning                    |            |
| New versionning                                       | 25.0.0                              | 1.0.0       | 1.0.0                      | 1.0.0-4.4.8                          | Introduce two new tag versions             | 1.0.x      |
| geOrchestra Core bugfix update                        | 25.0.1                              | 1.0.0       | 1.0.0                      | 1.0.0-4.4.8                          | Doesn't change as nothing changed in App   | 1.0.x      |
| Specific geOrchestra/geonetwork bugfix                | 25.0.0 - App bugfix                 | 1.0.1       | 1.0.1                      | 1.0.1-4.4.8                          | fork version bump                          | 1.0.x      |
| Major update of app                                   | 25.0.0 - App major version bump     | 2.0.0       | 2.0.0                      | 2.0.0-5.0.0                          | Bump to major either                       | 2.0.x      |
| New georchestra major version involving change...     | 25.1.0                              | 1.1.0       | 1.1.0                      | 1.1.0-4.4.8                          | Bump minor accordingly with changes        | 1.1.x      |
| New minor                                             | 25.1.1                              | 1.1.0       | 1.1.0                      | 1.1.0-4.4.8                          | No change                                  | 1.1.x      |
| Main branch of app                                    | All                                 | 99.master   | latest                     | -                                    | Main branch should set a prefix of 99.main | main       |
