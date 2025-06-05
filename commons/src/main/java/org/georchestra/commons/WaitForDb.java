/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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

package org.georchestra.commons;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class WaitForDb {

    private String url;
    private String username;
    private String password;
    private String driverClassName;

    @PostConstruct
    public void test() {
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            System.err.printf("CONFIGURATION ERROR: Unable to load JDBC driver '%s'", driverClassName);
            throw new RuntimeException(e);
        }

        while (true) {
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                System.out.println("--------------------------------> DB OK <------------------------------------");
                break;
            } catch (SQLException e) {
                System.out.println("------------------------> DB not ready - waiting <---------------------------");
                System.out.print(" url:             ");
                System.out.println(this.getUrl());
                System.out.print(" username:        ");
                System.out.println(this.getUsername());
                System.out.print(" driverClassName: ");
                System.out.println(this.getDriverClassName());
                try {
                    Thread.sleep(1000l);
                } catch (InterruptedException e1) {
                }
            }
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getDriverClassName() {
        return driverClassName;
    }
}
