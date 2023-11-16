## Metadata record building

| Request header | GeorchestraUserDetails | DatasetUploadState | MetadataRecordProperties | Default metadata record target |
| -----------| ------------------ | ----------------- | ----------------- | ----------------- |
| sec-username | username |  |  |  |
| sec-roles | roles |  |  |  |
| sec-firstname | firstName |  |  - datasetResponsibleParty.individualName<br>- metadataResponsibleParty.individualName|  |
| sec-lastname | lastName |  |   - datasetResponsibleParty.individualName<br>- metadataResponsibleParty.individualName |  |
| sec-email | email |  | - datasetResponsibleParty.email<br>- metadataResponsibleParty.email |  |
| sec-tel | telephoneNumber |  |  |  |
| sec-address | postalAddress |  | metadataResponsibleParty.deliveryPoint |  |
| sec-title | title |  |  |  |
| sec-notes | notes |  |  |  |
| sec-org | org.id |  | - datasetResponsibleParty.name<br>- metadataResponsibleParty.name |  |
| sec-orgname | org.name |  | - datasetResponsibleParty.organizationName<br>- metadataResponsibleParty.organizationName|  |
| sec- org-linkage| org.linkage |  | - datasetResponsibleParty.linkage<br>- metadataResponsibleParty.linkage |  |
| sec-org-address | org.postalAddress |  | datasetResponsibleParty.deliveryPoint |  |
| sec-org-category | org.category |  |  |  |
| sec-org-description | org.description |  |  |  |
| |  | metadataRecordId | metadataId | gmd:fileIdentifier/gco:CharacterString |
| |  | importedName |  |  |
| |  | publishedWorkspace |  |  |
| |  | publishing.publishedName | name |  |
| |  | publishing.title | title | gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString |
| |  | publishing.abstract | abstract | gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString |
| |  | publishing.keywords[] | keywords[] |  gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword[]/gco:CharacterString |
| |  | publishing.encoding | charsetEncoding |  |
| |  | publishing.srs | coordinateReferenceSystem |  |
| |  | publishing.srsReproject |  |  |
| |  | publishing.geographicBoundingBox.minx | westBoundLongitude | //gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude |
| |  | publishing.geographicBoundingBox.miny | southBoundLatitude | //gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude |
| |  | publishing.geographicBoundingBox.maxx | northBoundLatitude | //gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude |
| |  | publishing.geographicBoundingBox.maxy | eastBoundLongitude | //gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude |
| |  |  | spatialRepresentation = `vector` | //gmd:MD_DataIdentification/gmd:spatialRepresentationType/gmd:MD_SpatialRepresentationTypeCode/@codeListValue |
| |  | publishing.scale | spatialResolution | //gmd:MD_DataIdentification/gmd:spatialResolution/gmd:MD_Resolution/gmd:equivalentScale/gmd:MD_RepresentativeFraction/gmd:denominator/gco:Integer |
| |  |  | metadataPublicationDate  = `LocalDateTime.now()` | //gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date[2]/gmd:CI_Date/gmd:date/gco:Date |
| |  |  | metadataTimestamp  = `LocalDateTime.now()` | gmd:dateStamp/gco:DateTime |
| |  | publishing.<br>datasetCreationDate | creationDate | //gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date[1]/gmd:CI_Date/gmd:date/gco:Date |
| |  | publishing.dataset-<br>CreationProcessDescription | lineage |  |
| |  |  | resourceType = `dataset` |  |
| |  |  | dataIdentifier = `<gn-url>` + metadataId | //gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString |
| |  |  | datasetLanguage = `eng` |  |
| |  |  | metadataLanguage = `eng` | gmd:language/gmd:LanguageCode/@codeListValue |
| |  |  | distributionFormat = `ESRI Shapefile`|  |
| |  |  | distributionFormatVersion = `1.0` |  |
| |  |  | useLimitation  = `ODBL` |  |
| |  |  | useConstraints  = `license` |  |
| |  |  | useConstraints  = `license` |  |
| |  |  | accessConstraints  = `otherRestrictions` |  |
| |  |  | updateFequency = `asNeeded` |  |
| |  |  | onlineResources[0]<br>.protocol = `OGC:WMS` |  |
| |  |  | onlineResources[0]<br>.description = publishing.title + ` - WMS`|  |
| |  |  | onlineResources[0].linkage = <br>`<geoserver-url>` +<br>publishing.publishedWorkspace +<br>`?SERVICE=WMS&REQUEST=GetCapabilities` |  |
| |  |  | onlineResources[1]<br>.protocol = `OGC:WFS` |  |
| |  |  | onlineResources[1]<br>.description = publishing.title + ` - WFS`|  |
| |  |  | onlineResources[1].linkage = <br>`<geoserver-url>` +<br>publishing.publishedWorkspace +<br>`?SERVICE=WFS&REQUEST=GetCapabilities` |  |
| |  |  | onlineResources[2]<br>.protocol = `OGC:WFS` |  |
| |  |  | onlineResources[2]<br>.description = publishing.title + ` - WFS`|  |
| |  |  | onlineResources[2].linkage = <br>`<geoserver-url>` +<br>publishing.publishedWorkspace +<br>`?SERVICE=WFS&REQUEST=GetCapabilities` |  |
