package org.georchestra.console.ws.backoffice.users;

import org.georchestra.console.ds.DataServiceException;
import org.georchestra.console.dto.Account;
import org.springframework.ldap.NameNotFoundException;

public interface UserInfoExporter {

	String exportAsLdif(String user) throws NameNotFoundException, DataServiceException;

	String exportAsLdif(Account account);

	String toVcf(Account account);

	String exportUsersAsCsv(String... userNames) throws DataServiceException;

	String exportUsersAsVcard(String... users) throws Exception;

	String toCsv(Account account);

}