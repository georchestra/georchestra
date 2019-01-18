/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

package org.georchestra.ogcservstatistics.dataservices;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Retrieve all records present in the ogc service table
 * 
 * @author Mauricio Pazos
 *
 */
final public class RetrieveAllCommand extends AbstractQueryCommand{

	final static String DATE_COLUMN = "date";
	final static String USER__COLUMN = "user_name";
	final static String SERVICE_COLUMN = "service";
	final static String LAYER_COLUMN = "layer";
	final static String SECROLE_COLUMN = "roles";

	
	final static String SQL = " SELECT "+ DATE_COLUMN+ "," + USER__COLUMN +","+ SERVICE_COLUMN+","+LAYER_COLUMN+","+SECROLE_COLUMN  
							+ " FROM ogcstatistics.OGC_SERVICES_LOG"
							+ " ORDER BY "+ DATE_COLUMN+ "," + USER__COLUMN +","+ SERVICE_COLUMN+","+LAYER_COLUMN+","+SECROLE_COLUMN;
 	
	protected PreparedStatement prepareStatement() throws SQLException{

		PreparedStatement pStmt = this.connection.prepareStatement(SQL);
		
		return pStmt;
	}

	protected Map<String, Object> getRow(ResultSet rs) throws SQLException{
		
		Map<String,Object> row = new HashMap<String, Object>(5);
		row.put(DATE_COLUMN, rs.getDate(DATE_COLUMN));
		row.put(USER__COLUMN, rs.getString(USER__COLUMN));
		row.put(SERVICE_COLUMN, rs.getString(SERVICE_COLUMN));
		row.put(LAYER_COLUMN, rs.getString(LAYER_COLUMN));
		row.put(SECROLE_COLUMN, rs.getString(SECROLE_COLUMN));
		
		return row;
	}

}
