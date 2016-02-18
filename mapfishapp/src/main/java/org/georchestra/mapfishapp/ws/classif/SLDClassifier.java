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

package org.georchestra.mapfishapp.ws.classif;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.georchestra.mapfishapp.ws.DocServiceException;
import org.georchestra.mapfishapp.ws.classif.ClassifierCommand.E_ClassifType;
import org.geotools.data.DataSourceException;
import org.geotools.data.FeatureSource;
import org.geotools.data.wfs.impl.WFSContentDataStore;
import org.geotools.data.wfs.impl.WFSDataStoreFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.SLDTransformer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.NamedLayer;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Provides automatic styling by generating a SLD file given a parameterizable request on a WFS. <br />
 * Types of display: polygons filled with colors or proportional symbols <br />
 * Types of classification: on continuous or discrete values (Quantile method only) <br />
 * Check ClassifierCommand to see all the parameters that need to be provided.
 * @see ClassifierCommand
 * @author yoann.buch@gmail.com
 *
 */
public class SLDClassifier {
    
    private ClassifierCommand _command = null;
    private StyledLayerDescriptor _sld = null;
    private Map<String, UsernamePasswordCredentials> _credentials;
    
    private WFSDataStoreFactory _factory = new WFSDataStoreFactory();
    
    public void setWFSDataStoreFactory(WFSDataStoreFactory f) { _factory = f; } 
    
    /**
     * This classifier can only be requested by a ClassifierCommand given the wide range of cases and different 
     * parameters. The SLD is directly generated and be accessed via {@link SLDClassifier#getSLD()}
     * @param command ClassifierCommand provides the type of classification and display
     * @throws DocServiceException When client request is not valid
     */
    public SLDClassifier(Map<String, UsernamePasswordCredentials> credentials, final ClassifierCommand command,
            WFSDataStoreFactory fac) throws DocServiceException {
        this._credentials = credentials;

        // wfs-ng specific: If we do not have the prefix URL, then we need to
        // use a typename as string where the ":" has been replaced by an
        // underscore. Since we expect the controller to be called with the
        // "prefix:layername" pattern and we do not want to add an extra logic
        // to get the prefix URL in the code, we replace the character by hand.

        String ftName = command.getFeatureTypeName().replaceFirst(":", "_");
        command.setFeatureTypeName(ftName);
        _command = command;
        if (fac != null)
            _factory = fac;
        // turn off logger
        Handler[] handlers = Logger.getLogger("").getHandlers();
        for (int index = 0; index < handlers.length; index++) {
            handlers[index].setLevel(Level.OFF);
        }

        // start directly the classification
        doClassification();
    }

	/**
     * Gets the generated SLD file content 
     * @return SLD content as String
     */
    public String getSLD() {
        
        if (_sld == null) {
            throw new RuntimeException("sld has not been generated yet");
        }
        
        // transform SLD object into String
        SLDTransformer aTransformer = new SLDTransformer();
        aTransformer.setIndentation(4);
        String xml = "";
        // BUG: this code can certainly cause some issues in a global context
        // (think other webapps like GeoNetwork in the same servlet container).
        String oldTransformer = System.getProperty("javax.xml.transform.TransformerFactory");
        try {        
            System.setProperty("javax.xml.transform.TransformerFactory", org.apache.xalan.processor.TransformerFactoryImpl.class.getName());
            xml = aTransformer.transform(_sld);
        } catch (TransformerException e) {
            e.printStackTrace();
        } finally {
            if(oldTransformer != null) {
                System.setProperty("javax.xml.transform.TransformerFactory", oldTransformer);
            }
        }
        
        return xml;
    }

    /**
     * Upload all the features from the WFS and then prepare the factories to fulfill the different type of
     * classifications and displays
     * @throws DocServiceException
     */
    private void doClassification() throws DocServiceException {
        try {
            
            // connect to the remote WFS
            WFSContentDataStore wfs = connectToWFS(_command.getWFSUrl());
            
            // check if property name exists
            String ftName = _command.getFeatureTypeName();
            SimpleFeatureType ft = wfs.getSchema(ftName);
            int index = ft.indexOf(_command.getPropertyName());
            if(index == -1) {
                throw new DocServiceException(_command.getPropertyName() + " is not an attribute of " + _command.getFeatureTypeName(),
                        HttpServletResponse.SC_BAD_REQUEST);
            }
                       
            // Load all the features
            FeatureSource<SimpleFeatureType, SimpleFeature> source = wfs.getFeatureSource(ftName);
            FeatureCollection<SimpleFeatureType, SimpleFeature> featuresCollection = source.getFeatures();

            // We need a display (Symbolizers) and a value (Filters) fatories to generate a SLD file
            I_SymbolizerFactory symbolizerFact = null; // create symbols
            I_FilterFactory filterFact = null; // create filters
            
            // execute different type of classification given the type requested by user
            if (_command.getClassifType() == E_ClassifType.CHOROPLETHS ||
                _command.getClassifType() == E_ClassifType.PROP_SYMBOLS) {
                
                // Classification on continuous values. Sorting is needed to classify: 
                // Double values are mandatory (for now)

                if (getDataType(wfs) == String.class) {
                    // choropleths and prop symbols use quantile classification
                    // therefore classify on string type has no purpose
                    throw new DocServiceException("Classification on continous values (" + _command.getClassifType()+ ").\n" +
                    		"Attribute " + _command.getPropertyName() + " is string type." +
                    		" Therefore no classification on contiuous values can be done." +
                    		" It needs be a meaningful comparable type (numerical, date...)." +
                    		" Use unique values classification instead." , 
                            HttpServletResponse.SC_BAD_REQUEST);
                } else if ((getDataType(wfs) != Double.class) &&
                        (getDataType(wfs) != Float.class) && 
                        (getDataType(wfs) != Integer.class) && 
                        (getDataType(wfs) != Long.class) && 
                        (getDataType(wfs) != Short.class)) {
                    // for now, only double, float, integer, and short types are supported
                    // FIXME deal with others numerical types, dates...
                    // they all must be comparable type as sorting is required for classification
                    throw new DocServiceException("Classification on " + getDataType(wfs).getName() +
                    		" type is not supported.",
                            HttpServletResponse.SC_NOT_IMPLEMENTED);
                }
                    
                // get values to classify
                ArrayList<Double> values  = getDoubleValues(featuresCollection.features(), _command.getPropertyName());
                filterFact = new ContinuousFilterFactory(values, _command.getClassCount(), _command.getPropertyName());        
                
                if (_command.getClassifType() == E_ClassifType.CHOROPLETHS) {
                    switch (_command.getSymbolType()) {
                        case POLYGON:
                            symbolizerFact = new PolygonSymbolizerFactory(_command.getClassCount(), _command.getFirstColor(), _command.getLastColor());
                            break;
                        case LINE:
                            symbolizerFact = new LineSymbolizerFactory(_command.getClassCount(), _command.getFirstColor(), _command.getLastColor());
                            break;
                        case POINT:
                            symbolizerFact = new PointSymbolizerFactory(_command.getClassCount(), _command.getFirstColor(), _command.getLastColor());
                            break;
                        default:
                            throw new DocServiceException("Choropleths classification on symbol type: " + _command.getSymbolType() +
                                    " is not supported.", HttpServletResponse.SC_BAD_REQUEST);
                    }
                }
                else if (_command.getClassifType() == E_ClassifType.PROP_SYMBOLS) {
                    switch (_command.getSymbolType()) {
                        case LINE:
                            symbolizerFact = new LineSymbolizerFactory(_command.getClassCount(), _command.getMinSize(), _command.getMaxSize());
                            // customizing is possible
                            // symbolizerFact.setColor(Color.BLUE);
                            break;
                        case POINT:
                            symbolizerFact = new PointSymbolizerFactory(_command.getClassCount(), _command.getMinSize(), _command.getMaxSize());
                            // customizing is possible
                            // symbolizerFact.setColor(Color.BLUE);
                            // symbolizerFact.setSymbol(StyleBuilder.MARK_CROSS);
                            break;
                        default:
                            throw new DocServiceException("Proportional symbols classification on symbol type: " + _command.getSymbolType() +
                                    " is not supported.", HttpServletResponse.SC_BAD_REQUEST);
                    }
                }       
            }
            else if (_command.getClassifType() == E_ClassifType.UNIQUE_VALUES ) {

                // no needs to classify on Unique Values. They can be kept as Strings.
                Set<String> values  = getUniqueStringValues(featuresCollection.features(), _command.getPropertyName());
                filterFact = new DiscreteFilterFactory(values, _command.getPropertyName());

                switch (_command.getSymbolType()) {
                    case POLYGON:
                        symbolizerFact = new PolygonSymbolizerFactory(_command.getPaletteID(), values.size());
                        break;
                    case LINE:
                        symbolizerFact = new LineSymbolizerFactory(_command.getPaletteID(), values.size());
                        break;
                    case POINT:
                        symbolizerFact = new PointSymbolizerFactory(_command.getPaletteID(), values.size());
                        break;
                    default:
                        throw new DocServiceException("Unique values classification on symbol type: " + _command.getSymbolType() +
                                " is not supported.", HttpServletResponse.SC_BAD_REQUEST);
                }
            }
            else {
                throw new DocServiceException("Unknown classification type: " + _command.getClassifType(),
                        HttpServletResponse.SC_BAD_REQUEST);
            }
            
            assert(symbolizerFact != null);
            assert(filterFact != null);
            
            // With those 2 factories a FeatureTypeStyle can be created
            FeatureTypeStyle fts = createFeatureTypeStyle(filterFact, symbolizerFact);
            
            // Use FeatureTypeStyle to generate a complete SLD object
            _sld = createSLD(fts);
        } 
        catch (IOException e) {
            e.printStackTrace(); // could happened when communicating with WFS
        }
    }
    
    /**
     * Creates a FeatureTypeStyle (core part of a SLD file). It is composed by Rules (tag <sld:Rule>) and each Rule 
     * can contain one Filter (filters values) and one Symbolizer (what's displayed). It Needs 2 factories: <br />
     * - I_FilterFactory: gives Filters (tag <ogc:Filter>) <br />
     * - I_SymbolizerFactory: gives Symbolizers (tag: <sld:PolygonSymbolizer> or <sld:PointSymbolizer>) <br />
     * @param filterFact Filters Factory
     * @param symbolizerFact Symbolizers Factory
     * @return FeatureTypeStyle (List of Rules)
     */
    private FeatureTypeStyle createFeatureTypeStyle(I_FilterFactory filterFact, I_SymbolizerFactory symbolizerFact) {
        
        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle();
        Iterator<Symbolizer> symbolizers = symbolizerFact.iterator();
        Iterator<Filter> filters = filterFact.iterator();

        // use each factory iterator to get their items (Filter and Symbolizer)
        // there should be as many symbolizers as many filters
        while(symbolizers.hasNext() && filters.hasNext()) {

            // create a Rule. A Rule is composed by one Filter and one Symbolizer
            Rule rule = styleFactory.createRule();
            Filter filter = filters.next();
            rule.setSymbolizers(new Symbolizer[] {symbolizers.next()});
            rule.setFilter(filter.getGISFilter());
            rule.setName(filter.getName());
            rule.setTitle(filter.getName());
            fts.addRule(rule);     
        }

        if(filters.hasNext()) {
            throw new RuntimeException("BUG: more filters than symbolizers");
        }
        
        /* 
         * This piece of code can be added to add a stroke around the polygons.
         * This rule does not include a filter, therefore, it is globally applied.
         */
        
        /*
        StyleBuilder styleBuilder = new StyleBuilder();
        Rule rule = styleFactory.createRule();
        Stroke stroke = styleFactory.createStroke(
                styleBuilder.literalExpression(Color.BLACK),  // color
                styleBuilder.literalExpression(1),  // width
                styleBuilder.literalExpression(1.0)); // opacity
        LineSymbolizer ls = styleFactory.createLineSymbolizer(stroke, "");
        rule.setSymbolizers(new Symbolizer[] {ls});
        fts.addRule(rule);  
        */ 
        
        return fts; 
    }

    /**
     * Get the data type of the command attribute
     * @param wfs datastore
     * @return data type as Class
     */
    private Class<?> getDataType(WFSContentDataStore wfs) {
        SimpleFeatureType schema;
        Class<?> clazz = null;
        try {
            schema = wfs.getSchema(_command.getFeatureTypeName()); // get schema
            clazz = schema.getType(_command.getPropertyName()).getBinding(); // get data type as Class
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (clazz == null) {
            throw new RuntimeException("Should never happen, we need to know what type is the attribute of");
        }
        return clazz;
    }
    
    /**
     * Gives a connection to a remote WFS
     * @param wfsUrl URL of the WFS. Should be a GetCapabilities request
     * @return Virtual DataStore. All features can be extracted from it.
     * @throws DocServiceException 
     */
    @SuppressWarnings("unchecked")
    private WFSContentDataStore connectToWFS(final URL wfsUrl) throws DocServiceException {   
    	WFSContentDataStore wfs = null;
        Map m = new HashMap();
        try {
            UsernamePasswordCredentials credentials = findCredentials(wfsUrl);
            if(credentials != null) {
                m.put(WFSDataStoreFactory.USERNAME.key, credentials.getUserName());
                m.put(WFSDataStoreFactory.PASSWORD.key, credentials.getPassword());
            }
            // connect to remote WFS
            m.put(WFSDataStoreFactory.URL.key, wfsUrl);
            m.put(WFSDataStoreFactory.TIMEOUT.key, 60000); // default: 3000
            // TODO : .key necessary for those two ?
            m.put(WFSDataStoreFactory.TRY_GZIP, true); // try to optimize communication
            m.put(WFSDataStoreFactory.ENCODING, "UTF-8"); // try to force UTF-8
            // TODO : configurable ?
            m.put(WFSDataStoreFactory.MAXFEATURES.key, 2000);
            wfs = _factory.createDataStore(m);
        } 
        catch(SocketTimeoutException e) {
            throw new DocServiceException("WFS is unavailable", HttpServletResponse.SC_GATEWAY_TIMEOUT);
        }
        catch(DataSourceException e) {
            // happens when wfs url is wrong (missing request or service parameters...)
            throw new DocServiceException(e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return wfs;
    }
    
    private UsernamePasswordCredentials findCredentials(URL wfsUrl) {
        String targetHost = wfsUrl.getHost();
        String ipAddress;
        try {
            ipAddress = InetAddress.getByName(targetHost).getHostAddress();
        } catch (UnknownHostException e) {
            ipAddress = "";
        }
        for(Map.Entry<String, UsernamePasswordCredentials> cred:_credentials.entrySet()) {
            String host = cred.getKey();
            try {
                if(host.equalsIgnoreCase(targetHost) || InetAddress.getByName(host).getHostAddress().equals(ipAddress)) {
                    return cred.getValue();
                }
            } catch (UnknownHostException e) {
                continue;
            }
        }
        return null;
    }

    /**
     * Extract values as Double from the given features and property name. Executes the same job as
     * {@link SLDClassifier#getStringValues(FeatureIterator, String)} provides comparable values: useful to sort.
     * @param features Iterator to access all the Features from the WFS request
     * @param propertyName Property Name. Property from which values has to be extracted
     * @return List of Double values
     */
    private ArrayList<Double> getDoubleValues(final FeatureIterator<SimpleFeature> features, final String propertyName) {
        ArrayList<Double> values = new ArrayList<Double>();
        
        while(features.hasNext()) {
            SimpleFeature feature = features.next();
            if (feature.getProperty(_command.getPropertyName()).getValue() == null) {
            	continue;
            }
            String val = feature.getProperty(_command.getPropertyName()).getValue().toString();
            if(! val.trim().isEmpty() ) { // don't take into account attributes that are empty, it would corrupt the sld file
                values.add(Double.parseDouble(val));  
            }
        }
        
        return values;
    }
    
    /**
     * Extract values as String from the given features and property name. Executes the same job as
     * {@link SLDClassifier#getDoubleValues(FeatureIterator, String)} but it is regardless from the type.
     * Values are stored in a Set, it guarantees unique values.
     * @param features Iterator to access all the Features from the WFS request
     * @param propertyName Property Name. Property from which values has to be extracted
     * @return List of String values
     */
    private Set<String> getUniqueStringValues(final FeatureIterator<SimpleFeature> features, final String propertyName) {
        Set<String> values = new HashSet<String>();

        while(features.hasNext()) {
            SimpleFeature feature = features.next();
            String val;
            if (feature.getProperty(_command.getPropertyName()).getValue() == null) {
            	continue;
            } else {
            	val = feature.getProperty(_command.getPropertyName()).getValue().toString();
            }
            if (! val.trim().isEmpty() ) { // don't take into account attributes that are empty, it would corrupt the sld file
            	values.add(val);
            }
        }
        return values;
    }
    
    /**
     * Generate SLD file. Creates everything but the FeatureTypeStyle that must be provided.
     * @param fts FeatureTypeStyle. Contains all the rules and therefore the filters and symbolizers
     * @return StyledLayerDescriptor SLD file
     */
    private StyledLayerDescriptor createSLD(FeatureTypeStyle fts) {
        
        // create SLD
        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
        StyledLayerDescriptor sld = styleFactory.createStyledLayerDescriptor();       
        
        // add named layer
        NamedLayer layer = styleFactory.createNamedLayer();
        layer.setName(_command.getFeatureTypeName());    // name must match the layer name
        fts.setName(_command.getFeatureTypeName());
        sld.addStyledLayer(layer);
        
        // add a custom style to the user layer
        Style style = styleFactory.createStyle();
        style.setName(_command.getFeatureTypeName());
        style.setTitle(_command.getFeatureTypeName()+"_classification");
        style.addFeatureTypeStyle(fts);
        layer.addStyle(style);
        
        return sld;
    }

}
