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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;

/**
 * Stores and provides PolygonSymbolizer objects
 * @author yoann.buch@gmail.com
 *
 */
public class PolygonSymbolizerFactory implements I_SymbolizerFactory {

    private ArrayList<Color> _colors;
    private ColorsIterator _iterator;
    
    /**
     * Iterator to access Symbolizer objects created by this PolygonSymbolizerFactory <br />
     * Symbolizers are lazy created from the colors interpolated by the factory
     */
    public class ColorsIterator implements Iterator<Symbolizer> {

        private Iterator<Color> _it;
        private StyleBuilder _styleBuilder = new StyleBuilder();
        
        public ColorsIterator() {
            _it = _colors.iterator();
        }
        
        /**
         * Checks if Iterator has still another Symbolizer
         */
        public boolean hasNext() {
            return _it.hasNext();
        }

        /**
         * Get next Symbolizer
         */
        public PolygonSymbolizer next() {
            return _styleBuilder.createPolygonSymbolizer(_it.next());
        }

        /**
         * Unsupported
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * Interpolates colors between an interval sets by firstColor and lastColor given a number of classes.
     * Those colors will be used to create PolygonSymbolizer objects accessible via the {@link PolygonSymbolizerFactory#iterator()}
     * @param classCount Number of classes (classification context)
     * @param firstColor First color to start the interpolation
     * @param lastColor Last color to end the interpolation
     */
    public PolygonSymbolizerFactory(final int classCount, final Color firstColor, final Color lastColor) {
       
        _colors = SymbolizerUtils.choropleths(classCount, firstColor, lastColor);
        _iterator = new ColorsIterator();
    }
    
    /**
     * Sets colors used to create Symbolizer objects accessible then via {@link PolygonSymbolizerFactory#iterator()} <br />
     * Useful to assign colors when classification is made with unique values.
     * @param colorMapID known color map id (color map is a set of predefined colors)
     * @param size number of colors wanted (if color map is too small, random colors are used to complete)
     */
    public PolygonSymbolizerFactory(final int colorMapID, final int size) {
        
        _colors = SymbolizerUtils.uniqueValues(colorMapID, size);
        _iterator = new ColorsIterator();
    }

    /**
     * Gets the iterator to access the created PolygonSymbolizer objects. Its size can inferred from the number 
     * of classes passed by argument in {@link PolygonSymbolizerFactory#PolygonSymbolizerFactory(int, Color, Color)} or
     * the size in {@link PolygonSymbolizerFactory#PolygonSymbolizerFactory(int, int)}
     */
    public Iterator<Symbolizer> iterator() {
        return _iterator;
    }
    
    /**
     * Get colors. Get interpolated colors if created with {@link PolygonSymbolizerFactory#PolygonSymbolizerFactory(int, Color, Color)} <br />
     * or get colors from palette if created with  {@link PolygonSymbolizerFactory#PolygonSymbolizerFactory(int, int)}
     * @return list of Color
     */
    public ArrayList<Color> getColors() {
        return _colors;
    }

}
