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
	 * add the user to the group
	 * @param uid
	 * @throws NotFoundException 
	 * @throws DataServiceException 
	 */
	void addUser(String  groupID, String userId) throws DataServiceException, NotFoundException;

	/**
	 * Returns all groups. Each groups will contains its list of users.
	 * 
	 * @return list of {@link Group}
	 */
	List<Group> findAll() throws DataServiceException;

	/**
	 * Deletes the user from all groups 
	 * 
	 * @param uid
	 * @throws DataServiceException
	 */
	void deleteUser(String uid) throws DataServiceException;
	
	/**
	 * Deletes the user from the user
	 * 
	 * @param groupName
	 * @param uid
	 * @throws DataServiceException
	 */
	void deleteUser(String groupName, String uid) throws DataServiceException;

	/**
	 * Adds the group
	 * 
	 * @param group
	 * 
	 * @throws DataServiceException 
	 * @throws DuplicatedCommonNameException if the group es present in the LDAP store
	 */
	void insert(Group group) throws DataServiceException, DuplicatedCommonNameException; 

}
