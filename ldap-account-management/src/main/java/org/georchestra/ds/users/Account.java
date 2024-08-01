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

package org.georchestra.ds.users;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

/**
 * Account data transfer object.
 *
 * @author Mauricio Pazos
 *
 */
public interface Account extends Comparable<Account> {

    void setUniqueIdentifier(UUID uniqueId);

    UUID getUniqueIdentifier();

    void setUid(String uid);

    String getUid();

    /**
     * Person’s full name.
     */
    String getCommonName();

    /**
     * Person’s full name.
     */
    void setCommonName(String name);

    String getEmail();

    void setEmail(String email);

    String getPhone();

    void setPhone(String phone);

    String getDescription();

    void setDescription(String description);

    void setPassword(String password);

    String getPassword();

    String getSurname();

    void setSurname(String surname);

    /**
     * The givenName attribute is used to hold the part of a person’s name which is
     * not their surname nor middle name.
     */
    String getGivenName();

    /**
     * The givenName attribute is used to hold the part of a person’s name which is
     * not their surname nor middle name.
     */
    void setGivenName(String givenName);

    String getTitle();

    void setTitle(String title);

    String getPostalAddress();

    void setPostalAddress(String postalAddress);

    String getPostalCode();

    void setPostalCode(String postalCode);

    String getRegisteredAddress();

    void setRegisteredAddress(String registeredAddress);

    String getPostOfficeBox();

    void setPostOfficeBox(String postOfficeBox);

    String getPhysicalDeliveryOfficeName();

    void setPhysicalDeliveryOfficeName(String physicalDeliveryOfficeName);

    void setStreet(String street);

    String getStreet();

    void setLocality(String locality);

    String getLocality();

    void setFacsimile(String facsimile);

    String getFacsimile();

    void setMobile(String mobile);

    String getMobile();

    void setRoomNumber(String roomNumber);

    String getRoomNumber();

    void setStateOrProvince(String stateOrProvince);

    String getStateOrProvince();

    void setHomePostalAddress(String homePostalAddress);

    String getHomePostalAddress();

    void setOrg(String org);

    String getOrg();

    void setShadowExpire(Date expireDate);

    Date getShadowExpire();

    void setLastLogin(LocalDate lastLogin);

    LocalDate getLastLogin();

    void setCreationDate(LocalDate creationDate);

    LocalDate getCreationDate();

    void setPrivacyPolicyAgreementDate(LocalDate privacyPolicyAgreementDate);

    LocalDate getPrivacyPolicyAgreementDate();

    String getManager();

    void setManager(String manager);

    String getNote();

    void setNote(String note);

    String getContext();

    void setContext(String context);

    boolean isPending();

    void setPending(boolean pending);

    String[] getSshKeys();

    void setSshKeys(String[] sshKeys);

    String getSASLUser();

    void setSASLUser(String user);

    PasswordType getPasswordType();

    void setPasswordType(PasswordType passwordType);

    String getOAuth2Provider();

    void setOAuth2Provider(String oAuth2Provider);

    String getOAuth2Uid();

    void setOAuth2Uid(String oAuth2Uid);

    boolean getIsExternalAuth();

    void setIsExternalAuth(boolean isExternalAuth);
}
