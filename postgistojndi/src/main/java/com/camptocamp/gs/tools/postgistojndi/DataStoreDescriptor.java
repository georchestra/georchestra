/**
 * 
 */
package com.camptocamp.gs.tools.postgistojndi;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Read and Write the datastore.xml
 * 
 * @author Mauricio Pazos
 *
 */
class DataStoreDescriptor {

	
	/**
	 * 
	 * @param dataStorePath
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static Document read(String dataStorePath) throws SAXException, IOException, ParserConfigurationException {
	    
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(dataStorePath);	
	
		return doc;
	}
	
	/**
	 * 
	 * @param dataStorePath
	 * @param dataStoreDoc
	 * @throws TransformerException
	 */
	public static void write(String dataStorePath, Document dataStoreDoc ) throws TransformerException {

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		DOMSource source = new DOMSource(dataStoreDoc);
		
		StreamResult result = new StreamResult(new File(dataStorePath));
		
		//StreamResult result = new StreamResult(System.out);// used for testing
		
		transformer.transform(source, result);
	}
	
	
	public static boolean isPostgis(String dataStore) throws SAXException, IOException, ParserConfigurationException{
	
		Document jndiDoc = read(dataStore);
		Node node = jndiDoc.getFirstChild();
		Node type =  jndiDoc.getElementsByTagName("type").item(0);
		if(type == null) return false; // some datasotre hasn't type like shp
		
		String typeValue = type.getTextContent();

		boolean bool = "PostGIS".equalsIgnoreCase(typeValue);
		
		return bool;
	}
	
}
