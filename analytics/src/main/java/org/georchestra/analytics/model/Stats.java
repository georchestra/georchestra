package org.georchestra.analytics.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@NamedNativeQueries({
@NamedNativeQuery(name="Stats.getRequestCountForUserBetweenStartDateAndEndDate",
 query = "SELECT COUNT(*) AS count,	 date FROM ogcstatistics.ogc_services_log WHERE "
 		+ "user_name = :user AND date >= :startDate  AND date < :endDate "
		+ "GROUP BY date ORDER BY date"),
@NamedNativeQuery(name="Stats.getRequestCountForGroupBetweenStartDateAndEndDate",
query = "SELECT COUNT(*) AS count,	 date FROM ogcstatistics.ogc_services_log WHERE "
		+ ":group = ANY (roles) AND date >= :startDate  AND date < :endDate "
		+ "GROUP BY date ORDER BY date")
})
@Table(schema="ogcstatistics", name="ogc_services_log")
public class Stats {
	@Id
	@GeneratedValue
	private long id;
	@Column(name="user_name")
	private String user;
	
	@Column(columnDefinition="Date")
	private Date date;
	private String service;
	private String layer;
	private String request;
	private String org;
	@Type(type = "org.georchestra.analytics.util.PostGresArrayStringType")
	private String[] roles;

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getService() {
		return service;
	}
	
	public void setService(String service) {
		this.service = service;
	}
	
	public String getLayer() {
		return layer;
	}
	
	public void setLayer(String layer) {
		this.layer = layer;
	}
	
	public String getRequest() {
		return request;
	}
	
	public void setRequest(String request) {
		this.request = request;
	}
	
	public String getOrg() {
		return org;
	}
	
	public void setOrg(String org) {
		this.org = org;
	}
	
	public String[] getRoles() {
		return roles;
	}
	
	public void setRoles(String[] roles) {
		this.roles = roles;
	}

}
