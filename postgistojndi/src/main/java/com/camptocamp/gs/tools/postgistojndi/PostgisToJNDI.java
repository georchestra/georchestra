package com.camptocamp.gs.tools.postgistojndi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This tool migrate the Postgis data store to Postgis (JNDI) type.
 * 
 * The postgis configuration present in the datastore.xml file in the <b>data/workspaces/</b> directory will be
 * changed if it type is <b>Postgis</b>
 * </p>
 * <pre>
 * <b>EXAMPLE:</b>
 * <code>
 * <dataStore>
 * 
 * <b>existent in postgis (source)</b>
 *   <id>DataStoreInfoImpl--57d94f54:13755ddcd2e:-8000</id>
 *   <name>geoserverdb-direct</name>
 * <b>changed</b>
 *   <type>PostGIS</type>
 * <b>by</b>
 *   <type>PostGIS (JNDI)</type>
 * 
 * <b>existent in postgis (source)</b>
 *   <enabled>true</enabled>
 *   <workspace>
 *     <id>WorkspaceInfoImpl-7bc72c1b:13755fc1fc6:-7ffd</id>
 *   </workspace>
 *   <connectionParameters>
 * <b>new</b>    
 *     <entry key="jndiReferenceName">java:comp/env/jdbc/postgres/geoserverdb</entry>
 * 
 * <b>existent in postgis (source)</b>
 *     <entry key="namespace">localhost</entry>
 *     <entry key="dbtype">postgis</entry>
 *    <entry key="Loose bbox">true</entry>
 *    <entry key="schema">public</entry>
 *     <entry key="Expose primary keys">false</entry>
 *     <entry key="preparedStatements">false</entry>
 *     <entry key="Max open prepared statements">50</entry>
 *   </connectionParameters>
 * 
 *   <__default>false</__default>
 * </dataStore>
 * </code>
 * 
 * </pre>
 * 
 * <p>
 * <b>USAGE:</b> java -jar postgistojndi-0.0.1-SNAPSHOT.jar source target resourceRef
 * </p>
 * <p>
 * <b>source:</b>  it workspaces folder. This folder is not modified in this process.
 * <b>target:</b>  the folder where the new workspaces structure will be created
 * <b>resourceRef:</b>  take this value from geoserver web.xml file
 * </p>
 * <p>
 * <b>Example:</b>
 * <pre>
 * java -jar postgistojndi-0.0.1-SNAPSHOT.jar src/test/resources/workspaces target/test jdbc/postgres/geoserverdb
 * </pre>
 * </p>
 * @author Mauricio Pazos
 *
 */

public class PostgisToJNDI 
{
	private static PostgisToJNDITransformer TRANSFORMER = new PostgisToJNDITransformer();

	private static String resourceReference;
	
	private static int COUNT_MIRATED = 0;
	
    public static void main( String[] args )
    {
    	// check required parameters
    	if(args.length < 3){
    		String usage = "USAGE: java -jar postgistojndi-0.0.1-SNAPSHOT.jar source target resourceRef";
        	System.out.println("Error: three (3) arguments are expected\n\n" + usage);
        	return;
    	}
        String source = args[0];
        String target = args[1];
        resourceReference = args[2];

        // check source is not equals to target 
		System.out.println( "Migrating" );
		System.out.println( "Source: "+ source );
        System.out.println( "Target: "+ target );
    	
        File fSource = new File(source);
        File fTarget = new File(target+"/workspaces");
		if (!fTarget.exists()) {
			fTarget.mkdir();
		}
        
        if(fSource.toString().equals(fTarget.toString())){
        	System.out.println("Error: source and target cannot be equals");
        	
        	return;
        }
        
    	// traverse the workspaces for those datastore with type Posgis write a new datastore.xml 
    	// in the target with the changes required by the Postgis JNDI type.
        try {
			createNewWorkspaces(fSource, fTarget);
			
			System.out.println( "Migrating ..." );
			
	    	// print result
	        System.out.println( "Migration finished" );
	        System.out.println( "Source: "+ fSource.toString() );
	        System.out.println( "Target: "+ fTarget.toString() );
	        System.out.println( "Datastores migrated to PostGIS (JNDI)" + COUNT_MIRATED);

        } catch (Exception e) {
	        System.out.println( "Fail!:" + e.getMessage() );
		}
    }
    
	private static void createNewWorkspaces(final File source, File target) throws Exception {

		if (source.isDirectory()) {
			// if directory not exists, create it
			if (!target.exists()) {
				target.mkdir();
			}
			// list all the directory contents
			for (String file : source.list()) {
				// construct the src and dest file structure
				File srcFile = new File(source, file);
				File destFile = new File(target, file);
				createNewWorkspaces(srcFile, destFile);
			}

		} else {
			// copy the file
			InputStream in = new FileInputStream(source);
			OutputStream out = new FileOutputStream(target);

			byte[] buffer = new byte[1024];

			int length;
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}
			in.close();
			out.close();
			// if the file is a postgis configuration then applies it the transformation to Postgis (jndi) type
			if(isPostgisDatastore(source)){
				transform(source.toString(), target.toString());
				COUNT_MIRATED++;
			}
		}
	}
	
	private static boolean isPostgisDatastore(final File source) throws Exception {

		final String sourcePath = source.toString();
		int pos = sourcePath.indexOf("datastore.xml");
		if( pos != -1){
			return DataStoreDescriptor.isPostgis(sourcePath);
		}
		
		return false;
	}

	private static void transform(String inDataStore, String outDataStore) throws Exception{

			Document postgisDoc = DataStoreDescriptor.read(inDataStore);
			
			Document jndiDoc = TRANSFORMER.transform(postgisDoc, resourceReference);
			
			DataStoreDescriptor.write(outDataStore, jndiDoc);
	}
    
}
