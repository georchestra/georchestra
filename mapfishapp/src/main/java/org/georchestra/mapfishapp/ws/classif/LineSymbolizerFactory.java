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

import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;

/**
 * Stores and provides LineSymbolizer objects
 *
 */
public class LineSymbolizerFactory implements I_SymbolizerFactory {

    private Color _color = Color.RED;
    private ArrayList<Color> _colors;
    private Iterator<Symbolizer> _iterator;
    private ArrayList<Integer> _sizes;
    
    /**
     * Iterator to access Symbolizer objects created by this LineSymbolizerFactory <br />
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
         * Get next LineSymbolizer
         */
        public LineSymbolizer next() {
            return _styleBuilder.createLineSymbolizer(_it.next());
        }

        /**
         * Unsupported
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Iterator to access Symbolizers created by this LineSymbolizerFactory <br />
     * Symbolizers are lazy created from the sizes interpolated by the factory
     */
    public class SizesIterator implements Iterator<Symbolizer> {

        private Iterator<Integer> _it;
        private StyleBuilder _styleBuilder = new StyleBuilder();
        
        public SizesIterator() {
            _it = _sizes.iterator();
        }
        
        /**
         * Checks if Iterator has still another Symbolizer
         */
        public boolean hasNext() {
            return _it.hasNext();
        }

        /**
         * Get next LineSymbolizer
         */
        public LineSymbolizer next() {
            return _styleBuilder.createLineSymbolizer(_color, _it.next());  // graphic size in pixels
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
     * Those colors will be used to create LineSymbolizer objects accessible via the {@link LineSymbolizerFactory#iterator()}
     * @param classCount Number of classes (classification context)
     * @param firstColor First color to start the interpolation
     * @param lastColor Last color to end the interpolation
     */
    public LineSymbolizerFactory(final int classCount, final Color firstColor, final Color lastColor) {
       
        _colors = SymbolizerUtils.choropleths(classCount, firstColor, lastColor);
        _iterator = new ColorsIterator();
    }
    
    /**
     * Sets colors used to create Symbolizer objects accessible then via {@link LineSymbolizerFactory#iterator()} <br />
     * Useful to assign colors when classification is made with unique values.
     * @param colorMapID known color map id (color map is a set of predefined colors)
     * @param size number of colors wanted (if color map is too small, random colors are used to complete)
     */
    public LineSymbolizerFactory(final int colorMapID, final int size) {
        
        _colors = SymbolizerUtils.uniqueValues(colorMapID, size);
        _iterator = new ColorsIterator();
    }
    
    /**
     * Interpolates sizes between an interval set by minSize and maxsize given the number of classes.
     * Those sizes will be used to create LineSymbolizer accessible via the {@link LineSymbolizerFactory#iterator()}<br />
     * classCount = number of line widths <br />
     * All line widths are interpolated between minSize and maxSize. <br />
     * minSize is guaranteed, then same interval between values at the expense of the maxSize (only round values allowed:
     * sizes in pixels)
     * @param classCount Number of classes (classification context)
     * @param minSize Minimum symbol size 
     * @param maxSize Maximum symbol size
     */
    public LineSymbolizerFactory(final int classCount, final int minSize, final int maxSize) {
        
        _sizes = SymbolizerUtils.propSymbols(classCount, minSize, maxSize);
        _iterator = new SizesIterator();
    }

    /**
     * Gets the iterator to access the created LineSymbolizer objects. Its size can inferred from the number 
     * of classes passed by argument in {@link LineSymbolizerFactory#LineSymbolizerFactory(int, Color, Color)} or
     * the size in {@link LineSymbolizerFactory#LineSymbolizerFactory(int, int)}
     */
    public Iterator<Symbolizer> iterator() {
        return _iterator;
    }
    
    /**
     * Change line color. Default: Color.RED
     * @param color New line color
     */
    public void setColor(final Color color) {
        _color = color;
    }
    
    /**
     * Get interpolated sizes
     * @return List of sizes (in pixels)
     */
    public ArrayList<Integer> getSizes() {
        return _sizes;
    }
    
    /**
     * Get colors. Get interpolated colors if created with {@link LineSymbolizerFactory#LineSymbolizerFactory(int, Color, Color)} <br />
     * or get colors from palette if created with  {@link LineSymbolizerFactory#LineSymbolizerFactory(int, int)}
     * @return list of Color
     */
    public ArrayList<Color> getColors() {
        return _colors;
    }

}
