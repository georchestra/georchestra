package org.georchestra.ldapadmin.ds;

import java.util.List;

import org.georchestra.ldapadmin.dto.Account;

/**
 * Defines the operations to maintain the set of account.
 * 
 * @author Mauricio Pazos
 * 
 */
public interface AccountDao {


	/**
	 * Checks if the uid exist.
	 * @param uid
	 * @return true if the uid exist, false in other case.
	 * @throws DataServiceException
	 */
	boolean exist(final String uid) throws DataServiceException;
	
	/**
	 * Returns all accounts
	 * 
	 * @return List of {@link Account}
	 * @throws DataServiceException
	 */
	List<Account> findAll() throws DataServiceException;
	
	/**
	 * Returns all accounts that accomplish the provided filter.
	 * 
	 * @param uidFilter
	 * @return List of {@link Account}
	 * @throws DataServiceException
	 */
	List<Account> findFilterBy(final ProtectedUserFilter uidFilter) throws DataServiceException;

	/**
	 * Creates a new account
	 * 
	 * @param account
	 * @param groupID
	 * @throws DataServiceException
	 * @throws DuplicatedEmailException
	 * @throws RequiredFiedException
	 */
	void insert(final Account account, final String groupID) throws DataServiceException, DuplicatedUidException, DuplicatedEmailException;

	/**
	 * Updates the user account
	 * @param account
	 * @throws DataServiceException
	 * @throws DuplicatedEmailException
	 * @throws RequiredFiedException
	 */
	void update(final Account account) throws DataServiceException, DuplicatedEmailException;

	/**
	 * Updates the user account, given the old and the new state of the account
	 * Needed if a DN update is required (modifying the uid).
	 *
	 * @param account
	 * @param modified
	 *
	 * @throws DuplicatedEmailException
	 * @throws DataServiceException
	 * @throws NotFoundException
	 */
	void update(Account account, Account modified) throws DataServiceException, DuplicatedEmailException, NotFoundException;

	/**
	 * Changes the user password
	 * 
	 * @param uid
	 * @param password
	 * @throws DataServiceException
	 */
	void changePassword(final String uid, final String password)throws DataServiceException;


	/**
	 * Deletes the account
	 * 
	 * @param account
	 * @throws DataServiceException
	 * @throws NotFoundException
	 */
	void delete(final String uid) throws DataServiceException, NotFoundException;

	/**
	 * Returns the account that contains the uid provided as parameter.
	 * 
	 * @param uid
	 * 
	 * @return {@link Account}
	 * 
	 * @throws DataServiceException
	 * @throws NotFoundException
	 */
	Account findByUID(final String uid)throws DataServiceException, NotFoundException;

	/**
	 * Returns the account that contains the email provided as parameter.
	 * 
	 * @param email
	 * @return {@link Account}
	 * 
	 * @throws DataServiceException
	 * @throws NotFoundException
	 */
	Account findByEmail(final String email) throws DataServiceException, NotFoundException;
	

	
	/**
	 * Add the new password. This method is part of the "lost password" workflow to maintan the old password and the new password until the
	 * user can confirm that he had asked for a new password.   
	 * 
	 * @param udi
	 * @param newPassword
	 */
	void addNewPassword(String uid, String newPassword);

	
	/**
	 * Generates a new Id based on the uid provided as parameter.
	 * 
	 * @param uid
	 *  
	 * @return a new uid
	 * 
	 * @throws DataServiceException
	 */
	String generateUid(String uid) throws DataServiceException;

}
