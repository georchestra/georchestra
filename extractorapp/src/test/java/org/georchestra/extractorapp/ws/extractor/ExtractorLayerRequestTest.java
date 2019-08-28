/**
 * 
 */
package org.georchestra.extractorapp.ws.extractor;

import static org.junit.Assert.*;

import java.net.URL;

import org.json.JSONObject;
import org.junit.Test;

/**
 * @author fgravin
 *
 */
public class ExtractorLayerRequestTest {

    @Test
    public void testExtractorLayerRequest() throws Exception {

        final String owsUrl = "http://www.geopicardie.fr/geoserver/wcs?";
        final String owsType = "WCS";
        final String owsVersion = "1.0.0";
        final String email = "test@gmail.com";
        final String projection = "EPSG:2154";
        final double resolution = 0.5;
        final String format = "geotiff";
        final String layerName = "geopicardie:somme_ortho_2008_vis";

        JSONObject configJson = new JSONObject("{\"emails\":[\"" + email + "\"],"
                + "\"globalProperties\":{\"projection\":\"" + projection + "\",\"resolution\":" + resolution
                + ",\"rasterFormat\":\"" + format
                + "\",\"vectorFormat\":\"shp\",\"bbox\":{\"srs\":\"EPSG:2154\",\"value\":[500000,6870000,800000,7110000]}},"
                + "\"layers\":[{\"projection\":\"EPSG:2154\",\"resolution\":null,\"format\":\"" + format
                + "\",\"bbox\":{\"srs\":\"CRS:84\",\"value\":[2.0774392511928,50.025833789344,2.0813661495014,50.028618317235]},\"owsUrl\":\""
                + owsUrl + "\",\"owsType\":\"" + owsType + "\",\"layerName\":\"" + layerName + "\"}]}");

        ExtractorLayerRequest elr = new ExtractorLayerRequest(configJson.getJSONArray("layers").getJSONObject(0),
                configJson.getJSONObject("globalProperties"), configJson.getJSONArray("emails"));

        assertEquals(elr._url.toString(), owsUrl);
        assertEquals(elr._emails.length, 1);
        assertEquals(elr._emails[0], email);
        assertEquals(elr._epsg, projection);
        assertEquals(elr._resolution, resolution, 0.01);
        assertEquals(elr._format, format);
        assertEquals(elr._layerName, layerName);
        assertEquals(elr._bbox.getMinX(), 2.0774392511928, 0.00000000001);
        assertEquals(elr._bbox.getMinY(), 50.025833789344, 0.00000000001);
        assertEquals(elr._bbox.getMaxX(), 2.0813661495014, 0.00000000001);
        assertEquals(elr._bbox.getMaxY(), 50.028618317235, 0.00000000001);

        URL url = elr.capabilitiesURL(owsType, owsVersion);
        assertEquals(owsUrl + "REQUEST=GETCAPABILITIES&SERVICE=" + owsType + "&VERSION=" + owsVersion, url.toString());
    }

}
