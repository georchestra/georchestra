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

package org.georchestra.console.ws.backoffice.users;

import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.ACCOUNT;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.ANNIVERSARY;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.ASSISTANT_NAME;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.ASSISTANT_PHONE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.BDAY;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.BILLING_INFO;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.BIZ_ADDRESS;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.BIZ_ADDR_POBOX;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.BIZ_ADDR_STREET;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.BIZ_ADDR_STREET2;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.BIZ_ADDR_STREET3;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.BIZ_CITY;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.BIZ_COUNTRY;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.BIZ_FAX;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.BIZ_PC;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.BIZ_PHONE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.BIZ_PHONE2;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.BIZ_STATE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.CALLBACK;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.CAR_PHONE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.CATEGORIES;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.CHILDREN;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.COMPANY;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.COMPANY_PHONE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.DEPARTMENT;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.DIRECTORY_SERVER;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.EMAIL;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.EMAIL2;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.EMAIL3;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.FIRST_NAME;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.GENDER;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.HOBBY;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.HOME_ADDRESS;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.HOME_CITY;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.HOME_COUNTRY;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.HOME_FAX;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.HOME_PC;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.HOME_PHONE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.HOME_PHONE2;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.HOME_POBOX;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.HOME_STATE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.HOME_STREET;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.HOME_STREET2;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.HOME_STREET3;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.INITIALS;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.INTERNET_FREE_BUSY;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.ISDN;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.JOB_TITLE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.KEYWORDS;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.LANG;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.LAST_NAME;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.LOCATION;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.MANAGER_NAME;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.MIDDLE_NAME;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.MILEAGE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.MOBILE_PHONE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.NOTES;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.OFFICE_LOCATION;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.ORG_ID;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.OTHER_ADDR;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.OTHER_CITY;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.OTHER_COUNTRY;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.OTHER_FAX;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.OTHER_PC;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.OTHER_PHONE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.OTHER_POBOX;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.OTHER_ST;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.OTHER_ST2;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.OTHER_ST3;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.OTHER_STATE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.PAGER;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.PHONE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.PRIORITY;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.PRIVATE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.PROFESSION;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.RADIO_PHONE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.REFERRED_BY;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.SENSITIVITY;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.SPOUSE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.SUFFIX;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.TELEX;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.TITLE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.TTY_PHONE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.USER1;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.USER2;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.USER3;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.USER4;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField.WEBPAGE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.georchestra.console.dao.AdvancedDelegationDao;
import org.georchestra.console.ws.backoffice.users.CSVAccountExporter.CSVHeaderField;
import org.georchestra.ds.LdapDaoProperties;
import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.orgs.OrgsDao;
import org.georchestra.ds.users.AccountDao;
import org.georchestra.ds.users.AccountDaoImpl;
import org.georchestra.ds.users.AccountImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;

public class UsersExportTest {

    private UsersExport us;

    private AccountImpl account1, account2;
    private Org org1, org2;

    private static final String headers = Arrays.stream(CSVHeaderField.values()).map(a -> a.getName())
            .collect(Collectors.joining(","));

    @BeforeEach
    public void setUp() throws Exception {
        AccountDao accDao = mock(AccountDao.class);
        OrgsDao orgDao = mock(OrgsDao.class);

        account1 = new AccountImpl();
        account1.setUid("pmauduit");
        account1.setOrg("c2c");
        account1.setGivenName("Pierre");
        account1.setCommonName("Mauduit");
        account1.setEmail("abc@example.com");

        org1 = new Org();
        org1.setId("c2c");
        org1.setName("CampToCamp");

        account2 = new AccountImpl();
        account2.setUid("jdoe");
        account2.setOrg("org2");
        account2.setGivenName("John");
        account2.setCommonName("Doe");
        account2.setEmail("jdoe@example.com");

        org2 = new Org();
        org2.setId("org2");
        org2.setName("Org2");

        when(orgDao.findByUser(eq(account1))).thenReturn(org1);
        when(orgDao.findByUser(eq(account2))).thenReturn(org2);

        Authentication auth = mock(Authentication.class);
        Collection<GrantedAuthority> authorities = Collections.singleton(AdvancedDelegationDao.ROLE_SUPERUSER);
        doReturn(authorities).when(auth).getAuthorities();
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(accDao.findByUID(eq(account1.getUid()))).thenReturn(account1);
        when(accDao.findByUID(eq(account2.getUid()))).thenReturn(account2);

        UserInfoExporterImpl exporter = new UserInfoExporterImpl(accDao, orgDao);
        us = new UsersExport(exporter);
    }

    @Test
    public void testGetUsersAsCsv() throws Exception {
        String s = us.getUsersAsCsv("[\"pmauduit\"]");
        String expected = "Pierre,,Mauduit,,,,,,,,,,,,,,abc@example.com,,,,,,,,,,,,,,,,,,,,,,,,,,,,CampToCamp,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";

        String[] splitted = s.split("\r\n");

        assertFalse(s.contains("null"), "The CSV contains \"null\", unexpected");
        assertTrue(s.contains("abc@example.com"), "The CSV should contain \"abc@example.com\"");
        assertEquals(splitted[0], headers, "The CSV should have the headers");
        assertEquals(expected, splitted[1], "The CSV payoad should match entry data");
    }

    @Test
    public void testCsvValueExtract() throws Exception {
        setUpAllMappedFields();
        // mapped fields
        assertEquals(account1.getGivenName(), FIRST_NAME.apply(account1, org1));
        assertEquals(account1.getCommonName(), LAST_NAME.apply(account1, org1));
        assertEquals(account1.getTitle(), TITLE.apply(account1, org1));
        assertEquals(org1.getDescription(), NOTES.apply(account1, org1));
        assertEquals(account1.getEmail(), EMAIL.apply(account1, org1));
        assertEquals(account1.getPhone(), PHONE.apply(account1, org1));
        assertEquals(account1.getMobile(), MOBILE_PHONE.apply(account1, org1));
        assertEquals(account1.getHomePostalAddress(), HOME_ADDRESS.apply(account1, org1));
        assertEquals(account1.getLocality(), HOME_CITY.apply(account1, org1));
        assertEquals(account1.getManager(), MANAGER_NAME.apply(account1, org1));
        assertEquals(account1.getPostalAddress(), BIZ_ADDRESS.apply(account1, org1));
        assertEquals(account1.getStreet(), BIZ_ADDR_STREET.apply(account1, org1));
        assertEquals(account1.getPostOfficeBox(), BIZ_ADDR_POBOX.apply(account1, org1));
        String cities = org1.getCities().stream().collect(Collectors.joining(","));
        assertEquals(cities, BIZ_CITY.apply(account1, org1));
        assertEquals(account1.getFacsimile(), BIZ_FAX.apply(account1, org1));
        assertEquals(org1.getName(), COMPANY.apply(account1, org1));
        assertEquals(account1.getDescription(), JOB_TITLE.apply(account1, org1));
        assertEquals(account1.getPostalCode(), BIZ_PC.apply(account1, org1));
        assertEquals(account1.getStateOrProvince(), BIZ_COUNTRY.apply(account1, org1));
        assertEquals(account1.getRegisteredAddress(), OTHER_ADDR.apply(account1, org1));
        assertEquals(account1.getPhysicalDeliveryOfficeName(), OTHER_ST.apply(account1, org1));

        // unmapped fields
        assertNull(MIDDLE_NAME.apply(account1, org1));
        assertNull(SUFFIX.apply(account1, org1));
        assertNull(INITIALS.apply(account1, org1));
        assertNull(WEBPAGE.apply(account1, org1));
        assertNull(GENDER.apply(account1, org1));
        assertNull(BDAY.apply(account1, org1));
        assertNull(ANNIVERSARY.apply(account1, org1));
        assertNull(LOCATION.apply(account1, org1));
        assertNull(LANG.apply(account1, org1));
        assertNull(INTERNET_FREE_BUSY.apply(account1, org1));
        assertNull(EMAIL2.apply(account1, org1));
        assertNull(EMAIL3.apply(account1, org1));
        assertNull(HOME_PHONE.apply(account1, org1));
        assertNull(HOME_PHONE2.apply(account1, org1));
        assertNull(PAGER.apply(account1, org1));
        assertNull(HOME_FAX.apply(account1, org1));
        assertNull(HOME_STREET.apply(account1, org1));
        assertNull(HOME_STREET2.apply(account1, org1));
        assertNull(HOME_STREET3.apply(account1, org1));
        assertNull(HOME_POBOX.apply(account1, org1));
        assertNull(HOME_STATE.apply(account1, org1));
        assertNull(HOME_PC.apply(account1, org1));
        assertNull(HOME_COUNTRY.apply(account1, org1));
        assertNull(SPOUSE.apply(account1, org1));
        assertNull(CHILDREN.apply(account1, org1));
        assertNull(ASSISTANT_NAME.apply(account1, org1));
        assertNull(REFERRED_BY.apply(account1, org1));
        assertNull(COMPANY_PHONE.apply(account1, org1));
        assertNull(BIZ_PHONE.apply(account1, org1));
        assertNull(BIZ_PHONE2.apply(account1, org1));
        assertNull(ASSISTANT_PHONE.apply(account1, org1));
        assertNull(DEPARTMENT.apply(account1, org1));
        assertNull(OFFICE_LOCATION.apply(account1, org1));
        assertNull(ORG_ID.apply(account1, org1));
        assertNull(PROFESSION.apply(account1, org1));
        assertNull(ACCOUNT.apply(account1, org1));
        assertNull(BIZ_ADDR_STREET2.apply(account1, org1));
        assertNull(BIZ_ADDR_STREET3.apply(account1, org1));
        assertNull(BIZ_STATE.apply(account1, org1));
        assertNull(OTHER_PHONE.apply(account1, org1));
        assertNull(OTHER_FAX.apply(account1, org1));
        assertNull(OTHER_ST2.apply(account1, org1));
        assertNull(OTHER_ST3.apply(account1, org1));
        assertNull(OTHER_POBOX.apply(account1, org1));
        assertNull(OTHER_CITY.apply(account1, org1));
        assertNull(OTHER_STATE.apply(account1, org1));
        assertNull(OTHER_PC.apply(account1, org1));
        assertNull(OTHER_COUNTRY.apply(account1, org1));
        assertNull(CALLBACK.apply(account1, org1));
        assertNull(CAR_PHONE.apply(account1, org1));
        assertNull(ISDN.apply(account1, org1));
        assertNull(RADIO_PHONE.apply(account1, org1));
        assertNull(TTY_PHONE.apply(account1, org1));
        assertNull(TELEX.apply(account1, org1));
        assertNull(USER1.apply(account1, org1));
        assertNull(USER2.apply(account1, org1));
        assertNull(USER3.apply(account1, org1));
        assertNull(USER4.apply(account1, org1));
        assertNull(KEYWORDS.apply(account1, org1));
        assertNull(MILEAGE.apply(account1, org1));
        assertNull(HOBBY.apply(account1, org1));
        assertNull(BILLING_INFO.apply(account1, org1));
        assertNull(DIRECTORY_SERVER.apply(account1, org1));
        assertNull(SENSITIVITY.apply(account1, org1));
        assertNull(PRIORITY.apply(account1, org1));
        assertNull(PRIVATE.apply(account1, org1));
        assertNull(CATEGORIES.apply(account1, org1));
    }

    @Test
    public void testGetUsersAsCsvAllFields() throws Exception {
        setUpAllMappedFields();

        String s = us.getUsersAsCsv("[\"pmauduit\"]");

        CSVParser parser = CSVAccountExporter.FORMAT.parse(new StringReader(s));
        List<String> headerNames = parser.getHeaderNames();
        List<String> expectedHeaders = Arrays.asList(headers.split(","));
        assertEquals(expectedHeaders, headerNames);

        List<CSVRecord> records = parser.getRecords();
        assertEquals(2, records.size());
        // first record is the header
        CSVRecord header = records.get(0);
        // but just to be sure
        assertEquals(headerNames.get(0), header.get(0));

        CSVRecord record = records.get(1);
        assertCsvRecord(account1, org1, record);
    }

    private void setUpAllMappedFields() {
        account1.setTitle("CTO");
        account1.setPhone("+54 555-111");
        account1.setMobile("+54 341 555 222");
        account1.setHomePostalAddress("some address");
        account1.setManager("Homer");
        account1.setFacsimile("+54 555 1234");
        account1.setDescription("description \n with \r\n newlines\n and\n \"escape \"char");
        account1.setPostalAddress("some postal Address");
        account1.setStreet("the street name");
        account1.setPostOfficeBox("the post office box");
        account1.setPostalCode("2000");
        account1.setStateOrProvince("Stanta Fe");
        account1.setRegisteredAddress("the registered address");
        account1.setPhysicalDeliveryOfficeName("delivery office");
        account1.setLocality("Paris");

        org1.setCities(Arrays.asList("Paris", "Seoul"));
        org1.setAddress("c2c address");
        // https://translate.google.com/#view=home&op=translate&sl=fr&tl=ko&text=Camptocamp.org%20a%20pour%20but%20de%20faciliter%20%0Ale%20partage%20d'informations%20entre%20les%20pratiquants%20%0Ade%20sports%20de%20montagne%20et%20de%20contribuer%20%C3%A0%20la%20%0As%C3%A9curit%C3%A9%20des%20activit%C3%A9s%20montagne.
        org1.setDescription("Camptocamp.org의 목표는\n실무자 간의 정보 공유\n산악 스포츠와에 기여\n산악 활동의 안전.");
        org1.setUrl("https://www.camptocamp.org/");
    }

    @Test
    public void testGetUsersAsCsvMultipleUsers() throws Exception {
        String csv = us.getUsersAsCsv("[\"pmauduit\",\"jdoe\"]");

        CSVParser parser = CSVAccountExporter.FORMAT.parse(new StringReader(csv));
        List<String> headerNames = parser.getHeaderNames();
        List<String> expectedHeaders = Arrays.asList(headers.split(","));
        assertEquals(expectedHeaders, headerNames);

        List<CSVRecord> records = parser.getRecords();
        assertEquals(3, records.size());
        // first record is the header
        CSVRecord header = records.get(0);
        // but just to be sure
        assertEquals(headerNames.get(0), header.get(0));

        assertCsvRecord(account1, org1, records.get(1));
        assertCsvRecord(account2, org2, records.get(2));
    }

    private void assertCsvRecord(AccountImpl acc, Org org, CSVRecord record) {
        assertField(acc, org, record, FIRST_NAME);
        assertField(acc, org, record, LAST_NAME);
        assertField(acc, org, record, TITLE);
        assertField(acc, org, record, EMAIL);
        assertField(acc, org, record, PHONE);
        assertField(acc, org, record, MOBILE_PHONE);
        assertField(acc, org, record, HOME_ADDRESS);
        assertField(acc, org, record, MANAGER_NAME);
        assertField(acc, org, record, BIZ_FAX);
        assertField(acc, org, record, JOB_TITLE);
        assertField(acc, org, record, BIZ_ADDRESS);
        assertField(acc, org, record, BIZ_ADDR_STREET);
        assertField(acc, org, record, BIZ_ADDR_POBOX);
        assertField(acc, org, record, BIZ_PC);
        assertField(acc, org, record, BIZ_COUNTRY);
        assertField(acc, org, record, OTHER_ADDR);
        assertField(acc, org, record, OTHER_ST);
        assertField(acc, org, record, HOME_CITY);
    }

    private void assertField(AccountImpl acc, Org org, CSVRecord record, CSVHeaderField field) {
        String expected = field.apply(acc, org);
        if (null == expected) {
            expected = "";
        }
        String actual = record.get(field.getName());
        assertEquals(expected, actual);
    }

    @Test
    public void testGetUsersAsVcf() throws Exception {
        String s = us.getUsersAsVcard("[\"pmauduit\"]");

        assertTrue(s.startsWith("BEGIN:VCARD"), "expected ret containing BEGIN:VCARD, not found");
        assertTrue(s.contains("VERSION:3.0"), "Expect vcard version to be 3");
    }

    // TODO: these tests are never being run, refactor them as proper integration
    // tests
    private void setUpAgainstRealLdap() {
        assumeTrue(System.getProperty("console.test.openldap.ldapurl") != null
                && System.getProperty("console.test.openldap.basedn") != null);

        String ldapUrl = System.getProperty("console.test.openldap.ldapurl");
        String baseDn = System.getProperty("console.test.openldap.basedn");

        DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(ldapUrl + baseDn);
        contextSource.setBase(baseDn);
        contextSource.setUrl(ldapUrl);
        contextSource.setBaseEnvironmentProperties(new HashMap<String, Object>());
        contextSource.setAnonymousReadOnly(true);
        contextSource.setCacheEnvironmentProperties(false);

        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);

        AccountDaoImpl adao = new AccountDaoImpl(ldapTemplate);
        adao.setLdapDaoProperties(new LdapDaoProperties().setUserSearchBaseDN("ou=users"));

//        us.setAccountDao(adao);
    }

    @Test
    public void testGetUsersAsVcfAgainstOpenLdap() throws Exception {
        setUpAgainstRealLdap();
        String vcf = us.getUsersAsVcard("[\"testadmin\", \"testuser\"]");
        assertTrue(vcf.contains("psc+testuser@georchestra.org") && vcf.contains("psc+testadmin@georchestra.org"),
                "VCARD should contain both email address for testadmin and testuser");
    }

    @Test
    public void testGetUsersAsCsvAgainstOpenLdap() throws Exception {
        setUpAgainstRealLdap();
        String csv = us.getUsersAsVcard("[\"testadmin\", \"testuser\"]");

        assertTrue(csv.contains("psc+testuser@georchestra.org") && csv.contains("psc+testadmin@georchestra.org"),
                "CSV should contain both email address for testadmin and testuser");
        assertEquals(3, csv.split("\r\n").length, "CSV should contain 3 lines");
    }
}
