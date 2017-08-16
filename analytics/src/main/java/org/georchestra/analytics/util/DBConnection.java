package org.georchestra.analytics.util;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBConnection {

    private DataSource dataSource;
    private Pattern namedParameterPattern;
    private Connection nativeConnection;

    public DBConnection(DataSource jpaDataSource) throws PropertyVetoException, SQLException {

        this.namedParameterPattern = Pattern.compile("\\{(\\w+)\\}");
        this.nativeConnection = jpaDataSource.getConnection();
        this.dataSource = jpaDataSource;
    }

    private void checkConnection() throws SQLException {

        try {
            Statement st = this.nativeConnection.createStatement();
            st.executeQuery("SELECT 1");
        } catch (SQLException e) {
            try{
                this.nativeConnection.close();
            } catch(SQLException ex){}

            // Try to reconnect to DB one time
            this.nativeConnection = this.dataSource.getConnection();
        }
    }

    public String generateQuery(String sql, Map<String, Object> values) throws SQLException {

        // Check connection to database
        this.checkConnection();

        List<String> parameterNames = new LinkedList<String>();

        // Replace named parameter with standard prepared statement parameter : '?'
        Matcher m = this.namedParameterPattern.matcher(sql);
        while(m.find()){
            String parameterName = m.group(1);
            parameterNames.add(parameterName);
            sql = sql.replaceFirst("\\{" + parameterName + "\\}","?");
            m = this.namedParameterPattern.matcher(sql);
        }

        // Create statement
        PreparedStatement st = this.nativeConnection.prepareStatement(sql);

        for(int i=0;i<parameterNames.size();i++){
            // Check if parameter is defined
            if(!values.containsKey(parameterNames.get(i)))
                throw new IllegalArgumentException("No value specified for parameter : "
                        + parameterNames.get(i) + " in " + sql);
            st.setObject(i + 1, values.get(parameterNames.get(i)));
        }
        return st.toString();
    }

    public ResultSet execute(String query) throws SQLException {
        // Check connection to database :
        this.checkConnection();
        return this.nativeConnection.createStatement().executeQuery(query);

    }



}
