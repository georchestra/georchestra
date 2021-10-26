package org.georchestra.console.ws.backoffice.users;

import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.users.Account;
import org.springframework.ldap.NameNotFoundException;

public interface UserInfoExporter {

    String exportAsLdif(String user) throws NameNotFoundException, DataServiceException;

    String exportAsLdif(Account account);

    String exportUsersAsCsv(String... userNames) throws DataServiceException;

    String exportUsersAsVcard(String... users) throws Exception;

}