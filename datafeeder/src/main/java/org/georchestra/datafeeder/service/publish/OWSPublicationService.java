package org.georchestra.datafeeder.service.publish;

import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.PublishSettings;

public interface OWSPublicationService {

    /**
     * Publishes the given dataset to the OWS Server.
     * <p>
     * Once this method returns, {@link PublishSettings#getPublishedName()
     * dataset.getPublishing().getPublishedName()} may have changed by the service
     * to avoid layer name duplication, and
     * {@link PublishSettings#getPublishedWorkspace()
     * dataset.getPublishing().getPublishedWorkspace()} must not be {@code null}
     */
    void publish(DatasetUploadState dataset);

    /**
     * Updates the published dataset metadata on the OWS service for the published
     * layer given by the {@link DatasetUploadState}'s published
     * {@link PublishSettings#getPublishedWorkspace() workspace name} and
     * {@link PublishSettings#getPublishedName() layer name}, adding a metadata-link
     * pointing to the metadata {@link PublishSettings#getMetadataRecordId() record
     * id}.
     */
    void addMetadataLink(DatasetUploadState dataset);

}
