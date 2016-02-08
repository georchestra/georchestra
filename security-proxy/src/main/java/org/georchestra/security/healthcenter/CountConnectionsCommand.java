/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
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

package org.georchestra.security.healthcenter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.georchestra.ogcservstatistics.dataservices.AbstractQueryCommand;

/**
 * This command counts the open connections in Postres data base
 * 
 * @author Mauricio Pazos
 *
 */
final class CountConnectionsCommand extends AbstractQueryCommand{

	final static String CONNECTION_COUNT = "count_connections";
	
	final static String SQL = " SELECT count(*) as "+CONNECTION_COUNT+" FROM pg_catalog.pg_stat_activity "; 
			
	protected PreparedStatement prepareStatement() throws SQLException{

		PreparedStatement pStmt = this.connection.prepareStatement(SQL);
		
		return pStmt;
	}

	protected Map<String, Object> getRow(ResultSet rs) throws SQLException{
		
		Map<String,Object> row = new HashMap<String, Object>(4);
		row.put(CONNECTION_COUNT, rs.getInt(CONNECTION_COUNT));
		
		return row;
	}

	public Integer getCountConnections() {

		List<Map<String, Object>> result = getResult();
		
		if(result.size() > 0){
			Map<String,Object> row= result.get(0);
			Integer count = (Integer)row.get(CONNECTION_COUNT);
			
			return count;
		}
		return 0;
	}

}
