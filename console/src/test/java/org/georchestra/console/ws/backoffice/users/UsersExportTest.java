package org.georchestra.console.ws.backoffice.users;

import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.OutlookCSVHeaderField.BIZ_ADDRESS;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.OutlookCSVHeaderField.BIZ_ADDR_POBOX;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.OutlookCSVHeaderField.BIZ_ADDR_STREET;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.OutlookCSVHeaderField.BIZ_COUNTRY;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.OutlookCSVHeaderField.BIZ_FAX;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.OutlookCSVHeaderField.BIZ_PC;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.OutlookCSVHeaderField.EMAIL;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.OutlookCSVHeaderField.FIRST_NAME;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.OutlookCSVHeaderField.HOME_ADDRESS;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.OutlookCSVHeaderField.JOB_TITLE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.OutlookCSVHeaderField.LAST_NAME;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.OutlookCSVHeaderField.MANAGER_NAME;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.OutlookCSVHeaderField.MOBILE_PHONE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.OutlookCSVHeaderField.OTHER_ADDR;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.OutlookCSVHeaderField.OTHER_ST;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.OutlookCSVHeaderField.PHONE;
import static org.georchestra.console.ws.backoffice.users.CSVAccountExporter.OutlookCSVHeaderField.TITLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.georchestra.console.ds.AccountDao;
import org.georchestra.console.ds.AccountDaoImpl;
import org.georchestra.console.dto.AccountImpl;
import org.georchestra.console.ws.backoffice.users.CSVAccountExporter.OutlookCSVHeaderField;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;

public class UsersExportTest {

    private UsersExport us;

    private AccountImpl account1, account2;

    private static final String headers = "First Name,Middle Name,Last Name,Title,Suffix,Initials,Web Page,Gender,Birthday,Anniversary,"
            + "Location,Language,Internet Free Busy,Notes,E-mail Address,E-mail 2 Address,E-mail 3 Address,Primary Phone,Home Phone,"
            + "Home Phone 2,Mobile Phone,Pager,Home Fax,Home Address,Home Street,Home Street 2,Home Street 3,Home Address PO Box,Home City,"
            + "Home State,Home Postal Code,Home Country,Spouse,Children,Manager's Name,Assistant's Name,Referred By,Company Main Phone,"
            + "Business Phone,Business Phone 2,Business Fax,Assistant's Phone,Company,Job Title,Department,Office Location,Organizational ID Number,"
            + "Profession,Account,Business Address,Business Street,Business Street 2,Business Street 3,Business Address PO Box,Business City,"
            + "Business State,Business Postal Code,Business Country,Other Phone,Other Fax,Other Address,Other Street,Other Street 2,Other Street 3,"
            + "Other Address PO Box,Other City,Other State,Other Postal Code,Other Country,Callback,Car Phone,ISDN,Radio Phone,TTY/TDD Phone,Telex,"
            + "User 1,User 2,User 3,User 4,Keywords,Mileage,Hobby,Billing Information,Directory Server,Sensitivity,Priority,Private,Categories";

    @Before
    public void setUp() throws Exception {
        AccountDao mockedDao = Mockito.mock(AccountDao.class);
        account1 = new AccountImpl();
        account1.setUid("pmauduit");
        account1.setCommonName("Pierre");
        account1.setSurname("Mauduit");
        account1.setEmail("abc@example.com");

        account2 = new AccountImpl();
        account2.setUid("jdoe");
        account2.setCommonName("John");
        account2.setSurname("Doe");
        account2.setEmail("jdoe@example.com");

        when(mockedDao.findByUID(eq(account1.getUid()))).thenReturn(account1);
        when(mockedDao.findByUID(eq(account2.getUid()))).thenReturn(account2);
        us = new UsersExport(mockedDao);

    }

    @Test
    public void testGetUsersAsCsv() throws Exception {
        String s = us.getUsersAsCsv("[ \"pmauduit\" ]");
        assertFalse("The CSV contains \"null\", unexpected", s.contains("null"));
        assertTrue("The CSV should contain \"abc@example.com\"", s.contains("abc@example.com"));
    }

    @Test
    public void testGetUsersAsCsvAllFields() throws Exception {
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
        assertCsvRecord(account1, record);
    }

    @Test
    public void testGetUsersAsCsvMultipleUsers() throws Exception {
        String s = us.getUsersAsCsv("[\"pmauduit\",\"jdoe\"]");

        CSVParser parser = CSVAccountExporter.FORMAT.parse(new StringReader(s));
        List<String> headerNames = parser.getHeaderNames();
        List<String> expectedHeaders = Arrays.asList(headers.split(","));
        assertEquals(expectedHeaders, headerNames);

        List<CSVRecord> records = parser.getRecords();
        assertEquals(3, records.size());
        // first record is the header
        CSVRecord header = records.get(0);
        // but just to be sure
        assertEquals(headerNames.get(0), header.get(0));

        assertCsvRecord(account1, records.get(1));
        assertCsvRecord(account2, records.get(2));
    }

    private void assertCsvRecord(AccountImpl acc, CSVRecord record) {
        assertField(acc, record, FIRST_NAME);
        assertField(acc, record, LAST_NAME);
        assertField(acc, record, TITLE);
        assertField(acc, record, EMAIL);
        assertField(acc, record, PHONE);
        assertField(acc, record, MOBILE_PHONE);
        assertField(acc, record, HOME_ADDRESS);
        assertField(acc, record, MANAGER_NAME);
        assertField(acc, record, BIZ_FAX);
        assertField(acc, record, JOB_TITLE);
        assertField(acc, record, BIZ_ADDRESS);
        assertField(acc, record, BIZ_ADDR_STREET);
        assertField(acc, record, BIZ_ADDR_POBOX);
        assertField(acc, record, BIZ_PC);
        assertField(acc, record, BIZ_COUNTRY);
        assertField(acc, record, OTHER_ADDR);
        assertField(acc, record, OTHER_ST);
    }

    private void assertField(AccountImpl acc, CSVRecord record, OutlookCSVHeaderField field) {
        String expected = field.apply(acc);
        if (null == expected) {
            expected = "";
        }
        String actual = record.get(field.getName());
        assertEquals(expected, actual);
    }

    @Test
    public void testGetUsersAsVcf() throws Exception {
        String s = us.getUsersAsVcard("[ \"pmauduit\" ]");
        assertTrue("expected ret containing BEGIN:VCARD, not found", s.startsWith("BEGIN:VCARD"));
    }

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
        adao.setUserSearchBaseDN("ou=users");

        us.setAccountDao(adao);
    }

    @Test
    public void testGetUsersAsVcfAgainstOpenLdap() throws Exception {
        setUpAgainstRealLdap();

        String vcf = us.getUsersAsVcard("[\"testadmin\", \"testuser\" ]");
        assertTrue("VCARD should contain both email address for testadmin and testuser",
                vcf.contains("psc+testuser@georchestra.org") && vcf.contains("psc+testadmin@georchestra.org"));
    }

    @Test
    public void testGetUsersAsCsvAgainstOpenLdap() throws Exception {
        setUpAgainstRealLdap();

        String csv = us.getUsersAsCsv("[\"testadmin\", \"testuser\" ]");
        assertTrue("CSV should contain both email address for testadmin and testuser",
                csv.contains("psc+testuser@georchestra.org") && csv.contains("psc+testadmin@georchestra.org"));
        assertTrue("CSV should contain 3 lines", csv.split("\r\n").length == 3);
    }

}
