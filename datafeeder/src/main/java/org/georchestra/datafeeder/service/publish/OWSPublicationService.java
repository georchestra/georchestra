package org.georchestra.datafeeder.service.publish;

import org.georchestra.datafeeder.model.DatasetUploadState;

public interface OWSPublicationService {

    void publish(DatasetUploadState dataset);

    void addMetadataLink(DatasetUploadState dataset);

}
