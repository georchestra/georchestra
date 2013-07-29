/**
 * 
 */
package org.georchestra.ldapadmin.dto;

import java.util.LinkedList;
import java.util.List;

/**
 * A group and its users
 * 
 * @author Mauricio Pazos
 *
 */
class GroupImpl implements Group {

	private String name;
	private List<String> userList = new LinkedList<String>();

	/* (non-Javadoc)
	 * @see org.georchestra.ldapadmin.dto.Group#getCommonName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see org.georchestra.ldapadmin.dto.Group#setCommonName(java.lang.String)
	 */
	@Override
	public void setName(String cn) {
		this.name = cn;
	}

	/* (non-Javadoc)
	 * @see org.georchestra.ldapadmin.dto.Group#getMemberUid()
	 */
	@Override
	public List<String> getUserList() {
		return this.userList;
	}

	/* (non-Javadoc)
	 * @see org.georchestra.ldapadmin.dto.Group#setMemberUid(java.util.List)
	 */
	@Override
	public void setUserList(List<String> userUidList) {
		this.userList = userUidList;
	}

	/* (non-Javadoc)
	 * @see org.georchestra.ldapadmin.dto.Group#addMemberUid(java.lang.String)
	 */
	@Override
	public void addUser(String userUid) {
		this.userList.add(userUid);

	}

}
