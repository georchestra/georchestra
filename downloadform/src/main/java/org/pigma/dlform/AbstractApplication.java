package org.pigma.dlform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

public abstract class AbstractApplication {
	protected Log logger;
	protected String firstName;
	protected String secondName;
	protected String company;
	protected String email;
	protected String tel;
	protected String[] dataUseStr;
	protected String comment;
	protected boolean ok;
	protected String userName;
	protected String sessionId;
	
	protected PostGresqlConnection postgresqlConnection;
	
	protected AbstractApplication(PostGresqlConnection pgpool) {
		postgresqlConnection = pgpool;
	}
	
	protected boolean isInvalid() {
		return ((firstName == null) || (secondName == null)	|| (company == null) || (email == null)
				|| (tel == null) || (dataUseStr == null) || (comment == null) || (ok == false)
				|| (userName == null) || (sessionId == null));
	}



	public void initializeVariables(HttpServletRequest request) {
		// Rely first on the headers given by the Security-proxy
		// fallback on the form fields (in case of unauthenticated)
		firstName    = request.getHeader("sec-firstname") != null ? request.getHeader("sec-firstname") : request.getParameter("first_name");
		secondName   = request.getHeader("sec-lastname")  != null ? request.getHeader("sec-lastname")  : request.getParameter("second_name");
		company      = request.getHeader("sec-org")       != null ? request.getHeader("sec-org")       : request.getParameter("company");
		email        = request.getHeader("sec-email")     != null ? request.getHeader("sec-email")     : request.getParameter("email");
		tel          = request.getHeader("sec-tel")       != null ? request.getHeader("sec-tel")       : request.getParameter("tel");
		dataUseStr = request.getParameter("datause")    != null ? request.getParameter("datause").split(",") : null;
		comment      = request.getParameter("comment");
		ok          = request.getParameter("ok")         != null ? request.getParameter("ok").equalsIgnoreCase("on") : false;
		userName     = request.getHeader("sec-username")  != null ? request.getHeader("sec-username") : request.getParameter("username");
		sessionId    = request.getSession().getId();
	}
	
	protected void insertDataUse(Connection connection, String insertDataUseQuery, int idInserted) throws Exception {
		for (String dataUse : dataUseStr) {
			int dataUseI = Integer.parseInt(dataUse);
			PreparedStatement dataUseSt = null;
			try {
				dataUseSt = connection.prepareStatement(insertDataUseQuery);
				dataUseSt.setInt(1, idInserted);
				dataUseSt.setInt(2, dataUseI);
				dataUseSt.execute();
			} finally {
				if (dataUseSt != null) dataUseSt.close();
			}
		}
	}
	protected PreparedStatement prepareFirstStatement(Connection connection,
			String query, int returnGeneratedKeys) throws SQLException {
		PreparedStatement st = connection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
		

		st.setString(1, userName);
		st.setString(2, sessionId);
		st.setString(3, firstName);
		st.setString(4, secondName);
		st.setString(5, company);
		st.setString(6, email);
		st.setString(7, tel);
		st.setString(8, comment);

		return st;
	}

}
