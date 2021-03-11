package org.fao.geonet.openapi;

import java.util.List;

import org.fao.geonet.ApiClient;
import org.fao.geonet.openapi.model.Service;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface CswApi extends ApiClient.Api {

    /**
     * Add a virtual CSW The service name MUST be unique. An exception is returned
     * if not the case.
     * 
     * @param service Service details (required)
     * @return Integer
     */
    @RequestLine("PUT /srv/api/0.1/csw/virtuals")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    Integer addVirtualCsw(Service service);

    /**
     * Remove a virtual CSW After removal, all virtual CSW configuration is
     * reloaded.
     * 
     * @param identifier Service identifier (required)
     */
    @RequestLine("DELETE /srv/api/0.1/csw/virtuals/{identifier}")
    @Headers({ "Accept: application/json", })
    void deleteVirtualCsw(@Param("identifier") Integer identifier);

    /**
     * Get virtual CSW services Virtual CSWs are created to easily setup services
     * providing access to records without the need to define filters. For example,
     * in Europe, local, regional and national organizations define entry point for
     * records in the scope of the INSPIRE directive. Those services can then be
     * easily harvested to exchange information. Virtual CSWs do not support
     * transaction. For this use the main catalog CSW service.
     * 
     * @return List&lt;Service&gt;
     */
    @RequestLine("GET /srv/api/0.1/csw/virtuals")
    @Headers({ "Accept: application/json", })
    List<Service> getAllVirtualCsw();

    /**
     * Get a virtual CSW
     * 
     * @param identifier Service identifier (required)
     * @return Service
     */
    @RequestLine("GET /srv/api/0.1/csw/virtuals/{identifier}")
    @Headers({ "Accept: application/json", })
    Service getVirtualCsw(@Param("identifier") Integer identifier);

    /**
     * Update a virtual CSW
     * 
     * @param identifier Service identifier (required)
     * @param service    Service details (required)
     */
    @RequestLine("PUT /srv/api/0.1/csw/virtuals/{identifier}")
    @Headers({ "Content-Type: application/json", "Accept: application/json", })
    void updateVirtualCsw(@Param("identifier") Integer identifier, Service service);
}
