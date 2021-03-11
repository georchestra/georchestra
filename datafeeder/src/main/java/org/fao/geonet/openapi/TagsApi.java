package org.fao.geonet.openapi;

import java.util.List;

import org.fao.geonet.ApiClient;
import org.fao.geonet.openapi.model.MetadataCategory;
import org.fao.geonet.openapi.model.ResponseEntity;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface TagsApi extends ApiClient.Api {

    /**
     * Remove a tag
     * 
     * @param tagIdentifier Tag identifier (required)
     * @return ResponseEntity
     */
    @RequestLine("DELETE /srv/api/0.1/tags/{tagIdentifier}")
    @Headers({ "Accept: */*", })
    ResponseEntity deleteTag(@Param("tagIdentifier") Integer tagIdentifier);

    /**
     * Get a tag
     * 
     * @param tagIdentifier Tag identifier (required)
     * @return MetadataCategory
     */
    @RequestLine("GET /srv/api/0.1/tags/{tagIdentifier}")
    @Headers({ "Accept: application/json", })
    MetadataCategory getTag(@Param("tagIdentifier") Integer tagIdentifier);

    /**
     * Get tags
     * 
     * @return List&lt;MetadataCategory&gt;
     */
    @RequestLine("GET /srv/api/0.1/tags")
    @Headers({ "Accept: application/json", })
    List<MetadataCategory> getTags();

    /**
     * Create a tag If labels are not defined, a default label is created with the
     * category name for all languages.
     * 
     * @param category category (optional)
     * @return Integer
     */
    @RequestLine("PUT /srv/api/0.1/tags")
    @Headers({ "Content-Type: application/json", "Accept: */*", })
    Integer putTag(MetadataCategory category);

    /**
     * Update a tag
     * 
     * @param tagIdentifier Tag identifier (required)
     * @param category      category (optional)
     * @return ResponseEntity
     */
    @RequestLine("PUT /srv/api/0.1/tags/{tagIdentifier}")
    @Headers({ "Content-Type: application/json", "Accept: */*", })
    ResponseEntity updateTag(@Param("tagIdentifier") Integer tagIdentifier, MetadataCategory category);
}
