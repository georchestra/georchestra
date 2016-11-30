/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.ldapadmin.ws.backoffice.orgs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.georchestra.ldapadmin.ds.DataServiceException;
import org.georchestra.ldapadmin.ds.OrgsDao;
import org.georchestra.ldapadmin.dto.Org;
import org.georchestra.ldapadmin.dto.OrgExt;
import org.georchestra.ldapadmin.ws.backoffice.utils.ResponseUtil;
import org.georchestra.ldapadmin.ws.utils.Validation;
import org.georchestra.lib.file.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Controller
public class OrgsController {

    private static final Log LOG = LogFactory.getLog(OrgsDao.class.getName());

    private static final String BASE_MAPPING = "/private";
    private static final String BASE_RESOURCE = "orgs";
    private static final String REQUEST_MAPPING = BASE_MAPPING + "/" + BASE_RESOURCE;
    private static final String PUBLIC_REQUEST_MAPPING = "/public/" + BASE_RESOURCE;

    @Autowired
    private OrgsDao orgDao;

    @Autowired
    private Validation validation;

    @Autowired
    private GeorchestraConfiguration georConfig;

    @Autowired
    public OrgsController(OrgsDao dao) {
        this.orgDao = dao;
    }

    /**
     * Return a list of available organization as json array
     *
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = REQUEST_MAPPING, method = RequestMethod.GET)
    public void findAll(HttpServletResponse response) throws IOException {

        try {
            List<Org> orgs = this.orgDao.findAll();
            JSONArray res = new JSONArray();
            for (Org org : orgs)
                res.put(org.toJson());

            ResponseUtil.buildResponse(response, res.toString(4), HttpServletResponse.SC_OK);

        } catch (Exception e) {
            LOG.error(e.getMessage());
            ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(false, e.getMessage()),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            throw new IOException(e);
        }


    }

    /**
     * Set organization for one user
     */
    @RequestMapping(value = REQUEST_MAPPING + "/{org}/{user:.+}", method = RequestMethod.POST)
    public void addUserInOrg(@PathVariable String org, @PathVariable String user, HttpServletResponse response) throws IOException {

        try {
            Org oldOrg = this.orgDao.findForUser(user);
            if (oldOrg != null)
                this.orgDao.removeUser(oldOrg.getId(), user);
            this.orgDao.addUser(org, user);

            ResponseUtil.writeSuccess(response);

        } catch (DataServiceException ex){
            LOG.error(ex.getMessage());
            ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(false, ex.getMessage()),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * Remove user from organization
     */
    @RequestMapping(value = REQUEST_MAPPING + "/{org}/{user:.+}", method = RequestMethod.DELETE)
    public void removeUserfromOrg(@PathVariable String org, @PathVariable String user, HttpServletResponse response) throws IOException {
        this.orgDao.removeUser(org, user);
        ResponseUtil.writeSuccess(response);
    }


    /**
     * Retreive full information about one org as JSON document. Following keys will be available :
     *
     * * 'id' (not used)
     * * 'name'
     * * 'shortName'
     * * 'cities' as json array ex: [654,865498,98364,9834534,984984,6978984,98498]
     * * 'status'
     * * 'orgType'
     * * 'address'
     * * 'members' as json array ex: ["testadmin", "testuser"]
     *
     */
    @RequestMapping(value = REQUEST_MAPPING + "/{cn}", method = RequestMethod.GET)
    public void getOrgInfos(@PathVariable String cn, HttpServletResponse response) throws IOException {

        try {
            Org org = this.orgDao.findByCommonName(cn);
            OrgExt orgExt = this.orgDao.findExtById(cn);
            JSONObject res = this.encodeToJson(org, orgExt);
            ResponseUtil.buildResponse(response, res.toString(4), HttpServletResponse.SC_OK);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(false, e.getMessage()),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            throw new IOException(e);
        }

    }


    /**
     * Update information of one org
     *
     * Request should contain Json formated document containing following keys :
     *
     * * 'name'
     * * 'shortName'
     * * 'cities' as json array ex: [654,865498,98364,9834534,984984,6978984,98498]
     * * 'status'
     * * 'orgType'
     * * 'address'
     * * 'members' as json array ex: ["testadmin", "testuser"]
     *
     * All fields are optional.
     *
     * Full json example :
     *  {
     *     "name" : "Office national des forêts",
     *     "shortName" : "ONF",
     *     "cities" : [
     *        654,
     *        865498,
     *        98364,
     *        9834534,
     *        984984,
     *        6978984,
     *        98498
     *     ],
     *     "status" : "inscrit",
     *     "type" : "association",
     *     "address" : "128 rue de la plante, 73059 Chambrille",
     *     "members": [
     *        "testadmin",
     *        "testuser"
     *     ]
     *  }
     *
     */
    @RequestMapping(value = REQUEST_MAPPING + "/{commonName}", method = RequestMethod.PUT)
    public void updateOrgInfos(@PathVariable String commonName,
                               HttpServletRequest request, HttpServletResponse response)
            throws IOException, JSONException {

        try {
            // Parse Json
            JSONObject json = this.parseRequest(request, response);

            // Validate request against required fields for admin part
            if (!this.validation.validateOrgField("name", json))
                throw new IOException("required field : name");

            // Retrieve current orgs state from ldap
            Org org = this.orgDao.findByCommonName(commonName);
            OrgExt orgExt = this.orgDao.findExtById(commonName);

            // Update org and orgExt fields
            this.updateFromRequest(org, json);
            this.updateFromRequest(orgExt, json);

            // Persist changes to LDAP server
            this.orgDao.update(org);
            this.orgDao.update(orgExt);

            // Regenerate json and send it to browser
            this.returnOrgAsJSON(org, orgExt, response);

        } catch (Exception e) {
            LOG.error(e.getMessage());
            ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(false, e.getMessage()),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            throw new IOException(e);
        }

    }

    /**
     * Create a new org based on JSON document sent by browser. JSON document may contain following keys :
     *
     * * 'name' (mandatory)
     * * 'shortName'
     * * 'cities' as json array ex: [654,865498,98364,9834534,984984,6978984,98498]
     * * 'status'
     * * 'type'
     * * 'address'
     * * 'members' as json array ex: ["testadmin", "testuser"]
     *
     * All fields are optional except 'name' which is used to generate organization identifier.
     *
     * A new JSON document will be return to browser with a complete description of created org. @see updateOrgInfos()
     * for JSON format.
     */
    @RequestMapping(value = REQUEST_MAPPING, method = RequestMethod.POST)
    public void createOrg(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException {

        try {
            // Parse Json
            JSONObject json = this.parseRequest(request, response);

            // Check required fields
            for(String field : this.validation.getRequiredOrgFields())
                if(!json.has(field) || !StringUtils.hasLength(json.getString(field)))
                    throw new Exception(field + " required");

            Org org = new Org();
            OrgExt orgExt = new OrgExt();

            // Generate string identifier based on name
            String id = this.orgDao.generateId(json.getString(Org.JSON_NAME));
            org.setId(id);
            orgExt.setId(id);

            // Generate unique numeric identifier
            orgExt.setNumericId(this.orgDao.generateNumericId());

            // Update org and orgExt fields
            this.updateFromRequest(org, json);
            this.updateFromRequest(orgExt, json);

            // Validate org
            org.setStatus(Org.STATUS_REGISTERED);

            // Persist changes to LDAP server
            this.orgDao.insert(org);
            this.orgDao.insert(orgExt);

            // Regenerate json and send it to browser
            this.returnOrgAsJSON(org, orgExt, response);

        } catch (Exception e) {
            LOG.error(e.getMessage());
            ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(false, e.getMessage()),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            throw new IOException(e);
        }
    }


    /**
     * Delete one org
     */
    @RequestMapping(value = REQUEST_MAPPING + "/{commonName}", method = RequestMethod.DELETE)
    public void deleteOrg(@PathVariable String commonName, HttpServletResponse response)
            throws IOException, JSONException {

        try {
            // delete entities in LDAP server
            this.orgDao.delete(this.orgDao.findExtById(commonName));
            this.orgDao.delete(this.orgDao.findByCommonName(commonName));
            ResponseUtil.writeSuccess(response);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(false, e.getMessage()),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new IOException(e);
        }
    }

    /**
     * Return a list of required fields for org creation
     *
     * return a JSON array with required fields. Possible values :
     *
     * * 'shortName'
     * * 'address'
     * * 'type'
     */
    @RequestMapping(value = PUBLIC_REQUEST_MAPPING + "/requiredFields", method = RequestMethod.GET)
    public void getUserRequiredFields(HttpServletResponse response) throws IOException{
        try {
            JSONArray fields = new JSONArray();
            fields.put("name");
            ResponseUtil.buildResponse(response, fields.toString(4), HttpServletResponse.SC_OK);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(false, e.getMessage()),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new IOException(e);
        }
    }

    /**
     * Return a list of possible values for organization type
     *
     * return a JSON array with possible value
     */
    @RequestMapping(value = PUBLIC_REQUEST_MAPPING +"/orgTypeValues", method = RequestMethod.GET)
    public void getOrgTypeVAlues(HttpServletResponse response) throws IOException{
        try {
            JSONArray fields = new JSONArray();
            for(String field : this.orgDao.getOrgTypeValues())
                fields.put(field);
            ResponseUtil.buildResponse(response, fields.toString(4), HttpServletResponse.SC_OK);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(false, e.getMessage()),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new IOException(e);
        }
    }

    /**
     * Return configuration areas UI as json object. Configuration includes following values :
     *
     * - inital map center
     * - initial map zoom
     * - ows service to retrieve area geometry 'url'
     * - attribute to use as label 'value'
     * - attribute to use to group area 'group'
     * - attribute to use as identifier 'key'
     *
     * Ex :
     * {
     *   "map" : { "center": [49.5468, 5.123486], "zoom": 8},
     *   "areas" : { "url": "http://sdi.georchestra.org/geoserver/....;",
     *               "key": "insee_code",
     *               "value": "commune_name",
     *               "group": "department_name"}
     * }
     */

    @RequestMapping(value = PUBLIC_REQUEST_MAPPING + "/areaConfig.json", method = RequestMethod.GET)
    public void getAreaConfig(HttpServletResponse response) throws IOException, JSONException {
        JSONObject res = new JSONObject();
        JSONObject map = new JSONObject();
        // Parse center
        String[] rawCenter = this.georConfig.getProperty("AreaMapCenter").split("\\s*,\\s*");
        JSONArray center = new JSONArray();
        center.put(Double.parseDouble(rawCenter[0]));
        center.put(Double.parseDouble(rawCenter[1]));
        map.put("center", center);
        map.put("zoom", this.georConfig.getProperty("AreaMapZoom"));
        res.put("map", map);
        JSONObject areas = new JSONObject();
        areas.put("url", this.georConfig.getProperty("AreasUrl"));
        areas.put("key", this.georConfig.getProperty("AreasKey"));
        areas.put("value", this.georConfig.getProperty("AreasValue"));
        areas.put("group", this.georConfig.getProperty("AreasGroup"));
        res.put("areas", areas);
        ResponseUtil.buildResponse(response, res.toString(4), HttpServletResponse.SC_OK);
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleException(Exception e, HttpServletResponse response) throws JSONException, IOException {
        LOG.error(e.getMessage());
        ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(false, e.getMessage()),
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        throw new IOException(e);
    }

    /**
     * Update org instance based on field found in json object.
     *
     * All field of Org instance will be updated if corresponding key exists in json document except 'members'.
     *
     * @param org Org instance to update
     * @param json Json document to take information from
     * @throws JSONException If something went wrong during information extraction from json document
     */
    private void updateFromRequest(Org org, JSONObject json) throws JSONException {

        try{
            org.setName(json.getString("name"));
        } catch (JSONException ex){}

        try{
            org.setShortName(json.getString("shortName"));
        } catch (JSONException ex){}

        try{
            JSONArray cities = json.getJSONArray("cities");
            List<String> parsedCities = new LinkedList<String>();
            for(int i = 0; i < cities.length(); i++)
                parsedCities.add(cities.getString(i));
            org.setCities(parsedCities);
        } catch (JSONException ex){}

        try{
            JSONArray members = json.getJSONArray("members");
            List<String> parsedMembers = new LinkedList<String>();
            for(int i = 0; i < members.length(); i++)
                parsedMembers.add(members.getString(i));
            org.setMembers(parsedMembers);
        } catch (JSONException ex){}


        try{
            org.setStatus(json.getString("status"));
        } catch (JSONException ex){}

    }


    /**
     * Update orgExt instance based on field found in json object.
     *
     * All field of OrgExt instance will be updated if corresponding key exists in json document.
     *
     * @param orgExt OrgExt instance to update
     * @param json Json document to take information from
     * @throws JSONException If something went wrong during information extraction from json document
     */
    private void updateFromRequest(OrgExt orgExt, JSONObject json) throws JSONException {

        try{
            orgExt.setOrgType(json.getString("type"));
        } catch (JSONException ex){}

        try{
            orgExt.setAddress(json.getString("address"));
        } catch (JSONException ex){}

    }


    /**
     * Create a json document containing all information (org and org Ext) about organization
     *
     * @param org instance of Org
     * @param orgExt instance of OrgExt
     * @return json object containing informations from Org and OrgExt objects
     * @throws JSONException if error occurs when encoding value to json format
     */
    private JSONObject encodeToJson(Org org, OrgExt orgExt) throws JSONException {

        JSONObject res = org.toJson();
        if(orgExt.getOrgType() != null)
            res.put("type", orgExt.getOrgType());
        if(orgExt.getAddress() != null)
            res.put("address", orgExt.getAddress());
        return res;

    }

    /**
     * Parse request payload and return a JSON document
     *
     * @param request
     * @param response used to send error to browser
     * @return JSON document corresponding to browser request
     * @throws IOException if error occurs during JSON parsing
     */
    private JSONObject parseRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            return new JSONObject(FileUtils.asString(request.getInputStream()));
        } catch (JSONException e) {
            LOG.error(e.getMessage());
            ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(false, e.getMessage()),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new IOException(e);
        }

    }


    private void returnOrgAsJSON(Org org, OrgExt orgExt, HttpServletResponse response) throws IOException {
        try {
            JSONObject res = this.encodeToJson(org, orgExt);
            ResponseUtil.buildResponse(response, res.toString(4), HttpServletResponse.SC_OK);
        } catch (Exception e) {
            LOG.error(e.getMessage());
            ResponseUtil.buildResponse(response, ResponseUtil.buildResponseMessage(false, e.getMessage()),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            throw new IOException(e);
        }
    }


    public GeorchestraConfiguration getGeorConfig() {
        return georConfig;
    }

    public void setGeorConfig(GeorchestraConfiguration georConfig) {
        this.georConfig = georConfig;
    }
}
