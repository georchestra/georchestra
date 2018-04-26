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

package org.georchestra.ldapadmin.dto;

/**
 * Account factory. 
 * 
 * <p>
 * This factory provide the convenient account object (data transfer object) used by this application.
 * </p>
 * 
 * @author Mauricio Pazos
 *
 */
public class AccountFactory {

	private AccountFactory(){}


	public static String formatCommonName(String givenName, String surname) {
		return givenName + " " + surname;
	}

	/**
	 * Brief data
	 * 
	 * @param uid
	 * @param password
	 * @param firstName
	 * @param surname
	 * @param email
	 * @param phone
	 * @param description
	 * @return
	 */
	public static Account createBrief(
			String uid,
			String password,
			String firstName, 
			String surname, 
			String email, 
			String phone,
			String title,
			String description) {
		
		Account account = new AccountImpl();
		account.setUid(uid);
		account.setPassword(password);
		account.setGivenName(firstName);
		account.setSurname(surname);
		account.setCommonName(formatCommonName(firstName ,surname));
		account.setEmail(email);
		account.setPhone(phone);
		account.setTitle(title);
		account.setDescription(description);
		
		return account;
	}

	/**
	 * Creates an account object with detailed data.
	 */
	public static Account createDetails(
			String uid, 
			String givenName,
			String surname,
			String physicalDeliveryOfficeName,
			String postalAddress, 
			String postalCode, 
			String postOfficeBox,
			String registeredAddress, 
			String title) {
		
		Account a = new AccountImpl();
		
		a.setUid(uid);
		a.setGivenName(givenName);
		a.setSurname(surname);
		a.setCommonName(formatCommonName(givenName, surname) );
		a.setPhysicalDeliveryOfficeName(physicalDeliveryOfficeName);
		a.setPostalAddress(postalAddress);
		a.setPostalCode(postalCode);
		a.setPostOfficeBox(postOfficeBox);
		a.setRegisteredAddress(registeredAddress);
		a.setTitle(title);

		return a;
	}

	/**
	 * Creates an account object with all data.
	 * 
	 * @param uid
	 * @param cn full name
	 * @param surname surname 
	 * @param givenName first name
	 * @param email
	 * @param title
	 * @param phone
	 * @param description
	 * @param postalAddress
	 * @param postalCode
	 * @param registeredAddress
	 * @param postOfficeBox
	 * @param physicalDeliveryOfficeName
	 * @param locality 
	 * @param street 
	 * @param facsimile
	 * @param homePostalAddress
	 * @param mobile
	 * @param roomNumber
	 * @param stateOrProvince
	 * @param manager
	 * @param context
	 * @param org
	 *
	 * @return {@link Account}
	 */
	public static Account createFull(
			String uid,
			String cn, 
			String surname,
			String givenName, 
			String email,
			String title,
			String phone, 
			String description,
			String postalAddress,
			String postalCode, 
			String registeredAddress ,
			String postOfficeBox, 
			String physicalDeliveryOfficeName, 
			String street, 
			String locality, 
			String facsimile,
			String homePostalAddress,
			String mobile, 
			String roomNumber,
			String stateOrProvince,
			String manager,
			String context,
			String org) {
		
		
		Account a = new AccountImpl();
		
		a.setUid(uid);
		a.setCommonName(cn);
		a.setGivenName(givenName);
		a.setSurname(surname);
		a.setEmail(email);
		a.setTitle(title);
		a.setPhone(phone);
		a.setDescription(description);
		a.setStreet(street);
		a.setLocality(locality);
		a.setPostalAddress(postalAddress);
		a.setPostalCode(postalCode);
		a.setRegisteredAddress(registeredAddress);
		a.setPostOfficeBox(postOfficeBox);
		a.setPhysicalDeliveryOfficeName(physicalDeliveryOfficeName);
		a.setFacsimile(facsimile);
		a.setHomePostalAddress(homePostalAddress);
		a.setMobile(mobile);
		a.setRoomNumber(roomNumber);
		a.setStateOrProvince(stateOrProvince);
		a.setManager(manager);
		a.setContext(context);
		a.setOrg(org);
		
		return a;
	}

	/**
	 * Creates an account object from another one, given as argument.
	 *
	 * @param o other account to copy
	 */
	public static Account create(Account o) {
		Account a = new AccountImpl();
		a.setUid(o.getUid());
		a.setCommonName(o.getCommonName());
		a.setSurname(o.getSurname());
		a.setEmail(o.getEmail());
		a.setPhone(o.getPhone());
		a.setDescription(o.getDescription());
		// passwords / new passwords fields voluntarily omitted:
		// the password update process should not go through this.
		a.setGivenName(o.getGivenName());
		a.setTitle(o.getTitle());
		a.setPostalAddress(o.getPostalAddress());
		a.setPostalCode(o.getPostalCode());
		a.setRegisteredAddress(o.getRegisteredAddress());
		a.setPostOfficeBox(o.getPostOfficeBox());
		a.setPhysicalDeliveryOfficeName(o.getPhysicalDeliveryOfficeName());
		a.setStreet(o.getStreet());
		a.setLocality(o.getLocality());
		a.setFacsimile(o.getFacsimile());
		a.setMobile(o.getMobile());
		a.setRoomNumber(o.getRoomNumber());
		a.setStateOrProvince(o.getStateOrProvince());
		a.setHomePostalAddress(o.getHomePostalAddress());
		a.setManager(o.getManager());
		a.setShadowExpire(o.getShadowExpire());
		a.setContext(o.getContext());
		a.setOrg(o.getOrg());

		return a;
	}

}
