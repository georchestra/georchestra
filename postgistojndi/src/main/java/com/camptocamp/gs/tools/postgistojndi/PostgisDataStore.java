/**
 * 
 */
package com.camptocamp.gs.tools.postgistojndi;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Mauricio Pazos
 *
 */
final class PostgisDataStore {
	
	private final static String ID = "id";
	private final static String NAME = "name";
	private final static String TYPE = "type";
	private final static String ENABLED = "enabled";
	private final static String WORKSPACE = "workspace";
	private final static String WS_ID = "id";
	private final static String PARAMETERS = "connectionParameters";
	private final static String SCHEMA = "schema";
	private final static String DBTYPE = "dbtype";
	private final static String LOOSE_BOX = "Loose bbox";
	private final static String EXPOSE_PK = "Expose primary keys";
	private final static String PREP_STMT = "preparedStatements";
	private final static String MAX_PREP_STMT = "Max open prepared statements";
	private final static String JNDI_REF = "jndiReferenceName";
	private final static String NAME_SPACE = "namespace";
	private final static String DEFAULT = "__default";
	
	private PostgisDataStore(){}
	
	/**
	 * FIXME 
	 * @return
	 * @throws Exception
	 */
	public Document create() throws Exception{

			// create the jndi document
			DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = df.newDocumentBuilder();
			Document jndiDoc = builder.newDocument();
			
			// create the root
			Element jndiRoot = jndiDoc.createElement("dataStore");
			jndiDoc.appendChild(jndiRoot);
			
			// create the elements
			Element id = jndiDoc.createElement(ID);
			id.appendChild(jndiDoc.createTextNode("todo"));
			jndiRoot.appendChild(id);
			
				
			Element name = jndiDoc.createElement(NAME);
			name.appendChild(jndiDoc.createTextNode("todo"));
			jndiRoot.appendChild(name);
				
			Element type = jndiDoc.createElement(TYPE);
			type.appendChild(jndiDoc.createTextNode("todo"));
			jndiRoot.appendChild(type);

			Element enabled = jndiDoc.createElement(ENABLED);
			enabled.appendChild(jndiDoc.createTextNode("todo"));
			jndiRoot.appendChild(enabled);
			
			Element workspace = jndiDoc.createElement(WORKSPACE);
			Element wsId = jndiDoc.createElement(WS_ID);
			wsId.appendChild(jndiDoc.createTextNode("todo"));
			workspace.appendChild(wsId);
			jndiRoot.appendChild(workspace);

			// set the parameters
			Element connectionParameters = jndiDoc.createElement(PARAMETERS);
			jndiRoot.appendChild(connectionParameters);

			// parameter: schema entry
			Element entrySchema = jndiDoc.createElement("entry");
			entrySchema.appendChild(jndiDoc.createTextNode("todo"));
			connectionParameters.appendChild(entrySchema);

			Attr schema = jndiDoc.createAttribute("key");
			schema.setValue(SCHEMA);
			entrySchema.setAttributeNode(schema);
			
			// parameter: dbtype entry
			Element entryDbType = jndiDoc.createElement("entry");
			entryDbType.appendChild(jndiDoc.createTextNode("todo"));
			connectionParameters.appendChild(entryDbType);

			Attr dbType = jndiDoc.createAttribute("key");
			dbType.setValue(DBTYPE);
			entryDbType.setAttributeNode(dbType);
			
			// parameter: Loose bbox entry
			Element entryLooseBbox = jndiDoc.createElement("entry");
			entryLooseBbox.appendChild(jndiDoc.createTextNode("todo"));
			connectionParameters.appendChild(entryLooseBbox);

			Attr looseBox = jndiDoc.createAttribute("key");
			looseBox.setValue(LOOSE_BOX);
			entryLooseBbox.setAttributeNode(looseBox);

			// parameter: Expose primary keys
			Element entityExposePrimaryKey = jndiDoc.createElement("entry");
			entityExposePrimaryKey.appendChild(jndiDoc.createTextNode("todo"));
			connectionParameters.appendChild(entityExposePrimaryKey);

			Attr primaryKey = jndiDoc.createAttribute("key");
			primaryKey.setValue(EXPOSE_PK);
			entityExposePrimaryKey.setAttributeNode(primaryKey);
			
			// parameter: preparedStatements
			Element entityPrepStmt = jndiDoc.createElement("entry");
			entityPrepStmt.appendChild(jndiDoc.createTextNode("todo"));
			connectionParameters.appendChild(entityPrepStmt);

			Attr prepStmt = jndiDoc.createAttribute("key");
			prepStmt.setValue(PREP_STMT);
			entityPrepStmt.setAttributeNode(prepStmt);
			
			// parameter: Max open prepared statements
			Element entityMaxOpenPrepStmt = jndiDoc.createElement("entry");
			entityMaxOpenPrepStmt.appendChild(jndiDoc.createTextNode("todo"));
			connectionParameters.appendChild(entityMaxOpenPrepStmt);

			Attr maxOpenPrepStmt = jndiDoc.createAttribute("key");
			maxOpenPrepStmt.setValue(MAX_PREP_STMT);
			entityMaxOpenPrepStmt.setAttributeNode(maxOpenPrepStmt);
			
			// parameter: Max open prepared statements
			Element entityJndiRef = jndiDoc.createElement("entry");
			entityJndiRef.appendChild(jndiDoc.createTextNode("todo"));
			connectionParameters.appendChild(entityJndiRef);

			Attr jndiRef = jndiDoc.createAttribute("key");
			jndiRef.setValue(JNDI_REF);
			entityJndiRef.setAttributeNode(jndiRef);
			
			// parameter: Max open prepared statements
			Element entityNameSpace = jndiDoc.createElement("entry");
			entityNameSpace.appendChild(jndiDoc.createTextNode("todo"));
			connectionParameters.appendChild(entityNameSpace);

			Attr nameSpace = jndiDoc.createAttribute("key");
			nameSpace.setValue(NAME_SPACE);
			entityNameSpace.setAttributeNode(nameSpace);

			Element _default = jndiDoc.createElement(DEFAULT);
			_default.appendChild(jndiDoc.createTextNode("todo"));
			jndiRoot.appendChild(_default);
			
			return jndiDoc;
	}
	
	

}
