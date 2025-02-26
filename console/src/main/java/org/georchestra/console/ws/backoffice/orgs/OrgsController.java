/*
 * Copyright (C) 2009 by the geOrchestra PSC
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

import static org.georchestra.commons.security.SecurityHeaders.SEC_USERNAME;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.commons.security.SecurityHeaders;
import org.georchestra.console.dao.AdvancedDelegationDao;
import org.georchestra.console.dao.DelegationDao;
import org.georchestra.console.model.AdminLogType;
import org.georchestra.console.model.DelegationEntry;
import org.georchestra.console.ws.backoffice.utils.ResponseUtil;
import org.georchestra.console.ws.utils.LogUtils;
import org.georchestra.console.ws.utils.Validation;
import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.orgs.OrgsDao;
import org.georchestra.lib.file.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Sets;

@Controller
public class OrgsController {

    private static final Log LOG = LogFactory.getLog(OrgsController.class.getName());

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
    protected DelegationDao delegationDao;

    @Autowired
    protected AdvancedDelegationDao advancedDelegationDao;

    @Autowired
    protected LogUtils logUtils;

    /**
     * Areas map configuration
     *
     * This map appears on the /console/account/new page, when the user checks the
     * "my org does not exist" checkbox. Currently the map is configured with the
     * EPSG:4326 SRS.
     */

    /* Center of map */
    @Value("${AreaMapCenter:1.77, 47.3}")
    private String areaMapCenter;

    /* Zoom of map */
    @Value("${AreaMapZoom:6}")
    private String areaMapZoom;

    /* The following properties are used to configure the map widget behavior */

    /* Key stored in the org LDAP record to uniquely identify a feature. */
    @Value("${AreasKey:INSEE_COM}")
    private String areasKey;

    /* Feature "nice name" which appears in the widget list once selected. */
    @Value("${AreasValue:NOM_COM}")
    private String areasValue;

    /*
     * Feature property which is used to group together areas.
     *
     * eg: if the GeoJSON file represents regions, then AreasGroup might be the
     * property with the "state name".
     */
    @Value("${AreasGroup:NOM_DEP}")
    private String areasGroup;

    public OrgsController(OrgsDao dao) {
        this.orgDao = dao;
    }

    /**
     * Return a list of available organization as json array
     */
    @GetMapping(value = REQUEST_MAPPING, produces = "application/json; charset=utf-8")
    @PostFilter("hasPermission(filterObject, 'read')")
    @ResponseBody
    public List<Org> findAll(@RequestParam(defaultValue = "true") boolean logos) {
        List<Org> orgs = this.orgDao.findAll();
        if (!logos) {
            orgs.forEach(o -> o.setLogo(null));
        }
        Collections.sort(orgs);
        return orgs;
    }

    /**
     * Retreive full information about one org as JSON document. Following keys will
     * be available :
     *
     * * 'id' (not used) * 'name' * 'shortName' * 'cities' as json array ex:
     * [654,865498,98364,9834534,984984,6978984,98498] * 'status' * 'orgType' *
     * 'address' * 'members' as json array ex: ["testadmin", "testuser"]
     *
     */
    @GetMapping(value = REQUEST_MAPPING + "/{cn:.+}", produces = "application/json; charset=utf-8")
    @ResponseBody
    public Org getOrgInfos(@PathVariable String cn) {
        this.checkOrgAuthorization(cn);

        return this.orgDao.findByCommonName(cn);
    }

    /**
     * Retreive full information about one org as JSON document from unique
     * organization id number (e.g french SIRET number). Following keys will be
     * available :
     *
     * * 'id' (not used) * 'name' * 'shortName' * 'cities' as json array ex:
     * [654,865498,98364,9834534,984984,6978984,98498] * 'status' * 'orgType' *
     * 'address' * 'members' as json array ex: ["testadmin", "testuser"]
     *
     */
    @GetMapping(value = REQUEST_MAPPING + "/uoi/{orgUniqueId:.+}", produces = "application/json; charset=utf-8")
    @ResponseBody
    public Org getOrgInfosFromUniqueOrgId(@PathVariable String orgUniqueId) {
        Org orgInfos = this.orgDao.findByOrgUniqueId(orgUniqueId);
        this.checkOrgAuthorization(orgInfos.getId());
        return orgInfos;
    }

    /**
     * Update information of one org
     *
     * Request should contain Json formated document containing following keys :
     *
     * * 'name' * 'shortName' * 'cities' as json array ex:
     * [654,865498,98364,9834534,984984,6978984,98498] * 'status' * 'orgType' *
     * 'address' * 'members' as json array ex: ["testadmin", "testuser"]
     *
     * All fields are optional.
     *
     * Full json example : { "name" : "Office national des forêts", "shortName" :
     * "ONF", "cities" : [ 654, 865498, 98364, 9834534, 984984, 6978984, 98498 ],
     * "status" : "inscrit", "type" : "association", "address" : "128 rue de la
     * plante, 73059 Chambrille", "members": [ "testadmin", "testuser" ] }
     *
     */
    @PutMapping(value = REQUEST_MAPPING + "/{commonName:.+}", produces = "application/json; charset=utf-8")
    @ResponseBody
    public Org updateOrgInfos(@PathVariable String commonName, HttpServletRequest request)
            throws IOException, JSONException, SQLException {

        this.checkOrgAuthorization(commonName);

        // Parse Json
        JSONObject json = this.parseRequest(request);

        if (!this.validation.validateOrgUnicity(orgDao, json)) {
            throw new IOException("Organization : already exists");
        }

        // Validate request against required fields for admin part
        if (!this.validation.validateOrgField("name", json)) {
            throw new IOException("required field : name");
        }

        if (!this.validation.validateUrl(json.optString(Org.JSON_URL))) {
            throw new IOException(String.format("bad org url format: %s", json.optString(Org.JSON_URL)));
        }

        // Retrieve current orgs state from ldap
        Org org = this.orgDao.findByCommonName(commonName);
        final Org initialOrg = org.clone();

        // get default pending status
        Boolean defaultPending = org.isPending();

        // Update org and orgExt fields
        this.updateFromRequest(org, json);

        // Persist changes to LDAP server
        this.orgDao.update(org);

        if (!commonName.equals(org.getId())) {
            for (DelegationEntry delegation : this.advancedDelegationDao.findByOrg(commonName)) {
                delegation.removeOrg(commonName);
                delegation.setOrgs(ArrayUtils.add(delegation.getOrgs(), org.getId()));
                this.delegationDao.save(delegation);
            }
        }

        // log org and orgExt changes
        logUtils.logOrgChanged(initialOrg, json);

        // log if pending status change
        String username = SecurityHeaders.decode(request.getHeader(SEC_USERNAME));
        if (username != null && defaultPending != org.isPending()) {
            logUtils.createLog(org.getId(), AdminLogType.PENDING_ORG_ACCEPTED, null);
        }
        return org;
    }

    /**
     * Create a new org based on JSON document sent by browser. JSON document may
     * contain following keys :
     *
     * * 'name' * 'shortName' (mandatory) * 'cities' as json array ex:
     * [654,865498,98364,9834534,984984,6978984,98498] * 'status' * 'type' *
     * 'address' * 'members' as json array ex: ["testadmin", "testuser"]
     *
     * All fields are optional except 'shortName' which is used to generate
     * organization identifier.
     *
     * A new JSON document will be return to browser with a complete description of
     * created org. @see updateOrgInfos() for JSON format.
     */
    @PostMapping(value = REQUEST_MAPPING, produces = "application/json; charset=utf-8")
    @ResponseBody
    @PreAuthorize("hasRole('SUPERUSER')")
    public Org createOrg(HttpServletRequest request) throws IOException, JSONException {
        // Parse Json
        JSONObject json = this.parseRequest(request);

        if (!this.validation.validateOrgUnicity(orgDao, json)) {
            throw new IOException("An organization with this identification number already exists.");
        }

        // Validate request against required fields for admin part
        if (!this.validation.validateOrgField("shortName", json)) {
            throw new IOException("required field : shortName");
        }

        if (!this.validation.validateUrl(json.optString(Org.JSON_URL))) {
            throw new IOException(String.format("bad org url format: %s", json.optString(Org.JSON_URL)));
        }

        Org org = new Org();
        org.setId("");
        this.updateFromRequest(org, json);

        // Persist changes to LDAP server
        this.orgDao.insert(org);

        logUtils.createLog(org.getId(), AdminLogType.ORG_CREATED, null);

        return org;
    }

    /**
     * Delete one org
     */
    @DeleteMapping(REQUEST_MAPPING + "/{commonName:.+}")
    @PreAuthorize("hasRole('SUPERUSER')")
    public void deleteOrg(@PathVariable String commonName, HttpServletResponse response)
            throws IOException, SQLException {

        // Check if this role is part of a delegation
        for (DelegationEntry delegation : this.advancedDelegationDao.findByOrg(commonName)) {
            delegation.removeOrg(commonName);
            this.delegationDao.save(delegation);
        }

        // delete entities in LDAP server
        Org org = this.orgDao.findByCommonName(commonName);
        Boolean isPending = org.isPending();

        this.orgDao.delete(org);

        // get authent info without request
        if (isPending != null && isPending) {
            logUtils.createLog(commonName, AdminLogType.PENDING_ORG_REFUSED, null);
        } else {
            logUtils.createLog(commonName, AdminLogType.ORG_DELETED, null);
        }

        ResponseUtil.writeSuccess(response);
    }

    @GetMapping(PUBLIC_REQUEST_MAPPING + "/requiredFields")
    public void getRequiredFieldsForOrgCreation(HttpServletResponse response) throws IOException, JSONException {
        JSONArray fields = new JSONArray();
        validation.getRequiredOrgFields().forEach(fields::put);
        ResponseUtil.buildResponse(response, fields.toString(4), HttpServletResponse.SC_OK);
    }

    @GetMapping(PUBLIC_REQUEST_MAPPING + "/orgTypeValues")
    public void getOrganisationTypePossibleValues(HttpServletResponse response) throws IOException, JSONException {
        JSONArray fields = new JSONArray();
        for (String field : this.orgDao.getOrgTypeValues()) {
            fields.put(field);
        }
        ResponseUtil.buildResponse(response, fields.toString(4), HttpServletResponse.SC_OK);
    }

    /**
     * Return configuration areas UI as json object. Configuration includes
     * following values :
     *
     * - inital map center - initial map zoom - ows service to retrieve area
     * geometry 'url' - attribute to use as label 'value' - attribute to use to
     * group area 'group' - attribute to use as identifier 'key'
     *
     * Ex : { "map" : { "center": [49.5468, 5.123486], "zoom": 8}, "areas" : {
     * "url": "http://sdi.georchestra.org/geoserver/....;", "key": "insee_code",
     * "value": "commune_name", "group": "department_name"} }
     */

    @GetMapping(PUBLIC_REQUEST_MAPPING + "/areaConfig.json")
    public void getAreaConfig(HttpServletResponse response) throws IOException, JSONException {
        JSONObject res = new JSONObject();
        JSONObject map = new JSONObject();
        // Parse center
        try {
            String[] rawCenter = areaMapCenter.split("\\s*,\\s*");
            JSONArray center = new JSONArray();
            center.put(Double.parseDouble(rawCenter[0]));
            center.put(Double.parseDouble(rawCenter[1]));
            map.put("center", center);
            map.put("zoom", areaMapZoom);
            res.put("map", map);
        } catch (Exception e) {
            LOG.info("Could not parse value", e);
        }
        JSONObject areas = new JSONObject();
        areas.put("key", areasKey);
        areas.put("value", areasValue);
        areas.put("group", areasGroup);
        res.put("areas", areas);
        ResponseUtil.buildResponse(response, res.toString(4), HttpServletResponse.SC_OK);
    }

    /**
     * Return distribution of orgs by org type as json array or CSV
     *
     * json array contains objects with following keys: - type : org type - count :
     * count of org with specified type
     *
     * CSV contains two columns one for org type and second for count
     *
     */
    @GetMapping(BASE_MAPPING + "/orgsTypeDistribution.{format:(?:csv|json)}")
    public void orgTypeDistribution(HttpServletResponse response, @PathVariable String format)
            throws IOException, JSONException {

        Map<String, Integer> distribution = new HashMap<>();
        List<Org> orgs = this.orgDao.findAll();

        // Filter results if user is not SUPERUSER
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(ROLE_SUPERUSER)) {
            String[] filteredIds = this.delegationDao.findFirstByUid(auth.getName()).getOrgs();
            if (filteredIds != null && filteredIds.length > 0) {
                final Set<String> retain = Sets.newHashSet(filteredIds);
                orgs = orgs.stream().filter(o -> retain.contains(o.getName())).collect(Collectors.toList());
            }
        }

        for (Org org : orgs) {
            try {
                distribution.put(org.getOrgType(), distribution.get(org.getOrgType()) + 1);
            } catch (NullPointerException e) {
                distribution.put(org.getOrgType(), 1);
            }
        }

        PrintWriter out = response.getWriter();

        if (format.equalsIgnoreCase("csv")) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment;filename=orgsTypeDistribution.csv");

            out.println("organisation type, count");
            for (String type : distribution.keySet()) {
                out.println(type + "," + distribution.get(type));
            }
            out.close();

        } else if (format.equalsIgnoreCase("json")) {
            response.setContentType("application/json");

            JSONArray res = new JSONArray();
            for (String type : distribution.keySet()) {
                res.put(new JSONObject().put("type", type).put("count", distribution.get(type)));
            }
            out.println(res.toString(4));
            out.close();
        }

    }

    /**
     * Check authorization on org. Throw an exception if current user does not have
     * rights to edit or delete specified org.
     *
     * @param org Org identifier
     * @throws AccessDeniedException if current user does not have permissions to
     *                               edit this org
     */
    private void checkOrgAuthorization(String org) throws AccessDeniedException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Verify authent context and that org is under delegation if user is not
        // SUPERUSER
        if (auth != null && auth.getName() != null && !auth.getAuthorities().contains(ROLE_SUPERUSER)) {
            DelegationEntry delegation = this.delegationDao.findFirstByUid(auth.getName());
            if (delegation != null) {
                if (!Arrays.asList(delegation.getOrgs()).contains(org)) {
                    throw new AccessDeniedException("Org not under delegation");
                }
            } else {
                throw new AccessDeniedException("Org not under delegation");
            }
        }
    }

    /**
     * Update org instance based on field found in json object.
     *
     * All field of Org instance will be updated if corresponding key exists in json
     * document except 'members'.
     *
     * @param org  Org instance to update
     * @param json Json document to take information from
     * @throws JSONException If something went wrong during information extraction
     *                       from json document
     */
    protected void updateFromRequest(Org org, JSONObject json) throws IOException {
        org.setId(orgDao.reGenerateId(json.optString(Org.JSON_SHORT_NAME), org.getId()));
        org.setName(json.optString(Org.JSON_NAME));
        org.setShortName(json.optString(Org.JSON_SHORT_NAME));
        if (!json.isNull(Org.JSON_CITIES)) {
            List<String> cities = new ArrayList<>();
            if (!json.getJSONArray(Org.JSON_CITIES).isEmpty()) {
                cities = StreamSupport.stream(json.getJSONArray(Org.JSON_CITIES).spliterator(), false)
                        .map(Object::toString).collect(Collectors.toList());
            }
            org.setCities(cities);
        }
        if (!json.isNull(Org.JSON_MEMBERS)) {
            org.setMembers(StreamSupport.stream(json.getJSONArray(Org.JSON_MEMBERS).spliterator(), false)
                    .map(Object::toString).collect(Collectors.toList()));
        }
        org.setPending(json.optBoolean(Org.JSON_PENDING));

        org.setOrgType(json.optString(Org.JSON_ORG_TYPE));
        org.setAddress(json.optString(Org.JSON_ADDRESS));
        org.setDescription(json.optString(Org.JSON_DESCRIPTION));
        org.setNote(json.optString(Org.JSON_NOTE));
        org.setUrl(json.optString(Org.JSON_URL));
        org.setLogo(json.optString(Org.JSON_LOGO));
        org.setMail(json.optString(Org.JSON_MAIL));
        org.setOrgUniqueId(json.optString(Org.JSON_ORG_UNIQ_ID));
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
}
