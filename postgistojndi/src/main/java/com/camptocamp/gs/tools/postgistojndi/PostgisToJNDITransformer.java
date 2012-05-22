/**
 * 
 */
package com.camptocamp.gs.tools.postgistojndi;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Transform a postgis xml configuration to postgis jndi
 *  
 * @author Mauricio Pazos
 *
 */
final class PostgisToJNDITransformer {
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String DESCRIPTION = "description";
	private static final String TYPE = "type";
	private static final String ENABLED = "enabled";
	private static final String WORKSPACE = "workspace";
	private static final String WS_ID = "id";
	private static final String PARAMETERS = "connectionParameters";
	private static final String SCHEMA = "schema";
	private static final String DBTYPE = "dbtype";
	private static final String LOOSE_BOX = "Loose bbox";
	private static final String EXPOSE_PK = "Expose primary keys";
	private static final String PREP_STMT = "preparedStatements";
	private static final String MAX_PREP_STMT = "Max open prepared statements";
	private static final String PK_METADATA_TABLE = "Primary key metadata table";
	private static final String JNDI_REF = "jndiReferenceName";
	private static final String NAME_SPACE = "namespace";
	private static final String DEFAULT = "__default";
	
	// used to create the jndi template
	private static final String INITIAL_VALUE = "NOT UPDATED";
	private static final String DEFAULT_VALUE = "";
	/**
	 * 
	 * @param postgis
	 * @param dataStoreRefName
	 * @return
	 * @throws Exception
	 */
	public Document transform(final Document postgis, final String dataStoreRefName) throws Exception{

		Document jndiDoc = createPostgisJNDIDataStore();
		
		// modify the jndi template with the postgis values
		Node id = jndiDoc.getElementsByTagName(ID).item(0);
		final String idValue = postgis.getElementsByTagName(ID).item(0).getTextContent();
		id.setTextContent(idValue);
	
		Node name = jndiDoc.getElementsByTagName(NAME).item(0);
		String nameValue = postgis.getElementsByTagName(NAME).item(0).getTextContent();
		name.setTextContent(nameValue);
		
		Node desc = jndiDoc.getElementsByTagName(DESCRIPTION).item(0);
		String descValue = getElementValue(postgis, DESCRIPTION); 
		desc.setTextContent(descValue);

		// value for posgis jndi type
		Node type = jndiDoc.getElementsByTagName(TYPE).item(0);
		type.setTextContent("PostGIS (JNDI)");
		
		Node enabled = jndiDoc.getElementsByTagName(ENABLED).item(0);
		String enabledValue = postgis.getElementsByTagName(ENABLED).item(0).getTextContent();
		enabled.setTextContent(enabledValue);
		
		// set the workspace's id
		Node workspace = jndiDoc.getElementsByTagName(WORKSPACE).item(0);
		Node nodeWorkspaceID = workspace.getFirstChild();

		Node postgisWS = postgis.getElementsByTagName(WORKSPACE).item(0);
		Node firstChild = postgisWS.getFirstChild().getNextSibling();
		String wsIDValue = firstChild.getTextContent();
		
		nodeWorkspaceID.setTextContent(wsIDValue);
		
		// set the param
		Node connectionParameters = jndiDoc.getElementsByTagName(PARAMETERS).item(0);
		NodeList param = connectionParameters.getChildNodes();
		for (int i = 0; i < param.getLength(); i++) {
			
			Node node = param.item(i);
			Node keyNode =node.getAttributes().getNamedItem("key");
			String keyValue = keyNode.getNodeValue();

			if( SCHEMA.equals(keyValue) ){
				String value = getParameter(postgis, SCHEMA);
				node.setTextContent(value);
			} else if( DBTYPE.equals(keyValue) ){
				String value = getParameter(postgis, DBTYPE);
				if(!"postgis".equalsIgnoreCase(value.trim())){
					throw new IllegalStateException("Failed processing "+idValue+". postgis type is expected but was " + value);
				}
				node.setTextContent(value);
				
			} else if( PK_METADATA_TABLE.equals(keyValue) ){
				String value = getParameter(postgis, PK_METADATA_TABLE);
				node.setTextContent(value);
			} else if( LOOSE_BOX.equals(keyValue) ){
				String value = getParameter(postgis, LOOSE_BOX);
				node.setTextContent(value);
			} else if( EXPOSE_PK.equals(keyValue) ){
				String value = getParameter(postgis, EXPOSE_PK);
				node.setTextContent(value);
			} else if( PREP_STMT.equals(keyValue) ){
				String value = getParameter(postgis, PREP_STMT);
				node.setTextContent(value);
			} else if( MAX_PREP_STMT.equals(keyValue) ){
				String value = getParameter(postgis, MAX_PREP_STMT);
				node.setTextContent(value);
			} else if( NAME_SPACE.equals(keyValue) ){
				String value = getParameter(postgis, NAME_SPACE);
				node.setTextContent(value);
				
			} else if( JNDI_REF.equals(keyValue) ){
				// new value
				node.setTextContent("java:comp/env/"+ dataStoreRefName);
			} else {
				throw new IllegalStateException("unspected parameter: "+ keyValue);
			}
		}
		Node _default = jndiDoc.getElementsByTagName(DEFAULT).item(0);
		String defaultValue = postgis.getElementsByTagName(ENABLED).item(0).getTextContent();
		_default.setTextContent(defaultValue);

		return jndiDoc;
	}
	

	private String getElementValue(Document postgis, String description2) {
		NodeList element = postgis.getElementsByTagName(DESCRIPTION);
		if(element == null) return DEFAULT_VALUE;
		Node item = element.item(0);
		if(item == null) return DEFAULT_VALUE;
		String value = item.getTextContent();		
		
		return value;
	}


	/**
	 * Searchs the element with the key provided as parameter to return the associated value. 
	 * @param postgis xml document that defines the postgis datastore 
	 * @param elementKey
	 * @return value of element with the key provided
	 */
	private String getParameter(final Document postgis, String elementKey) {
		
		Node connectionParameters = postgis.getElementsByTagName(PARAMETERS).item(0);
		NodeList param = connectionParameters.getChildNodes();
		
		String value = "";
		int length = param.getLength();
		for (int i = 0; i < length; i++) {
			
			Node node = param.item(i);
			NamedNodeMap attributes = node.getAttributes();
			if(attributes == null) continue;
			Node keyNode =attributes.getNamedItem("key");
			String keyValue = keyNode.getNodeValue();

			if( elementKey.equals(keyValue) ){
				value = node.getTextContent();
				break;
			} 		
		}
		return value;
	}


	private Document createPostgisJNDIDataStore() throws ParserConfigurationException {
		// create the jndi document
		DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = df.newDocumentBuilder();
		Document jndiDoc = builder.newDocument();
		
		// create the root
		Element jndiRoot = jndiDoc.createElement("dataStore");
		jndiDoc.appendChild(jndiRoot);
		
		// create the elements
		Element id = jndiDoc.createElement(ID);
		id.appendChild(jndiDoc.createTextNode(INITIAL_VALUE));
		jndiRoot.appendChild(id);
		
			
		Element name = jndiDoc.createElement(NAME);
		name.appendChild(jndiDoc.createTextNode(INITIAL_VALUE));
		jndiRoot.appendChild(name);
		
		Element desc = jndiDoc.createElement(DESCRIPTION);
		desc.appendChild(jndiDoc.createTextNode(INITIAL_VALUE));
		jndiRoot.appendChild(desc);
		
			
		Element type = jndiDoc.createElement(TYPE);
		type.appendChild(jndiDoc.createTextNode(INITIAL_VALUE));
		jndiRoot.appendChild(type);

		Element enabled = jndiDoc.createElement(ENABLED);
		enabled.appendChild(jndiDoc.createTextNode(INITIAL_VALUE));
		jndiRoot.appendChild(enabled);
		
		Element workspace = jndiDoc.createElement(WORKSPACE);
		Element wsId = jndiDoc.createElement(WS_ID);
		wsId.appendChild(jndiDoc.createTextNode(INITIAL_VALUE));
		workspace.appendChild(wsId);
		jndiRoot.appendChild(workspace);

		// set the parameters
		Element connectionParameters = jndiDoc.createElement(PARAMETERS);
		jndiRoot.appendChild(connectionParameters);

		// parameter: schema entry
		Element entrySchema = jndiDoc.createElement("entry");
		entrySchema.appendChild(jndiDoc.createTextNode(INITIAL_VALUE));
		connectionParameters.appendChild(entrySchema);

		Attr schema = jndiDoc.createAttribute("key");
		schema.setValue(SCHEMA);
		entrySchema.setAttributeNode(schema);
		
		// parameter: dbtype entry
		Element entryDbType = jndiDoc.createElement("entry");
		entryDbType.appendChild(jndiDoc.createTextNode(INITIAL_VALUE));
		connectionParameters.appendChild(entryDbType);

		Attr dbType = jndiDoc.createAttribute("key");
		dbType.setValue(DBTYPE);
		entryDbType.setAttributeNode(dbType);
		
		// parameter: Loose bbox entry
		Element entryLooseBbox = jndiDoc.createElement("entry");
		entryLooseBbox.appendChild(jndiDoc.createTextNode(INITIAL_VALUE));
		connectionParameters.appendChild(entryLooseBbox);

		Attr looseBox = jndiDoc.createAttribute("key");
		looseBox.setValue(LOOSE_BOX);
		entryLooseBbox.setAttributeNode(looseBox);

		// parameter: Expose primary keys
		Element entityExposePrimaryKey = jndiDoc.createElement("entry");
		entityExposePrimaryKey.appendChild(jndiDoc.createTextNode(INITIAL_VALUE));
		connectionParameters.appendChild(entityExposePrimaryKey);

		Attr primaryKey = jndiDoc.createAttribute("key");
		primaryKey.setValue(EXPOSE_PK);
		entityExposePrimaryKey.setAttributeNode(primaryKey);
		
		// parameter: preparedStatements
		Element entityPrepStmt = jndiDoc.createElement("entry");
		entityPrepStmt.appendChild(jndiDoc.createTextNode(INITIAL_VALUE));
		connectionParameters.appendChild(entityPrepStmt);

		Attr prepStmt = jndiDoc.createAttribute("key");
		prepStmt.setValue(PREP_STMT);
		entityPrepStmt.setAttributeNode(prepStmt);
		
		// parameter: Max open prepared statements
		Element entityMaxOpenPrepStmt = jndiDoc.createElement("entry");
		entityMaxOpenPrepStmt.appendChild(jndiDoc.createTextNode(INITIAL_VALUE));
		connectionParameters.appendChild(entityMaxOpenPrepStmt);

		Attr maxOpenPrepStmt = jndiDoc.createAttribute("key");
		maxOpenPrepStmt.setValue(MAX_PREP_STMT);
		entityMaxOpenPrepStmt.setAttributeNode(maxOpenPrepStmt);
		
		// parameter: Primary key metadata table
		Element pkMetadataTable = jndiDoc.createElement("entry");
		pkMetadataTable.appendChild(jndiDoc.createTextNode(INITIAL_VALUE));
		connectionParameters.appendChild(pkMetadataTable);

		Attr pkMetadataTableKey = jndiDoc.createAttribute("key");
		pkMetadataTableKey.setValue(PK_METADATA_TABLE);
		pkMetadataTable.setAttributeNode(pkMetadataTableKey);
				
		
		// parameter: Max open prepared statements
		Element entityJndiRef = jndiDoc.createElement("entry");
		entityJndiRef.appendChild(jndiDoc.createTextNode(INITIAL_VALUE));
		connectionParameters.appendChild(entityJndiRef);

		Attr jndiRef = jndiDoc.createAttribute("key");
		jndiRef.setValue(JNDI_REF);
		entityJndiRef.setAttributeNode(jndiRef);
		
		// parameter: Max open prepared statements
		Element entityNameSpace = jndiDoc.createElement("entry");
		entityNameSpace.appendChild(jndiDoc.createTextNode(INITIAL_VALUE));
		connectionParameters.appendChild(entityNameSpace);

		Attr nameSpace = jndiDoc.createAttribute("key");
		nameSpace.setValue(NAME_SPACE);
		entityNameSpace.setAttributeNode(nameSpace);

		Element _default = jndiDoc.createElement(DEFAULT);
		_default.appendChild(jndiDoc.createTextNode(INITIAL_VALUE));
		jndiRoot.appendChild(_default);
		
		return jndiDoc;
	}
	
	

}
