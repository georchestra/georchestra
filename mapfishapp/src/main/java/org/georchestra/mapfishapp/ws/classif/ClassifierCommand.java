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

package org.georchestra.mapfishapp.ws.classif;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletResponse;


import org.georchestra.mapfishapp.ws.DocServiceException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Command used to encapsulate a classification request. 
 * @author yoann.buch@gmail.com
 *
 */
public class ClassifierCommand {

    /**
     * All following members are designed to map names used by the client side and the java code
     */
    private static final String JSON_WFSURL             = "wfs_url";
    private static final String JSON_FEATURETYPE        = "layer_name";
    private static final String JSON_PROPERTY           = "attribute_name";
    private static final String JSON_CLASSIFICATIONTYPE = "type";
    private static final String JSON_SYMBOLTYPE         = "symbol_type";
    private static final String JSON_FIRSTCOLOR         = "first_color";
    private static final String JSON_LASTCOLOR          = "last_color";
    private static final String JSON_MINSIZE            = "min_size";
    private static final String JSON_MAXSIZE            = "max_size";
    private static final String JSON_CLASSCOUNT         = "class_count";
    private static final String JSON_PALETTEID          = "palette";
    
    /**
     * Type of classification
     */
    public enum E_ClassifType {CHOROPLETHS, PROP_SYMBOLS, UNIQUE_VALUES};
    public enum E_SymbolType {POINT, LINE, POLYGON};
    
    private URL _wfsUrl;
    private String _featureTypeName;
    private String _propertyName;
    private E_ClassifType _classifType;
    private E_SymbolType _symbolType;
    
    private Color _firstColor;
    private Color _lastColor;
    private int _minSize;
    private int _maxSize;
    private int _classCount;
    private int _paletteID;
    
    /**
     * Create command from a String.
     * @param jsonRequest This string must be in JSON format
     * @throws DocServiceException When client request is not valid
     */
    public ClassifierCommand(final String jsonRequest) throws DocServiceException  {    
        try {
            JSONTokener tokener = new JSONTokener(jsonRequest);
            JSONObject jObj = new JSONObject(tokener);
            interpretJSON(jObj);
        } catch (JSONException e) {
            throw new DocServiceException(e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Create command from a JSONObject
     * @param jsonRequest JSONObject, the request
     * @throws DocServiceException When request is not valid
     */
    public ClassifierCommand(final JSONObject jsonRequest) throws DocServiceException {
        interpretJSON(jsonRequest);
    }
    
    /**
     * Interpret the request in JSON. Check JSON format, required fields and store them in member attributes.
     * @param jsonRequest JSONObject, the request
     * @throws DocServiceException When request is not valid
     */
    private void interpretJSON(final JSONObject jsonRequest) throws DocServiceException {
        try {
            _wfsUrl = new URL(URLDecoder.decode(jsonRequest.getString(JSON_WFSURL)));
            _featureTypeName = jsonRequest.getString(JSON_FEATURETYPE);
            _propertyName = jsonRequest.getString(JSON_PROPERTY);
            
            // get the classification type
            // if unknown throw exception
            String type = jsonRequest.getString(JSON_CLASSIFICATIONTYPE);
            setClassifType(type);
            
            // get the symbol type
            // if unknown throw exception
            String symbol = jsonRequest.getString(JSON_SYMBOLTYPE);
            setSymbolType(symbol);
            
            // get specific parameters given the requested classification type
            if(_classifType == E_ClassifType.CHOROPLETHS) {
                try {
                    _firstColor = Color.decode(jsonRequest.getString(JSON_FIRSTCOLOR));
                    _lastColor = Color.decode(jsonRequest.getString(JSON_LASTCOLOR));
                }
                catch(NumberFormatException e) {
                    throw new DocServiceException("Colors should be given in hex format", HttpServletResponse.SC_BAD_REQUEST);
                }
                _classCount = jsonRequest.getInt(JSON_CLASSCOUNT);
                if(_classCount <= 0) {
                    throw new DocServiceException("Number of classes cannot negative", HttpServletResponse.SC_BAD_REQUEST);
                }
            }
            else if (_classifType == E_ClassifType.PROP_SYMBOLS) {
                _minSize = jsonRequest.getInt(JSON_MINSIZE);
                _maxSize = jsonRequest.getInt(JSON_MAXSIZE);
                _classCount = jsonRequest.getInt(JSON_CLASSCOUNT);
                
                if(_classCount <= 0) {
                    throw new DocServiceException("Number of classes cannot negative", HttpServletResponse.SC_BAD_REQUEST);
                }
                if(_minSize < 0 || _maxSize < 0) {
                    throw new DocServiceException("Sizes cannot be negative", HttpServletResponse.SC_BAD_REQUEST);
                }
                if(_maxSize <= _minSize) {
                    throw new DocServiceException("Maximum size cannot be greater or equal than minimum size", HttpServletResponse.SC_BAD_REQUEST);
                }
            }
            else if (_classifType == E_ClassifType.UNIQUE_VALUES) {
                _paletteID = jsonRequest.getInt(JSON_PALETTEID);
            }      
        }
        catch (MalformedURLException e) {
            throw new DocServiceException(e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } 
        catch (JSONException e) {
            throw new DocServiceException(e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    /**
     * Determine if the type requested by the user is known
     * @param type type requested by the customer
     * @return true: known; false: unknown, consider the command corrupted
     * @throws DocServiceException 
     */
    private void setClassifType(final String type) throws DocServiceException {
        
        for (E_ClassifType e_type : E_ClassifType.values()) {
            if(e_type.toString().toLowerCase().equals(type.toLowerCase())) {
                _classifType = e_type;
                break;
            }
        }
        if (_classifType == null) {
            throw new DocServiceException("Unknown classification type:" + type, HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    /**
     * Determine if the type requested by the user is known
     * @param type type requested by the customer
     * @return true: known; false: unknown, consider the command corrupted
     * @throws DocServiceException 
     */
    private void setSymbolType(final String type) throws DocServiceException {
        
        for (E_SymbolType e_type : E_SymbolType.values()) {
            if(e_type.toString().toLowerCase().equals(type.toLowerCase())) {
                _symbolType = e_type;
                break;
            }
        }
        if (_symbolType == null) {
            throw new DocServiceException("Unknown symbol type:" + type, HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Get the URL of the WFS
     * @return URL
     */
    public URL getWFSUrl() {
        return _wfsUrl;
    }
    
    /**
     * Get the name of the feature type. A feature type is one element from the list of the WFS method GetCapabilities.
     * @return String feature type name
     */
    public String getFeatureTypeName() {
        return _featureTypeName;
    }
    
    /**
     * Get the name of the property. The WFS method: GetFeature returns a collection of features. In each features there
     * is a list of properties or attributes.
     * @return property name
     */
    public String getPropertyName() {
        return _propertyName;
    }
    
    /**
     * Get the classification type
     * @return E_Type, classification type
     */
    public E_ClassifType getClassifType() {
        return _classifType;
    }
    
    /**
     * Get the symbol type
     * @return E_SymbolType, symbol type
     */
    public E_SymbolType getSymbolType() {
        return _symbolType;
    }
    
    /**
     * Get the first color of the interpolation. Should only be used when Command was filled with CHOROPLETHS type.
     * @return String Color HEX
     */
    public Color getFirstColor() {
        if(_classifType != E_ClassifType.CHOROPLETHS) {
            throw new RuntimeException("First Color is only available with CHOROPLETHS type");
        }
        return _firstColor;
    }
    
    /**
     * Get the last color of the interpolation. Should only be used when Command was filled with CHOROPLETHS type.
     * @return String Color HEX
     */
    public Color getLastColor() {
        if(_classifType != E_ClassifType.CHOROPLETHS) {
            throw new RuntimeException("Last Color is only available with CHOROPLETHS type");
        }
        return _lastColor;
    }
    
    /**
     * Get the start size of the interpolation. Should only be used when Command was filled with PROP_SYMBOLS type.
     * @return size in pixel
     */
    public int getMinSize() {
        if(_classifType != E_ClassifType.PROP_SYMBOLS) {
            throw new RuntimeException("Min size is only available with PROP_SYMBOLS type");
        }
        return _minSize;
    }
    
    /**
     * Get the end size of the interpolation. Should only be used when Command was filled with PROP_SYMBOLS type.
     * @return size in pixel
     */
    public int getMaxSize() {
        if(_classifType != E_ClassifType.PROP_SYMBOLS) {
            throw new RuntimeException("Max size is only available with PROP_SYMBOLS type");
        }
        return _maxSize;
    }

    /**
     * Get the number of classes. Should only be used when Command was filled with anything but UNIQUE_VALUES type.
     * @return classes count
     */
    public int getClassCount() {
        if(_classifType == E_ClassifType.UNIQUE_VALUES) {
            throw new RuntimeException("No classes are necesseray to classify in UNIQUE_VALUES mode");
        }
        return _classCount;
    }

    /**
     * Get the palette id. Should only be used when Command was filled with PROP_SYMBOLS type. <br />
     * This id corresponds to a specific palette known on server side.
     * @return palette id.
     */
    public int getPaletteID() {
        if(_classifType != E_ClassifType.UNIQUE_VALUES) {
            throw new RuntimeException("Palette id is only available with UNIQUE_VALUES type");
        }
        return _paletteID;
    }

	public void setFeatureTypeName(String ftName) {
		this._featureTypeName = ftName;
		
	}
    
    
}
