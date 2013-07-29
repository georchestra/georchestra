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


	private static String formatCommonName(String givenName, String surname) {
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
	 * @param org
	 * @param details
	 * @return
	 */
	public static Account createBrief(
			String uid,
			String password,
			String firstName, 
			String surname, 
			String email, 
			String phone,
			String org, 
			String details) {
		
		Account account = new AccountImpl();
		
		account.setUid(uid);
		account.setPassword(password);

		account.setGivenName(firstName);
		account.setSurname(surname);

		account.setCommonName(formatCommonName(firstName ,surname));

		account.setEmail(email);
		account.setPhone(phone);
		account.setOrg(org);
		account.setDetails(details);
		
		return account;
	}

	/**
	 * Creates an account object with detailed data.
	 */
	public static Account createDetails(
			String uid, 
			String givenName,
			String surname, 
			String org, 
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

		a.setOrg(org);

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
	 * @param cn
	 * @param surname
	 * @param givenName
	 * @param email
	 * @param org
	 * @param title
	 * @param phone
	 * @param description
	 * @param postalAddress
	 * @param postalCode
	 * @param registeredAddress
	 * @param postOfficeBox
	 * @param physicalDeliveryOfficeName
	 * 
	 * @return {@link Account}
	 */
	public static Account createFull(
			String uid,
			String cn, 
			String surname,
			String givenName, 
			String email,
			String org, 
			String title,
			String phone, 
			String description,
			String postalAddress,
			String postalCode, 
			String registeredAddress ,
			String postOfficeBox, 
			String physicalDeliveryOfficeName) {
		
		Account a = new AccountImpl();
		
		a.setUid(uid);
		a.setCommonName(cn);
		a.setGivenName(givenName);
		a.setSurname(surname);
		a.setEmail(email);

		a.setOrg(org);
		a.setTitle(title);

		a.setPhone(phone);
		a.setDetails(description);
		
		a.setPostalAddress(postalAddress);
		a.setPostalCode(postalCode);
		a.setRegisteredAddress(registeredAddress);
		a.setPostOfficeBox(postOfficeBox);
		a.setPhysicalDeliveryOfficeName(physicalDeliveryOfficeName);
		
		return a;
	}

}
