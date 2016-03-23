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

package org.georchestra.analytics.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@NamedNativeQueries({
// no user / group filter
@NamedNativeQuery(name="Stats.getRequestCountBetweenStartDateAndEndDateByHour",
query = "SELECT COUNT(*) AS count,	to_char(date, 'YYYY-mm-dd hh') FROM ogcstatistics.ogc_services_log WHERE "
		+ "date >= :startDate  AND date < :endDate "
		+ "GROUP BY to_char(date, 'YYYY-mm-dd hh') ORDER BY to_char(date, 'YYYY-mm-dd hh')"),

@NamedNativeQuery(name="Stats.getRequestCountBetweenStartDateAndEndDateByDay",
query = "SELECT COUNT(*) AS count,	to_char(date, 'YYYY-mm-dd') FROM ogcstatistics.ogc_services_log WHERE "
		+ "date >= :startDate  AND date < :endDate "
		+ "GROUP BY to_char(date, 'YYYY-mm-dd') ORDER BY to_char(date, 'YYYY-mm-dd')"),

@NamedNativeQuery(name="Stats.getRequestCountBetweenStartDateAndEndDateByWeek",
query = "SELECT COUNT(*) AS count,	 to_char(date, 'YYYY-WW') FROM ogcstatistics.ogc_services_log WHERE "
		+ "date >= :startDate  AND date < :endDate "
		+ "GROUP BY to_char(date, 'YYYY-WW') ORDER BY to_char(date, 'YYYY-WW')"),

@NamedNativeQuery(name="Stats.getRequestCountBetweenStartDateAndEndDateByMonth",
query = "SELECT COUNT(*) AS count,	 to_char(date, 'YYYY-mm') FROM ogcstatistics.ogc_services_log WHERE "
		+ "date >= :startDate  AND date < :endDate "
		+ "GROUP BY to_char(date, 'YYYY-mm') ORDER BY to_char(date, 'YYYY-mm')"),
	
// users
@NamedNativeQuery(name="Stats.getRequestCountForUserBetweenStartDateAndEndDateByHour",
query = "SELECT COUNT(*) AS count,	to_char(date, 'YYYY-mm-dd hh') FROM ogcstatistics.ogc_services_log WHERE "
		+ "user_name = :user AND date >= :startDate  AND date < :endDate "
		+ "GROUP BY to_char(date, 'YYYY-mm-dd hh') ORDER BY to_char(date, 'YYYY-mm-dd hh')"),

@NamedNativeQuery(name="Stats.getRequestCountForUserBetweenStartDateAndEndDateByDay",
query = "SELECT COUNT(*) AS count,	to_char(date, 'YYYY-mm-dd') FROM ogcstatistics.ogc_services_log WHERE "
		+ "user_name = :user AND date >= :startDate  AND date < :endDate "
		+ "GROUP BY to_char(date, 'YYYY-mm-dd') ORDER BY to_char(date, 'YYYY-mm-dd')"),

@NamedNativeQuery(name="Stats.getRequestCountForUserBetweenStartDateAndEndDateByWeek",
query = "SELECT COUNT(*) AS count,	 to_char(date, 'YYYY-WW') FROM ogcstatistics.ogc_services_log WHERE "
		+ "user_name = :user AND date >= :startDate  AND date < :endDate "
		+ "GROUP BY to_char(date, 'YYYY-WW') ORDER BY to_char(date, 'YYYY-WW')"),

@NamedNativeQuery(name="Stats.getRequestCountForUserBetweenStartDateAndEndDateByMonth",
query = "SELECT COUNT(*) AS count,	 to_char(date, 'YYYY-mm') FROM ogcstatistics.ogc_services_log WHERE "
		+ "user_name = :user AND date >= :startDate  AND date < :endDate "
		+ "GROUP BY to_char(date, 'YYYY-mm') ORDER BY to_char(date, 'YYYY-mm')"),

// groups
@NamedNativeQuery(name="Stats.getRequestCountForGroupBetweenStartDateAndEndDateByHour",
query = "SELECT COUNT(*) AS count,	 to_char(date, 'YYYY-mm-dd hh')  FROM ogcstatistics.ogc_services_log WHERE "
		+ ":group = ANY (roles) AND date >= :startDate  AND date < :endDate "
		+ "GROUP BY to_char(date, 'YYYY-mm-dd hh') ORDER BY to_char(date, 'YYYY-mm-dd hh')"),

@NamedNativeQuery(name="Stats.getRequestCountForGroupBetweenStartDateAndEndDateByDay",
query = "SELECT COUNT(*) AS count,	 to_char(date, 'YYYY-mm-dd') FROM ogcstatistics.ogc_services_log WHERE "
		+ ":group = ANY (roles) AND date >= :startDate  AND date < :endDate "
		+ "GROUP BY to_char(date, 'YYYY-mm-dd') ORDER BY to_char(date, 'YYYY-mm-dd')"),

@NamedNativeQuery(name="Stats.getRequestCountForGroupBetweenStartDateAndEndDateByWeek",
query = "SELECT COUNT(*) AS count,	 to_char(date, 'YYYY-WW') FROM ogcstatistics.ogc_services_log WHERE "
		+ ":group = ANY (roles) AND date >= :startDate  AND date < :endDate "
		+ "GROUP BY to_char(date, 'YYYY-WW') ORDER BY to_char(date, 'YYYY-WW')"),

@NamedNativeQuery(name="Stats.getRequestCountForGroupBetweenStartDateAndEndDateByMonth",
query = "SELECT COUNT(*) AS count, to_char(date, 'YYYY-mm') FROM ogcstatistics.ogc_services_log WHERE "
		+ ":group = ANY (roles) AND date >= :startDate  AND date < :endDate "
		+ "GROUP BY to_char(date, 'YYYY-mm') ORDER BY to_char(date, 'YYYY-mm')"),

// distinct users
@NamedNativeQuery(name="Stats.getDistinctUsersByGroup",
query = "SELECT DISTINCT user_name, org, COUNT(*)  FROM ogcstatistics.ogc_services_log WHERE "
		+ ":group = ANY (roles) AND date >= :startDate  AND date < :endDate GROUP BY user_name, org ORDER BY COUNT(*) DESC"),
@NamedNativeQuery(name="Stats.getDistinctUsers",
query = "SELECT DISTINCT user_name, org, COUNT(*) FROM ogcstatistics.ogc_services_log WHERE "
		+ " date >= :startDate  AND date < :endDate GROUP BY user_name, org ORDER BY COUNT(*) DESC"),

// layer stats
@NamedNativeQuery(name="Stats.getLayersStatisticsForUser",
query = "SELECT DISTINCT layer, COUNT(*) FROM ogcstatistics.ogc_services_log WHERE "
		+ " date >= :startDate  AND date < :endDate AND user = :user AND layer != '' GROUP BY layer ORDER BY COUNT(*) DESC"),

@NamedNativeQuery(name="Stats.getLayersStatisticsForUserLimit",
query = "SELECT DISTINCT layer, COUNT(*) FROM ogcstatistics.ogc_services_log WHERE "
		+ " date >= :startDate  AND date < :endDate AND user = :user AND layer != '' GROUP BY layer ORDER BY COUNT(*) DESC LIMIT :limit"),

@NamedNativeQuery(name="Stats.getLayersStatisticsForGroup",
query = "SELECT DISTINCT layer, COUNT(*) FROM ogcstatistics.ogc_services_log WHERE "
		+ " date >= :startDate  AND date < :endDate AND :group = ANY(roles) AND layer != '' GROUP BY layer ORDER BY COUNT(*) DESC"),

@NamedNativeQuery(name="Stats.getLayersStatisticsForGroupLimit",
query = "SELECT DISTINCT layer, COUNT(*) FROM ogcstatistics.ogc_services_log WHERE "
		+ " date >= :startDate  AND date < :endDate AND :group = ANY(roles) AND layer != '' GROUP BY layer ORDER BY COUNT(*) DESC LIMIT :limit"),

@NamedNativeQuery(name="Stats.getLayersStatistics",
query = "SELECT DISTINCT layer, COUNT(*) FROM ogcstatistics.ogc_services_log WHERE "
		+ " date >= :startDate  AND date < :endDate AND layer != '' GROUP BY layer ORDER BY COUNT(*) DESC"),

@NamedNativeQuery(name="Stats.getLayersStatisticsLimit",
query = "SELECT DISTINCT layer, COUNT(*) FROM ogcstatistics.ogc_services_log WHERE "
		+ " date >= :startDate  AND date < :endDate AND layer != '' GROUP BY layer ORDER BY COUNT(*) DESC LIMIT :limit"),
})
@Table(schema="ogcstatistics", name="ogc_services_log")
public class Stats {
	@Id
	@SequenceGenerator(name="ogc_services_log_id_seq", sequenceName="ogc_services_log_id_seq",
		schema="ogcstatistics", initialValue=1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ogc_services_log_id_seq")
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
