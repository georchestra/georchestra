# Data.gouv.fr specificities

In Geonetwork, some modifications are required to ensure that the metadata quality score can reach 100%.

You can find the [patch file here](assets/dcat_data_gouv_fr_output.patch) to see full details of the modifications.

The patch file is applied to the `tpl-rdf.xsl` file located in the `WEB-INF/data/config/schema_plugins/iso19139/layout/tpl-rdf.xsl` directory of the Geonetwork application.

## Developer corner

If you want to try modifications locally, you can setup a local [data.gouv](https://github.com/opendatateam/udata/).

- Deploy udata using the [docker composition](https://github.com/opendatateam/docker-udata/) 
- Configure the harvesting plugins to load on startup : 
  - add `PLUGINS = ['front', 'csw-dcat', 'csw-iso-19139', 'dcat', 'recommendations']` in `udata.cfg`
- Build the image and configure it following https://udata.readthedocs.io/en/latest/quickstart/ 
- Start the Geonetwork local instance 
- You can use ngrok to get a public URL to the GN app
  - Declare this ngrok path for the harvester config 
