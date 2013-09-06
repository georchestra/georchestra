/**
 * 
 */
package org.georchestra.ldapadmin.ws.newaccount;


/**
 * A number is added or increment as postfix.  
 * 
 * @author Mauricio Pazos
 */
public final class UidGenerator {
	
	private UidGenerator(){}
	
	public static String next(String uid){

		
		String str = uid.replaceAll("[^0-9]", "");
		try{
			int i = Integer.parseInt(str);
			
			i++;
			
			String replacement = String.valueOf( i );
			return uid.replace( str, replacement );
			
			
		} catch(Exception e){
			return uid + 1;
		}
	}
	

}
