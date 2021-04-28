# Email message templates

Message template locations are to be configured in `datafeeder.propeties` using the following properties:

```
datafeeder.email.ackTemplate=file:${georchestra.datadir}/datafeeder/templates/analysis-started-email-template.txt
datafeeder.email.analysisFailedTemplate=file:${georchestra.datadir}/datafeeder/templates/analysis-failed-email-template.txt
datafeeder.email.publishFailedTemplate=file:${georchestra.datadir}/datafeeder/templates/data-publishing-failed-email-template.txt
datafeeder.email.publishSuccessTemplate=file:${georchestra.datadir}/datafeeder/templates/data-publishing-succeeded-email-template.txt
```

A message template consists of a number of one-line header parts, followed by the full message body.

Variables on a message template are specified using `${variable-name}` notation, like in the following example:

```
to: ${user.email}
cc: ${administratorEmail}
bcc:
sender: ${administratorEmail}
from: Georchestra Importer Application
subject:
body:

Dear ${user.name}, 
....
```

The following variables are resolved against the job's user, dataset, or publishing attributes:

* `${user.name}`:
* `${user.lastName}`:
* `${user.email}`:
* `${job.id}`:
* `${job.createdAt}`:
* `${job.error}`:
* `${job.analizeStatus}`:
* `${job.publishStatus}`:
* `${dataset.name}`:
* `${dataset.featureCount}`:
* `${dataset.encoding}`:
* `${dataset.nativeBounds}`:
* `${publish.tableName}`:
* `${publish.layerName}`:
* `${publish.workspace}`:
* `${publish.srs}`:
* `${publish.encoding}`:
* `${metadata.id}`:
* `${metadata.title}`:
* `${metadata.abstract}`:
* `${metadata.creationDate}`:
* `${metadata.lineage}`:
* `${metadata.latLonBoundingBox}`:
* `${metadata.keywords}`:
* `${metadata.scale}`:

Additionally, any other <code>${property}</code> will be resolved against the application context 
(for example, any property specified in `default.properties` or `datafeeder.properties`).
