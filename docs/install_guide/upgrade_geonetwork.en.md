# Upgrading geonetwork

those are generally the steps to check/remember to do when upgrading
geonetwork, be it for a major or a minor version, eg more or less applies for
3.8->4.0, 4.0->4.2, or 4.2.2->4.2.4.

## in the geonetwork data directory

* several files should be updated with the ones from the webapp:
  * the configuration of the elasticsearch index
  * the schemas
  * the formatters used by the complete view
```
/srv/data/geonetwork# rm -Rf config/schema_plugins data/formatter
/srv/data/geonetwork# cp -r /srv/tomcat/georchestra/webapps/geonetwork/WEB-INF/data/config/schema_plugins config/
/srv/data/geonetwork# cp -r /srv/tomcat/georchestra/webapps/geonetwork/WEB-INF/data/config/index config/
/srv/data/geonetwork# cp -r /srv/tomcat/georchestra/webapps/geonetwork/WEB-INF/data/data/formatter data/
```
* make sure those files can be overwritten by the user running tomcat/the webapp
```
/srv/data/geonetwork# chown -R tomcat:tomcat data/formatter config/schema_plugins
```
* if you use the https://github.com/georchestra/geonetwork_minimal_datadir
  repository, generally those changes are already available in the latest
  branch of the repository
* drop the `wro4j-cache.mv.db` file (that's equivalent to the 'clear js & css
  cache' button in the tools pane of the admin ui, but sometimes this page
  might not be accessible when upgrading from a previous version)
* restart/reload the webapp
* check the logs, because if there are database schema changes to apply, those
  should be done at the first start of the new version of the webapp

## in the geonetwork admin UI

* connected as admin, go to the *Tools* page in the *Admin console* menu
* hit *delete index and reindex* button. This will reset the elasticsearch
  index, update its configuration (the `records.json` file in the
  `config/index/` subdirectory of the geonetwork data directory) and rebuild
  the index
* go in the *user interface* pane of the *Settings* page in the *Admin console* menu
* hit the *Json form* button and *Current configuration*. This will show you
  what differs from the default configuration provided by the project.
* make a note of all those changes that were done previously, hit the 'Reset
  configuration' button. This will make sure the UI configuration uses the
  default provided by the georchestra project for its geonetwork fork.
* then, via the *Configuration form* tab, you can report the modifications you
  previously did in the UI config to your own taste.
* make sure that facets in search results do still work, and that you still have
  all your metadata :)
