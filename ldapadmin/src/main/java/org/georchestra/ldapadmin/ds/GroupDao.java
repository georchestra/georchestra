/**
 * 
 */
package org.georchestra.ldapadmin.ds;

import java.util.List;

import org.georchestra.ldapadmin.dto.Group;

/**
 * @author Mauricio Pazos
 *
 */
public interface GroupDao {

	/**
	 * adds the user to the group
	 * @param uid
	 * @throws NotFoundException 
	 * @throws DataServiceException 
	 */
	void addUser(String  groupID, String userId) throws DataServiceException, NotFoundException;


	void addUsers(String cn, List<String> addList) throws DataServiceException, NotFoundException; 

	/**
	 * Returns all groups. Each groups will contains its list of users.
	 * 
	 * @return list of {@link Group}
	 */
	List<Group> findAll() throws DataServiceException;

	/**
	 * Returns all groups for a given uid.
	 *
	 * @return list of {@link Group}
	 */
	List<Group> findAllForUser(String userId) throws DataServiceException;

	/**
	 * Returns the group's users
	 * 
	 * @return list of user uid
	 */
	List<String> findUsers(final String groupName) throws DataServiceException;

	/**
	 * Deletes the user from all groups 
	 *
	 * @param uid
	 * @throws DataServiceException
	 */
	void deleteUser(String uid) throws DataServiceException;

	void deleteUsers(String cn, List<String> deleteList) throws DataServiceException, NotFoundException;

	/**
	 * Deletes the user from the group
	 * 
	 * @param groupName
	 * @param uid
	 * @throws DataServiceException
	 */
	void deleteUser(String groupName, String uid) throws DataServiceException;

	/**
	 * Modifies the user (e.g. rename) from the group
	 *
	 * @param groupName
	 * @param oldUid
	 * @param newUid
	 * @throws DataServiceException
	 */
	void modifyUser(String groupName, String oldUid, String newUid) throws DataServiceException;

	/**
	 * Adds the group
	 * 
	 * @param group
	 * 
	 * @throws DataServiceException 
	 * @throws DuplicatedCommonNameException if the group es present in the LDAP store
	 */
	void insert(Group group) throws DataServiceException, DuplicatedCommonNameException;

	/**
	 * Removes the group
	 * 
	 * @param commonName
	 * @throws DataServiceException
	 * @throws NotFoundException
	 */
	void delete(String commonName) throws DataServiceException,	NotFoundException;

	/**
	 * Search the group based on the common name (cn)
	 * @param commonName
	 * @return {@link Group}
	 * 
	 * @throws NotFoundException
	 */
	Group findByCommonName(String commonName) throws DataServiceException, NotFoundException;

	
	/**
	 * Modifies the groups fields in the store
	 * 
	 * @param groupName
	 * @param modified
	 * 
	 */
	void update(String groupName, Group modified) throws DataServiceException, NotFoundException, DuplicatedCommonNameException;

	void addUsersInGroups(List<String> putGroup, List<String> users)  throws DataServiceException, NotFoundException;

	void deleteUsersInGroups(List<String> deleteGroup, List<String> users) throws DataServiceException, NotFoundException;


	
}
