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

package org.georchestra.dlform;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;



public class DownloadQuery {

    private String firstName;
    private String secondName;
    private String company;
    private String email;
    private String tel;
    private Integer[] dataUse;
    private String comment;
    private boolean ok;
    private String userName;
    private String sessionId;

    /* extractorapp specific variables */
    private String jsonSpec;

    /* geonetwork specific variables */
    private int metadataId;
    private String fileName;


    public int getMetadataId() {
        return metadataId;
    }
    public String getFileName() {
        return fileName;
    }

    public String getJsonSpec() {
        return jsonSpec;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getSecondName() {
        return secondName;
    }
    public String getCompany() {
        return company;
    }
    public String getEmail() {
        return email;
    }
    public String getTel() {
        return tel;
    }
    public Integer[] getDataUse() {
        return dataUse;
    }
    public String getComment() {
        return comment;
    }
    public String getUserName() {
        return userName;
    }
    public String getSessionId() {
        return sessionId;
    }

    public DownloadQuery(HttpServletRequest request) {
        // Rely first on the headers given by the Security-proxy
        // fallback on the form fields (in case of unauthenticated)
        firstName = request.getHeader("sec-firstname") != null ?
                request.getHeader("sec-firstname") : request.getParameter("first_name");

        secondName = request.getHeader("sec-lastname") != null ?
                request.getHeader("sec-lastname") : request.getParameter("last_name");

        company = request.getHeader("sec-orgname") != null ?
                request.getHeader("sec-orgname") : request.getParameter("company");

        email = request.getHeader("sec-email") != null ?
                request.getHeader("sec-email") : request.getParameter("email");

        tel = request.getHeader("sec-tel") != null ?
                request.getHeader("sec-tel") : request.getParameter("tel");

        // datause is an array of int
        String[] dUse = request.getParameter("datause") != null ?
                request.getParameter("datause").split(",") : null;
        if ((dUse != null) && (dUse.length > 0)) {
            ArrayList<Integer> tmpdataUse = new ArrayList<Integer>();
            for (String t : dUse) {
                try {
                    tmpdataUse.add(new Integer(t));
                } catch (NumberFormatException e) {
                    // Discards the unparseable input
                }
            }
            if (tmpdataUse.size() > 0) {
                dataUse = tmpdataUse.toArray(new Integer[tmpdataUse.size()]);
            }
            else {
                // No valid record sent
                dataUse = null;
            }
        }
        comment = request.getParameter("comment");

        ok = request.getParameter("ok") != null ?
                request.getParameter("ok").equalsIgnoreCase("on") : false;

        userName = request.getHeader("sec-username");

        sessionId = request.getParameter("sessionid");

        jsonSpec  = request.getParameter("json_spec");

        fileName     = request.getParameter("fname");
        metadataId   = request.getParameter("id") != null ? Integer.parseInt(request.getParameter("id")) : -1;
    }
    public boolean isInvalid() {
        return ((firstName == null) || (secondName == null)
                || (company == null) || (email == null)
                || (dataUse == null) || (ok == false)
                || (sessionId == null));
    }
}
