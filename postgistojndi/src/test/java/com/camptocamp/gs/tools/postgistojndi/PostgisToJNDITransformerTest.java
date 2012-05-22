/**
 * 
 */
package com.camptocamp.gs.tools.postgistojndi;


import java.io.File;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Test case for PostgisToJNDITransformer
 * 
 * @author Mauricio Pazos
 *
 */
public class PostgisToJNDITransformerTest {

	private static final String jndiRef = "jdbc/postgres/geoserverdb";


	private static void createTestResultDirectory(){
		File target = new File("target/test-result");
		if( !target.exists() ){
			target.mkdirs();
		}

	}

	@Test
	public void testTransformDataStoreXmlFile() throws Exception {

		PostgisToJNDITransformer creator = new PostgisToJNDITransformer();
		
		Document postgisDoc = DataStoreDescriptor.read("src/test/resources/workspaces/ws-test/geoserverdb-test/datastore.xml");
		
		
		Document jndiDoc = creator.transform(postgisDoc, jndiRef);

		createTestResultDirectory();
		DataStoreDescriptor.write("target/test-result/datastore.xml", jndiDoc);
	}
	
	@Test
	public void testTransformDataStoreWithEmptyValues() throws Exception {

		PostgisToJNDITransformer creator = new PostgisToJNDITransformer();
		
		Document postgisDoc = DataStoreDescriptor.read("src/test/resources/datastore.xml");
		
		Document jndiDoc = creator.transform(postgisDoc, jndiRef);

		createTestResultDirectory();
		DataStoreDescriptor.write("target/test-result/datastore-data-source-test-empty.xml", jndiDoc);
	}

}
