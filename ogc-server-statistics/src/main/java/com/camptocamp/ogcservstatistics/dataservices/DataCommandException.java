package com.camptocamp.ogcservstatistics.dataservices;

import java.sql.SQLException;

public class DataCommandException extends Exception {

	/**
	 * for serialization 
	 */
	private static final long serialVersionUID = -5196425322579527757L;
	

	public DataCommandException(String message) {
		super(message);
	}


	public DataCommandException(SQLException e) {
		super(e);
	}

	
	
}
