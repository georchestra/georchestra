package org.georchestra.console.ws.backoffice.users;

import java.io.IOException;
import java.io.StringWriter;

import org.georchestra.console.ds.AccountDao;
import org.georchestra.console.ds.DataServiceException;
import org.georchestra.console.dto.Account;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.SearchExecutor;
import org.ldaptive.SearchResult;
import org.ldaptive.io.LdifWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.NameNotFoundException;

import ezvcard.VCard;
import ezvcard.parameter.EmailType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.FormattedName;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to export user account information in different formats (e.g. CSV,
 * V-CARD, LDIF).
 */
@Slf4j
public class UserInfoExporterImpl implements UserInfoExporter {

	private static final String CSV_DELIMITER = ",";

	// Outlook csv header
	private final String OUTLOOK_CSV_HEADER = "First Name,Middle Name,Last Name,Title,Suffix,Initials,Web Page,Gender,Birthday,Anniversary,"
			+ "Location,Language,Internet Free Busy,Notes,E-mail Address,E-mail 2 Address,E-mail 3 Address,Primary Phone,Home Phone,"
			+ "Home Phone 2,Mobile Phone,Pager,Home Fax,Home Address,Home Street,Home Street 2,Home Street 3,Home Address PO Box,Home City,"
			+ "Home State,Home Postal Code,Home Country,Spouse,Children,Manager's Name,Assistant's Name,Referred By,Company Main Phone,"
			+ "Business Phone,Business Phone 2,Business Fax,Assistant's Phone,Company,Job Title,Department,Office Location,Organizational ID Number,"
			+ "Profession,Account,Business Address,Business Street,Business Street 2,Business Street 3,Business Address PO Box,Business City,"
			+ "Business State,Business Postal Code,Business Country,Other Phone,Other Fax,Other Address,Other Street,Other Street 2,Other Street 3,"
			+ "Other Address PO Box,Other City,Other State,Other Postal Code,Other Country,Callback,Car Phone,ISDN,Radio Phone,TTY/TDD Phone,Telex,"
			+ "User 1,User 2,User 3,User 4,Keywords,Mileage,Hobby,Billing Information,Directory Server,Sensitivity,Priority,Private,Categories\r\n";

	private @Value("${ldapScheme}://${ldapHost}:${ldapPort}") String ldapUrl;
	private @Value("${ldapAdminDn:cn=admin,dc=georchestra,dc=org}") String ldapUserName;
	private @Value("${ldapAdminPassword:secret}") String ldapPassword;
	private @Value("${ldapUsersRdn:ou=users},${ldapBaseDn:dc=georchestra,dc=org}") String userSearchBaseDn;

	private AccountDao accountDao;

	public @Autowired UserInfoExporterImpl(AccountDao accountDao) {
		this.accountDao = accountDao;
	}

	@Override
	public String exportAsLdif(@NonNull String user) throws NameNotFoundException, DataServiceException {
		Account a = accountDao.findByUID(user);
		return exportAsLdif(a);
	}

	@Override
	public String exportAsLdif(@NonNull Account account) {
		log.info("Exporting personal user data for account {}", account.getUid());
		ConnectionConfig connConfig = new ConnectionConfig(this.ldapUrl);
		// connConfig.setUseStartTLS("true".equals(this.getUseStartTLS()));
		connConfig.setConnectionInitializer(
				new BindConnectionInitializer(this.ldapUserName, new Credential(this.ldapPassword)));
		ConnectionFactory cf = new DefaultConnectionFactory(connConfig);
		SearchResult result;
		final String filter = String.format("(uid=%s)", account.getUid());
		try {
			SearchExecutor executor = new SearchExecutor();
			executor.setBaseDn(this.userSearchBaseDn);
			log.info("Running search '{}'", filter);
			result = executor.search(cf, filter).getResult();
			log.info("User query successful: {}", result);
		} catch (LdapException e) {
			log.error("Error running {}", filter, e);
			throw new IllegalStateException(e);
		}

		StringWriter writer = new StringWriter();
		try {
			new LdifWriter(writer).write(result);
		} catch (IOException e) {
			log.error("Error generating LDIF file", e);
			throw new IllegalStateException(e);
		}
		String ldifContents = writer.toString();
		log.info("Returning LDIF: {}", ldifContents);
		return ldifContents;
	}

	@Override
	public @NonNull String toVcf(@NonNull Account account) {
		VCard v = new VCard();
		FormattedName f = new FormattedName(account.getGivenName() + " " + account.getSurname());
		v.addFormattedName(f);
		v.addEmail(account.getEmail(), EmailType.WORK);
		v.addTelephoneNumber(account.getPhone(), TelephoneType.WORK);
		v.addTitle(account.getTitle());
		Address a = new Address();
		a.setPostalCode(account.getPostalCode());
		a.setStreetAddress(account.getPostalAddress());
		a.setPoBox(account.getPostOfficeBox());
		a.setLocality(account.getLocality());
		v.addAddress(a);
		v.addTelephoneNumber(account.getMobile(), TelephoneType.CELL);

		return v.write();
	}

	@Override
	public @NonNull String exportUsersAsCsv(@NonNull String... userNames) throws DataServiceException {
		StringBuilder res = new StringBuilder();
		res.append(OUTLOOK_CSV_HEADER); // add csv outlook header
		for (String user : userNames) {
			try {
				Account a = accountDao.findByUID(user);
				res.append(toCsv(a));
			} catch (NameNotFoundException e) {
				log.error("User [{}] not found, skipping", user, e);
			}
		}

		return res.toString();
	}

	@Override
	public @NonNull String exportUsersAsVcard(@NonNull String... users) throws Exception {
		StringBuilder ret = new StringBuilder();
		for (String user : users) {
			try {
				Account a = accountDao.findByUID(user);
				ret.append(toVcf(a));
			} catch (NameNotFoundException e) {
				log.error("User [{}] not found, skipping", user, e);
			}
		}

		return ret.toString();
	}

	// PMD complains this is a too long method, I agree, but it's legacy code
	// unrelated to the purpose of this patch
	@SuppressWarnings("PMD")
	@Override
	public @NonNull String toCsv(@NonNull Account account) {

		StringBuilder csv = new StringBuilder();

		csv.append(toFormatedString(account.getCommonName()));
		csv.append(CSV_DELIMITER);
		csv.append(CSV_DELIMITER);// Middle Name
		csv.append(toFormatedString(account.getSurname()));
		csv.append(CSV_DELIMITER);
		csv.append(toFormatedString(account.getTitle()));
		csv.append(CSV_DELIMITER);
		csv.append(CSV_DELIMITER);// Suffix
		csv.append(CSV_DELIMITER); // Initials
		csv.append(CSV_DELIMITER);// Web Page
		csv.append(CSV_DELIMITER); // Gender
		csv.append(CSV_DELIMITER);// Birthday
		csv.append(CSV_DELIMITER); // Anniversary
		csv.append(CSV_DELIMITER);// Location
		csv.append(CSV_DELIMITER); // Language
		csv.append(CSV_DELIMITER);// Internet Free Busy
		csv.append(CSV_DELIMITER); // Notes
		csv.append(toFormatedString(account.getEmail()));
		csv.append(CSV_DELIMITER);
		csv.append(CSV_DELIMITER);// E-mail 2 Address
		csv.append(CSV_DELIMITER); // E-mail 3 Address
		csv.append(toFormatedString(account.getPhone()));// primary phone
		csv.append(CSV_DELIMITER);
		csv.append(CSV_DELIMITER);// Home Phone
		csv.append(CSV_DELIMITER); // Home Phone 2
		csv.append(toFormatedString(account.getMobile()));
		csv.append(CSV_DELIMITER); // Mobile Phone
		csv.append(CSV_DELIMITER);// Pager
		csv.append(CSV_DELIMITER);// Home Fax
		csv.append(toFormatedString(account.getHomePostalAddress()));
		csv.append(CSV_DELIMITER);// Home Address
		csv.append(CSV_DELIMITER);// Home Street
		csv.append(CSV_DELIMITER);// Home Street 2
		csv.append(CSV_DELIMITER);// Home Street 3
		csv.append(CSV_DELIMITER);// Home Address PO Box
		csv.append(CSV_DELIMITER); // locality
		csv.append(CSV_DELIMITER); // Home City
		csv.append(CSV_DELIMITER);// Home State
		csv.append(CSV_DELIMITER); // Home Postal Code
		csv.append(CSV_DELIMITER);// Home Country
		csv.append(CSV_DELIMITER);// Spouse
		csv.append(CSV_DELIMITER);// Children
		csv.append(toFormatedString(account.getManager())); // Manager's Name
		csv.append(CSV_DELIMITER);// Assistant's Name
		csv.append(CSV_DELIMITER); // Referred By
		csv.append(CSV_DELIMITER);// Company Main Phone
		csv.append(CSV_DELIMITER);// Business Phone
		csv.append(CSV_DELIMITER);// Business Phone 2
		csv.append(toFormatedString(account.getFacsimile()));
		csv.append(CSV_DELIMITER); // Business Fax
		csv.append(CSV_DELIMITER);// Assistant's Phone
		csv.append(CSV_DELIMITER); // Organization
		csv.append(CSV_DELIMITER); // Company
		csv.append(toFormatedString(account.getDescription()));
		csv.append(CSV_DELIMITER);// Job Title
		csv.append(CSV_DELIMITER);// Department
		csv.append(CSV_DELIMITER);// Office Location
		csv.append(CSV_DELIMITER);// Organizational ID Number
		csv.append(CSV_DELIMITER);// Profession
		csv.append(CSV_DELIMITER); // Account
		csv.append(toFormatedString(account.getPostalAddress()));
		csv.append(CSV_DELIMITER);// Business Address
		csv.append(toFormatedString(account.getStreet()));
		csv.append(CSV_DELIMITER);// Business Street
		csv.append(CSV_DELIMITER);// Business Street 2
		csv.append(CSV_DELIMITER); // Business Street 3
		csv.append(toFormatedString(account.getPostOfficeBox()));
		csv.append(CSV_DELIMITER);// Business Address PO Box
		csv.append(CSV_DELIMITER);// Business City
		csv.append(CSV_DELIMITER);// Business State
		csv.append(toFormatedString(account.getPostalCode()));
		csv.append(CSV_DELIMITER); // Business Postal Code
		csv.append(toFormatedString(account.getStateOrProvince()));
		csv.append(CSV_DELIMITER);// Business Country
		csv.append(CSV_DELIMITER);// Other Phone
		csv.append(CSV_DELIMITER);// Other Fax
		csv.append(toFormatedString(account.getRegisteredAddress()));
		csv.append(CSV_DELIMITER); // Other Address
		csv.append(toFormatedString(account.getPhysicalDeliveryOfficeName()));
		csv.append(CSV_DELIMITER);// Other Street
		csv.append(CSV_DELIMITER);// Other Street 2
		csv.append(CSV_DELIMITER);// Other Street 3
		csv.append(CSV_DELIMITER);// Other Address PO Box
		csv.append(CSV_DELIMITER); // Other City
		csv.append(CSV_DELIMITER);// Other State
		csv.append(CSV_DELIMITER);// Other Postal Code
		csv.append(CSV_DELIMITER);// Other Country
		csv.append(CSV_DELIMITER); // Callback
		csv.append(CSV_DELIMITER);// Car Phone
		csv.append(CSV_DELIMITER);// ISDN
		csv.append(CSV_DELIMITER);// Radio Phone
		csv.append(CSV_DELIMITER);// TTY/TDD Phone
		csv.append(CSV_DELIMITER); // Telex
		csv.append(CSV_DELIMITER);// User 1
		csv.append(CSV_DELIMITER);// User 2
		csv.append(CSV_DELIMITER);// User 3
		csv.append(CSV_DELIMITER); // User 4
		csv.append(CSV_DELIMITER);// Keywords
		csv.append(CSV_DELIMITER);// Mileage
		csv.append(CSV_DELIMITER);// Hobby
		csv.append(CSV_DELIMITER);// Billing Information
		csv.append(CSV_DELIMITER); // Directory Server
		csv.append(CSV_DELIMITER);// Sensitivity
		csv.append(CSV_DELIMITER);// Priority
		csv.append(CSV_DELIMITER);// Private
		csv.append(CSV_DELIMITER); // Categories
		csv.append("\r\n"); // CRLF
		return csv.toString();
	}

	private String toFormatedString(String data) {
		String ret = "";
		if (data != null) {
			ret = data.replace(",", ".");
		}
		return ret;
	}

}