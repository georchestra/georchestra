/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.ds.users;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Rules to valid whether the user is protected.
 * <p>
 * A protected user is only known by the system. Thus this class maintains the
 * list of the protected users, configured by the system administrator.
 * </p>
 *
 *
 * @author Mauricio Pazos
 *
 */
public class UserRule {

    private static final Log LOG = LogFactory.getLog(UserRule.class.getName());

    private Set<String> listOfprotectedUsers = new HashSet<String>();

    public Set<String> getListOfprotectedUsers() {
        return listOfprotectedUsers;
    }

    public void setListOfprotectedUsers(String[] listOfprotectedUsers) {

        HashSet<String> res = new HashSet<String>();
        res.addAll(Arrays.asList(listOfprotectedUsers));
        this.listOfprotectedUsers = res;

    }

    public UserRule() {
    }

    /**
     * True if the uid is a protected user
     *
     * @param uid
     * @return
     */
    public boolean isProtected(final String uid) {

        assert uid != null;

        if (this.listOfprotectedUsers.isEmpty()) {
            UserRule.LOG.debug("There isn't any protected user configured");
        }

        return this.listOfprotectedUsers.contains(uid);
    }

    public Predicate<Account> isProtected() {
        return a -> this.isProtected(a.getUid());
    }

    public List<String> getListUidProtected() {

        if (this.listOfprotectedUsers.isEmpty()) {
            UserRule.LOG.debug("There isn't any protected user configured");
        }
        LinkedList<String> res = new LinkedList<String>();
        res.addAll(this.listOfprotectedUsers);

        return res;
    }

}
