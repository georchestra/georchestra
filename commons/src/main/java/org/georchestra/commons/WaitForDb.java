package org.georchestra.commons;

import org.apache.commons.dbcp.BasicDataSource;
import java.sql.SQLException;


public class WaitForDb {

    private String url;
    private String username;
    private String password;
    private String driverClassName;

    public void test(){
        BasicDataSource db =  new BasicDataSource();
        db.setUrl(this.getUrl());
        db.setUsername(this.getUsername());
        db.setPassword(this.getPassword());
        db.setDriverClassName(this.getDriverClassName());

        while(true) {
            try {
                db.getConnection();
                System.out.println("--------------------------------> DB OK <------------------------------------");
                break;
            } catch (SQLException e) {
                System.out.println("---------------------------> DB Not Ready waiting <--------------------------");
                try { Thread.sleep(1000l); } catch (InterruptedException e1) {}
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
