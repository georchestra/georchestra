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

package org.georchestra.console.ws.newaccount;

import java.io.Serializable;


/**
 * This model maintains the account form data.
 *
 * @author Mauricio Pazos
 *
 */
public class AccountFormBean implements Serializable{

	private static final long serialVersionUID = 6955470190631684934L;

	private String uid;
	private String firstName;
	private String surname;

	private String org;
	private String title;
	private String email;
	private String phone;
	private String description;
	private String password;
	private String confirmPassword;

	private String recaptcha_response_field;

	// Org creation fields
	private boolean createOrg;
	private String orgName;
	private String orgShortName;
	private String orgAddress;
	private String orgType;

	public String getRecaptcha_response_field() {
		return recaptcha_response_field;
	}
	public void setRecaptcha_response_field(String recaptcha_response_field) {
		this.recaptcha_response_field = recaptcha_response_field;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String name) {
		this.firstName = name;
	}
	public String getOrg() {
		return org;
	}
	public void setOrg(String org) {
		this.org = org;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getConfirmPassword() {
		return confirmPassword;
	}
	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String sn){

		this.surname = sn;
	}
	public boolean getCreateOrg() {
		return createOrg;
	}
	public void setCreateOrg(boolean createOrg) {
		this.createOrg = createOrg;
	}
	public String getOrgName() {
		return orgName;
	}
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	public String getOrgShortName() {
		return orgShortName;
	}
	public void setOrgShortName(String orgShortName) {
		this.orgShortName = orgShortName;
	}
	public String getOrgAddress() {
		return orgAddress;
	}
	public void setOrgAddress(String orgAddress) {
		this.orgAddress = orgAddress;
	}
	public String getOrgType() {
		return orgType;
	}
	public void setOrgType(String orgType) {
		this.orgType = orgType;
	}

	@Override
	public String toString() {
		return "AccountFormBean [uid=" + uid + ", firstName=" + firstName
				+ ", surname=" + surname + ", org=" + org + ", title=" + title
				+ ", email=" + email
				+ ", phone=" + phone + ", description=" + description + ", password="
				+ password + ", confirmPassword=" + confirmPassword
				+ ", recaptcha_response_field=" + recaptcha_response_field
				+ "]";
	}



}
