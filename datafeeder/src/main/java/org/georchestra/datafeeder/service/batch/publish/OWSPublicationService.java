package org.georchestra.datafeeder.service.batch.publish;

import org.georchestra.datafeeder.model.DataUploadJob;

public interface OWSPublicationService {

    void publishDatasets(DataUploadJob job);

    void addMetadataLinks(DataUploadJob job);

}
