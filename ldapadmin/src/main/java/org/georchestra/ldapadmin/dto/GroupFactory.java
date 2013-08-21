/**
 * 
 */
package org.georchestra.ldapadmin.dto;

/**
 * This factory creates instance of {@link Group}.
 * 
 * 
 * @author Mauricio Pazos
 *
 */
public class GroupFactory {

	private GroupFactory(){}
	
	
	public static Group create() {
		
		return new GroupImpl();
	}

	public static Group create(String commonName, String description) {
		
		Group g = new GroupImpl();
		
		g.setName(commonName);
		g.setDescription(description);
		
		return g;
	}

}
