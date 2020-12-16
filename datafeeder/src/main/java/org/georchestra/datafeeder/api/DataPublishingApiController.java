package org.georchestra.datafeeder.api;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.NativeWebRequest;

import io.swagger.annotations.Api;

@Controller
@Api(tags = { "Data Publishing" }) // hides the empty data-publishing-api-controller entry in swagger-ui.html
public class DataPublishingApiController implements DataPublishingApi {

    private @Autowired NativeWebRequest currentRequest;

    public @Override Optional<NativeWebRequest> getRequest() {
        return Optional.of(currentRequest);
    }

}
