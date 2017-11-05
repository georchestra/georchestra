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

package org.georchestra.lib.sqlcommand;

import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * Executes Insert, Update and Delete SQL command.
 * 
 * <p>
 * The subclass must provide the sql command to execute. To do that the {@link AbstractUpdateCommand#prepareStatement()} method 
 * </p>
 * 
 * 
 * @author Mauricio Pazos
 *
 */
public abstract class AbstractUpdateCommand extends AbstractDataCommand{
	
	

	/**
	 * Execute the sql insert to add the new row (uid, token, timestamp)
	 *  
	 * @see org.georchestra.ogcservstatistics.dataservices.DataCommand#execute()
	 */
	@Override
	public void execute() throws DataCommandException {
        assert this.connection != null: "database connection is null, use setConnection";

        // executes the sql statement and checks that the update operation will be inserted one row in the table
        PreparedStatement pStmt=null;
        try {
        	this.connection.setAutoCommit(false);
            pStmt = prepareStatement();
            int updatedRows = pStmt.executeUpdate();
            this.connection.commit();
            
            if(updatedRows < 1){
                throw new DataCommandException("Fail executing. " + pStmt.toString());
            }

        } catch (SQLException e) {
        	if(this.connection != null){
        		try {
					this.connection.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
		            throw new DataCommandException(e.getMessage());
				}
	            throw new DataCommandException(e.getMessage());
        	}
        } finally{
            try {
                if(pStmt != null) pStmt.close();
            	this.connection.setAutoCommit(true);
                
            } catch (SQLException e1) {
                throw new DataCommandException(e1.getMessage());
            } 
        }
	}

	
	/**
	 * The subclass should provide a method to prepare Insert, Update or Delete 
	 * @return {@link PreparedStatement}
	 * @throws SQLException
	 */
	protected abstract PreparedStatement prepareStatement() throws SQLException;
	

}
