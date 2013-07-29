/**
 * 
 */
package org.georchestra.ldapadmin.ds;

import java.util.List;

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

	List<String> findAllGroups() throws DataServiceException;

}
