/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
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
