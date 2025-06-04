package org.georchestra.datafeeder.service.geonetwork;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import lombok.NonNull;
import org.georchestra.datafeeder.model.UserInfo;

public interface GeoNetworkClient {

    void setApiUrl(URL apiUrl);

    void setBasicAuth(String username, String password);

    void setHeadersAuth(Map<String, String> authHeaders);

    void checkServiceAvailable() throws IOException;

    GeoNetworkResponse putXmlRecord(@NonNull String metadataId, @NonNull String xmlRecord, String groupName,
            UserInfo user, Boolean publishToAll, Boolean orgBasedSync);

    String getXmlRecord(@NonNull String recordId);

}
