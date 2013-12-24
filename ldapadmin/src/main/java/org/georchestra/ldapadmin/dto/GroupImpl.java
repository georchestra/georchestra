/**
 * 
 */
package org.georchestra.ldapadmin.dto;

import java.util.LinkedList;
import java.util.List;

/**
 * A group and its users.
 * 
 * @author Mauricio Pazos
 *
 */
class GroupImpl implements Group, Comparable<Group> {

	private String name;
	private List<String> userList = new LinkedList<String>();
	private String description;
	private String gidNumber;

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

	@Override
	public void setDescription(String description) {
		this.description = description;
		
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public void setGidNumber(String gidNumber) {
		
		this.gidNumber = gidNumber;
		
	}

	@Override
	public String getGidNumber() {

		return this.gidNumber;
	}

	@Override
	public String toString() {
		return "GroupImpl [name=" + name + ", userList=" + userList
				+ ", description=" + description + ", gidNumber=" + gidNumber
				+ "]";
	}
	
	

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
    @Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((name == null) ? 0 : name.hashCode());
	    return result;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    @Override
    public boolean equals(Object obj) {
	    if (this == obj) {
		    return true;
	    }
	    if (obj == null) {
		    return false;
	    }
	    if (!(obj instanceof GroupImpl)) {
		    return false;
	    }
	    GroupImpl other = (GroupImpl) obj;
	    if (name == null) {
		    if (other.name != null) {
			    return false;
		    }
	    } else if (!name.equals(other.name)) {
		    return false;
	    }
	    return true;
    }

	@Override
    public int compareTo(Group o) {
	    
	    return this.name.compareTo(o.getName());
    }

	
}
