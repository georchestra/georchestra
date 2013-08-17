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

}
