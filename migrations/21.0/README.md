# From 19.04 to 20.0.x

## Geonetwork 4

### Redundant authentication panel
_Georchestra_ handles the authentication of the user for the underlying _geonetwork 4_. New installs of _georchestra_ removes the authentication panel in the embedded _Geonetwork 4_ (see [#187](https://github.com/georchestra/geonetwork/pull/187)).

For existing _Geonetwork 4_ installations, it can be done by unticking the checkbox in the `Admin console`->`User Interface`:
```
☐ Authentication
```
