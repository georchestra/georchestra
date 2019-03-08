/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

package org.georchestra.console.ws.backoffice.orgs;

import org.apache.commons.lang3.ArrayUtils;
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.georchestra.console.dao.AdvancedDelegationDao;
import org.georchestra.console.dao.DelegationDao;
import org.georchestra.console.ds.OrgsDao;
import org.georchestra.console.dto.orgs.Org;
import org.georchestra.console.dto.orgs.OrgDetail;
import org.georchestra.console.dto.orgs.OrgExt;
import org.georchestra.console.model.DelegationEntry;
import org.georchestra.console.ws.backoffice.utils.ResponseUtil;
import org.georchestra.console.ws.utils.Validation;
import org.georchestra.lib.file.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class OrgsController {

    private static final String BASE_MAPPING = "/private";
    private static final String BASE_RESOURCE = "orgs";
    private static final String REQUEST_MAPPING = BASE_MAPPING + "/" + BASE_RESOURCE;
    private static final String PUBLIC_REQUEST_MAPPING = "/public/" + BASE_RESOURCE;

    private static GrantedAuthority ROLE_SUPERUSER = new SimpleGrantedAuthority("ROLE_SUPERUSER");

    @Autowired
    private OrgsDao orgDao;

    @Autowired
    protected Validation validation;

    @Autowired
    private GeorchestraConfiguration georConfig;

    @Autowired
    protected DelegationDao delegationDao;

    @Autowired
    protected AdvancedDelegationDao advancedDelegationDao;

    @Autowired
    public OrgsController(OrgsDao dao) {
        this.orgDao = dao;
    }

    /**
     * Return a list of available organization as json array
     */
    @RequestMapping(value = REQUEST_MAPPING, method = RequestMethod.GET, produces="application/json; charset=utf-8")
    @PostFilter("hasPermission(filterObject, 'read')")
    @ResponseBody
    public List<Org> findAll(){
        List<Org> orgs = this.orgDao.findAll();
        Collections.sort(orgs);
        return orgs;
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
    @RequestMapping(value = REQUEST_MAPPING + "/{cn:.+}", method = RequestMethod.GET, produces="application/json; charset=utf-8")
    @ResponseBody
    public Org getOrgInfos(@PathVariable String cn){
        this.checkOrgAuthorization(cn);

        Org org = this.orgDao.findByCommonName(cn);
        OrgExt orgExt = this.orgDao.findExtById(cn);
        OrgDetail orgDetail = this.orgDao.findDetailById(cn);
        org.setOrgExt(orgExt);
        org.setOrgDetail(orgDetail);
        return org;
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
    @RequestMapping(value = REQUEST_MAPPING + "/{commonName:.+}", method = RequestMethod.PUT, produces="application/json; charset=utf-8")
    @ResponseBody
    public Org updateOrgInfos(@PathVariable String commonName, HttpServletRequest request)
            throws IOException, JSONException, SQLException {

        this.checkOrgAuthorization(commonName);

        // Parse Json
        JSONObject json = this.parseRequest(request);

        // Validate request against required fields for admin part
        if (!this.validation.validateOrgField("name", json))
            throw new IOException("required field : name");

        // Retrieve current orgs state from ldap
        Org org = this.orgDao.findByCommonName(commonName);
        OrgExt orgExt = this.orgDao.findExtById(commonName);
        OrgDetail orgDetail = this.orgDao.findDetailById(commonName);

        // Update org and orgExt fields
        this.updateFromRequest(org, json);
        orgExt.setId(org.getId());
        orgDetail.setId(org.getId());
        this.updateFromRequest(orgExt, json);
        this.updateFromRequest(orgDetail, json);
        // Persist changes to LDAP server
        this.orgDao.update(org);

        if (!commonName.equals(org.getId())) {
            for (DelegationEntry delegation : this.advancedDelegationDao.findByOrg(commonName)) {
                delegation.removeOrg(commonName);
                delegation.setOrgs(ArrayUtils.add(delegation.getOrgs(), org.getId()));
                this.delegationDao.save(delegation);
            }
        }



        this.orgDao.update(orgExt);
        this.orgDao.update(orgDetail);
        org.setOrgExt(orgExt);
        org.setOrgDetail(orgDetail);
        return org;
    }

    /**
     * Create a new org based on JSON document sent by browser. JSON document may contain following keys :
     *
     * * 'name'
     * * 'shortName' (mandatory)
     * * 'cities' as json array ex: [654,865498,98364,9834534,984984,6978984,98498]
     * * 'status'
     * * 'type'
     * * 'address'
     * * 'members' as json array ex: ["testadmin", "testuser"]
     *
     * All fields are optional except 'shortName' which is used to generate organization identifier.
     *
     * A new JSON document will be return to browser with a complete description of created org. @see updateOrgInfos()
     * for JSON format.
     */
    @RequestMapping(value = REQUEST_MAPPING, method = RequestMethod.POST, produces="application/json; charset=utf-8")
    @ResponseBody
    @PreAuthorize("hasRole('SUPERUSER')")
    public Org createOrg(HttpServletRequest request) throws IOException, JSONException {
        // Parse Json
        JSONObject json = this.parseRequest(request);

        // Validate request against required fields for admin part
        if (!this.validation.validateOrgField("shortName", json))
            throw new IOException("required field : shortName");

        Org org = new Org();
        org.setId("");
        this.updateFromRequest(org, json);

        OrgExt orgExt = new OrgExt();
        orgExt.setId(org.getId());
        this.updateFromRequest(orgExt, json);

        OrgDetail orgDetail = new OrgDetail();
        orgDetail.setId(org.getId());
        this.updateFromRequest(orgDetail, json);

        // Persist changes to LDAP server
        this.orgDao.insert(org);
        this.orgDao.insert(orgExt);
        this.orgDao.insert(orgDetail);

        org.setOrgExt(orgExt);
        org.setOrgDetail(orgDetail);

        return org;
    }


    /**
     * Delete one org
     */
    @RequestMapping(value = REQUEST_MAPPING + "/{commonName:.+}", method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('SUPERUSER')")
    public void deleteOrg(@PathVariable String commonName, HttpServletResponse response) throws IOException, SQLException {

        // Check if this role is part of a delegation
        for(DelegationEntry delegation: this.advancedDelegationDao.findByOrg(commonName)){
            delegation.removeOrg(commonName);
            this.delegationDao.save(delegation);
        }

        // delete entities in LDAP server
        this.orgDao.delete(this.orgDao.findExtById(commonName));
        this.orgDao.delete(this.orgDao.findByCommonName(commonName));
        this.orgDao.delete(this.orgDao.findDetailById(commonName));
        ResponseUtil.writeSuccess(response);
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
    public void getUserRequiredFields(HttpServletResponse response) throws IOException, JSONException {
            JSONArray fields = new JSONArray();
            fields.put("name");
            fields.put("shortName");
            ResponseUtil.buildResponse(response, fields.toString(4), HttpServletResponse.SC_OK);
    }

    /**
     * Return a list of possible values for organization type
     *
     * return a JSON array with possible value
     */
    @RequestMapping(value = PUBLIC_REQUEST_MAPPING +"/orgTypeValues", method = RequestMethod.GET)
    public void getOrgTypeValues(HttpServletResponse response) throws IOException, JSONException {
        JSONArray fields = new JSONArray();
        for(String field : this.orgDao.getOrgTypeValues())
            fields.put(field);
        ResponseUtil.buildResponse(response, fields.toString(4), HttpServletResponse.SC_OK);
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
        try {
            String[] rawCenter = this.georConfig.getProperty("AreaMapCenter").split("\\s*,\\s*");
            JSONArray center = new JSONArray();
            center.put(Double.parseDouble(rawCenter[0]));
            center.put(Double.parseDouble(rawCenter[1]));
            map.put("center", center);
            map.put("zoom", this.georConfig.getProperty("AreaMapZoom"));
            res.put("map", map);
        } catch (Exception e){}
        JSONObject areas = new JSONObject();
        areas.put("url", this.georConfig.getProperty("AreasUrl"));
        areas.put("key", this.georConfig.getProperty("AreasKey"));
        areas.put("value", this.georConfig.getProperty("AreasValue"));
        areas.put("group", this.georConfig.getProperty("AreasGroup"));
        res.put("areas", areas);
        ResponseUtil.buildResponse(response, res.toString(4), HttpServletResponse.SC_OK);
    }


    /**
     * Return distribution of orgs by org type as json array or CSV
     *
     * json array contains objects with following keys:
     * - type : org type
     * - count : count of org with specified type
     *
     * CSV contains two columns one for org type and second for count
     *
     */
    @RequestMapping(value = BASE_MAPPING + "/orgsTypeDistribution.{format:(?:csv|json)}", method = RequestMethod.GET)
    public void orgTypeDistribution(HttpServletResponse response,
                                    @PathVariable String format) throws IOException, JSONException {

        Map<String, Integer> distribution = new HashMap();
        List<OrgExt> orgs = this.orgDao.findAllExt();

        // Filter results if user is not SUPERUSER
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(!auth.getAuthorities().contains(ROLE_SUPERUSER))
            orgs.retainAll(Arrays.asList(this.delegationDao.findOne(auth.getName()).getOrgs()));

        for (OrgExt org : orgs) {
            try {
                distribution.put(org.getOrgType(), distribution.get(org.getOrgType()) + 1);

            } catch (NullPointerException e) {
                distribution.put(org.getOrgType(), 1);
            }
        }

        PrintWriter out = response.getWriter();

        if(format.equalsIgnoreCase("csv")){
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment;filename=orgsTypeDistribution.csv");

            out.println("organisation type, count");
            for(String type : distribution.keySet())
                out.println(type + "," + distribution.get(type));
            out.close();

        } else if(format.equalsIgnoreCase("json")){
            response.setContentType("application/json");

            JSONArray res = new JSONArray();
            for(String type : distribution.keySet())
                res.put(new JSONObject().put("type", type).put("count", distribution.get(type)));
            out.println(res.toString(4));
            out.close();
        }

    }

    /**
     * Check authorization on org. Throw an exception if current user does not have rights to edit or delete
     * specified org.
     *
     * @param org Org identifier
     * @throws AccessDeniedException if current user does not have permissions to edit this org
     */
    private void checkOrgAuthorization(String org) throws AccessDeniedException{
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Verify that org is under delegation if user is not SUPERUSER
        if(!auth.getAuthorities().contains(ROLE_SUPERUSER)){
            DelegationEntry delegation = this.delegationDao.findOne(auth.getName());
            if(!Arrays.asList(delegation.getOrgs()).contains(org))
                throw new AccessDeniedException("Org not under delegation");
        }
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
    protected void updateFromRequest(Org org, JSONObject json) throws IOException {
        org.setId(orgDao.reGenerateId(json.optString(Org.JSON_SHORT_NAME), org.getId()));
        org.setName(json.optString(Org.JSON_NAME));
        org.setShortName(json.optString(Org.JSON_SHORT_NAME));
        if (!json.isNull(Org.JSON_CITIES)) {
            org.setCities(StreamSupport
                .stream(json.optJSONArray(Org.JSON_CITIES).spliterator(), false)
                .map(Object::toString)
                .collect(Collectors.toList()));
        }
        if (!json.isNull(Org.JSON_MEMBERS)) {
            org.setMembers(StreamSupport
                .stream(json.optJSONArray(Org.JSON_MEMBERS).spliterator(), false)
                .map(Object::toString)
                .collect(Collectors.toList()));
        }
        org.setPending(json.optBoolean(Org.JSON_PENDING));
    }

    protected void updateFromRequest(OrgDetail org, JSONObject json) {
        org.setUrl(json.optString(Org.JSON_URL));
        org.setPending(json.optBoolean(Org.JSON_PENDING));
        org.setUrl(json.optString(Org.JSON_URL));
        org.setLogo(json.optString(Org.JSON_LOGO));
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
    private void updateFromRequest(OrgExt orgExt, JSONObject json) {
        orgExt.setOrgType(json.optString(OrgExt.JSON_ORG_TYPE));
        orgExt.setAddress(json.optString(OrgExt.JSON_ADDRESS));
        orgExt.setPending(json.optBoolean(Org.JSON_PENDING));
        orgExt.setDescription(json.optString(Org.JSON_DESCRIPTION));
    }

    /**
     * Parse request payload and return a JSON document
     *
     * @param request
     * @return JSON document corresponding to browser request
     * @throws IOException if error occurs during JSON parsing
     */
    private JSONObject parseRequest(HttpServletRequest request) throws IOException, JSONException {
        return new JSONObject(FileUtils.asString(request.getInputStream()));
    }

    public GeorchestraConfiguration getGeorConfig() {
        return georConfig;
    }

    public void setGeorConfig(GeorchestraConfiguration georConfig) {
        this.georConfig = georConfig;
    }
}
