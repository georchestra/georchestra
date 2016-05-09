package org.georchestra.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.client.fluent.Request;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * This class is used to check availability of several services of geOrchestra stack
 *
 * Services status can be checked with following path :
 *
 * /services_monitoring
 *
 * This page display status of services and global http code can be tested. If /services_monitoring respond with HTTP
 * 200 Code then services should be OK otherwise this page will return a HTTP 500 code with description of services not
 * available.
 *
 */

public class ServicesMonitoring {

    private Properties mappings;

    public ServicesMonitoring(Properties mappings) throws IOException {
        this.mappings = mappings;
    }

    public void checkServices(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Boolean success = true;
        PrintWriter writer = response.getWriter();
        writer.write("<html><head><title>geOrchestra services monitoring</title>");
        writer.write("<meta charset=\"utf-8\"/>");
        writer.write("<style>.success { color: green } .fail { color: red } div { padding: 10px }</style>");
        writer.write("</head><body>");

        // Test geonetwork
        try {
            this.checkGeonetwork();
            writer.write(this.writeSuccess("Geonetwork"));
        } catch (Exception e) {
            success = false;
            writer.write(this.writeFailure("Geonetwork", e));
        }

        // Test geoserver
        try {
            this.checkGeoserver();
            writer.write(this.writeSuccess("Geoserver"));
        } catch (Exception e) {
            success = false;
            writer.write(this.writeFailure("Geoserver", e));
        }

        // Test header
        try {
            this.checkHeader();
            writer.write(this.writeSuccess("Header"));
        } catch (Exception e) {
            success = false;
            writer.write(this.writeFailure("Header", e));
        }

        if(success)
            response.setStatus(200);
        else
            response.setStatus(500);

        writer.write("</body></html>");
    }

    private String checkGeonetwork() throws Exception {
        String geonetworkTestURL = this.mappings.getProperty("geonetwork");
        geonetworkTestURL += "srv/eng/csw?service=CSW&version=2.0.2&request=GetRecords";
        geonetworkTestURL += "&constraintlanguage=CQL_TEXT&typeNames=csw:Record";

        return this.grepUrl(geonetworkTestURL,"SearchResults");
    }

    private String checkGeoserver() throws Exception {
        String geoserverURL = this.mappings.getProperty("geoserver");
        geoserverURL += "wms?SERVICE=WMS&REQUEST=GetCapabilities";

        return this.grepUrl(geoserverURL,"WMS_Capabilities");
    }

    private String checkHeader() throws Exception{
        String headerURL = this.mappings.getProperty("header");
        return this.grepUrl(headerURL,"html");
    }

    // Helpers
    // --------

    private String grepUrl(String url, String searchTerm) throws Exception {
        String response = Request.Get(url).execute().returnContent().asString();
        if (!response.contains(searchTerm))
            throw new Exception("Unable to find '"+ searchTerm + "' on " + url);
        return response;
    }

    private String writeSuccess(String service){
        return "<div class=\"success\">" + service + " OK</div>";
    }

    private String writeFailure(String service, Exception e){
        return "<div class=\"fail\">" + service + " FAIL : " + e.getClass().getName() + ": " + e.getMessage() + "</div>";
    }

}
