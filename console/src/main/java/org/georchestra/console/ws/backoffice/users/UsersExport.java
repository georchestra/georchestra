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

package org.georchestra.console.ws.backoffice.users;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.georchestra.console.dao.AdvancedDelegationDao;
import org.georchestra.console.ds.DataServiceException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import lombok.NonNull;

@Controller
public class UsersExport {

    private @Autowired
    GDPRAccountWorker gdprInfoExporter;

    private UserInfoExporter accountInfoExporter;

    @Autowired
    private AdvancedDelegationDao advancedDelegationDao;

    public @Autowired
    UsersExport(UserInfoExporter accountInfoExporter) {
        this.accountInfoExporter = accountInfoExporter;
    }

    /**
     * Generates a ZIP file bundle with all the calling user's EU
     * <a href="https://eugdpr.org/">General Data Protection Regulation</a>)
     * relevant information available on the system.
     */
    @RequestMapping(method = RequestMethod.GET, value = "/private/users/gdpr/download", produces = "application/zip")
    public void downloadUserData(HttpServletResponse response)
            throws NameNotFoundException, DataServiceException, IOException {

        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        final String userId = auth.getName();

        Resource data = gdprInfoExporter.generateUserData(userId);

        int contentLength;
        try {
            File file = data.getFile();
            contentLength = (int) file.length();
        } catch (IOException notAFileResource) {
            contentLength = 0;
        }
        try {
            String fileName = userId + "_account_data.zip";
            response.setContentType("application/zip");
            response.setContentLength(contentLength);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            IOUtils.copy(data.getInputStream(), response.getOutputStream());
            response.flushBuffer();
        } finally {
            gdprInfoExporter.dispose(data);
        }
    }

    @PostMapping(value = "/private/users.csv", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "text/csv; charset=utf-8")
    @ResponseBody
    public String getUsersAsCsv(@RequestBody String users) throws Exception {
        String[] parsedUsers = parseUserNamesFromJSONArray(users);
        checkAccessPermissionToUsersData(parsedUsers);
        @NonNull
        String csvUsers = accountInfoExporter.exportUsersAsCsv(parsedUsers);
        return csvUsers;
    }

    @PostMapping(value = "/private/users.vcf", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "text/x-vcard; charset=utf-8")
    @ResponseBody
    public String getUsersAsVcard(@RequestBody String users) throws Exception {
        String[] parsedUsers = parseUserNamesFromJSONArray(users);
        checkAccessPermissionToUsersData(parsedUsers);
        @NonNull
        String vcards = accountInfoExporter.exportUsersAsVcard(parsedUsers);
        return vcards;
    }

    /**
     * Parses and returns the user names given as a JSON array (e.g.
     * {@code ["admin", "testuser"]})
     */
    private String[] parseUserNamesFromJSONArray(String rawUsers) {
        JSONArray jsonUsers = new JSONArray(rawUsers);
        String[] users = StreamSupport.stream(jsonUsers.spliterator(), false).toArray(String[]::new);
        return users;
    }

    /**
     * Checks that the calling user has permissions to view data regarding the requested users
     *
     * @throws AccessDeniedException if current user does not have permissions to
     *                               view data of all requested users
     */
    private void checkAccessPermissionToUsersData(String[] users) throws JSONException {
        // check if user is under delegation for delegated admins
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().contains(AdvancedDelegationDao.ROLE_SUPERUSER)) {
            Set<String> usersUnderDelegation = this.advancedDelegationDao.findUsersUnderDelegation(auth.getName());
            List<String> invalid = Arrays.stream(users).filter(u -> !usersUnderDelegation.contains(u))
                    .collect(Collectors.toList());
            if (!invalid.isEmpty()) {
                throw new AccessDeniedException("Some users are not under delegation: " + invalid);
            }
        }
    }

}
