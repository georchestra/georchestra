/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

import org.geotools.styling.Graphic;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;

/**
 * Stores and provides PointSymbolizer objects
 * @author yoann.buch@gmail.com
 *
 */
public class PointSymbolizerFactory implements I_SymbolizerFactory {

    private String _knownSymbol = StyleBuilder.MARK_CIRCLE;
    private Color _color = Color.RED;
    private double _size = 10;
    private double _opacity = 1.0;
    private ArrayList<Integer> _sizes;
    private Iterator<Symbolizer> _iterator;
    private ArrayList<Color> _colors;

    /**
     * Iterator to access Symbolizers created by this PointSymbolizerFactory <br />
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
         * Get next PointSymbolizer
         */
        public PointSymbolizer next() {
            Mark mark = _styleBuilder.createMark(_styleBuilder.attributeExpression(_knownSymbol),
                    _styleBuilder.createFill(_color, _opacity), // color, opacity
                    null);
            Graphic graph = _styleBuilder.createGraphic(null, new Mark[] { mark }, null,
                    _styleBuilder.literalExpression(_opacity), // opacity
                    _styleBuilder.literalExpression(_it.next()), // graphic size in pixels
                    _styleBuilder.literalExpression(0)); // rotation
            return _styleBuilder.createPointSymbolizer(graph);
        }

        /**
         * Unsupported
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * Iterator to access Symbolizer objects created by this PointSymbolizerFactory <br />
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
         * Get next PointSymbolizer
         */
        public PointSymbolizer next() {
            Mark mark = _styleBuilder.createMark(_styleBuilder.attributeExpression(_knownSymbol),
                    _styleBuilder.createFill(_it.next(), _opacity), // color, opacity
                    null);
            Graphic graph = _styleBuilder.createGraphic(null, new Mark[] { mark }, null,
                    _styleBuilder.literalExpression(_opacity), // opacity
                    _styleBuilder.literalExpression(_size), // graphic size in pixels
                    _styleBuilder.literalExpression(0)); // rotation
            return _styleBuilder.createPointSymbolizer(graph);
        }

        /**
         * Unsupported
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * Interpolates sizes between an interval set by minSize and maxsize given the number of classes.
     * Those sizes will be used to create PointSymbolizer accessible via the {@link PointSymbolizerFactory#iterator()}<br />
     * classCount = number of symbols <br />
     * All symbol sizes are interpolated between minSize and maxSize. <br />
     * minSize is guaranteed, then same interval between values at the expense of the maxSize (only round values allowed:
     * sizes in pixels)
     * @param classCount Number of classes (classification context)
     * @param minSize Minimum symbol size 
     * @param maxSize Maximum symbol size
     */
    public PointSymbolizerFactory(final int classCount, final int minSize, final int maxSize) {
        
        _sizes = SymbolizerUtils.propSymbols(classCount, minSize, maxSize);
        _iterator = new SizesIterator();
    }
    
    /**
     * Interpolates colors between an interval sets by firstColor and lastColor given a number of classes.
     * Those colors will be used to create PointSymbolizer objects accessible via the {@link PointSymbolizerFactory#iterator()}
     * @param classCount Number of classes (classification context)
     * @param firstColor First color to start the interpolation
     * @param lastColor Last color to end the interpolation
     */
    public PointSymbolizerFactory(final int classCount, final Color firstColor, final Color lastColor) {
       
        _colors = SymbolizerUtils.choropleths(classCount, firstColor, lastColor);
        _iterator = new ColorsIterator();
    }
    
    /**
     * Sets colors used to create Symbolizer objects accessible then via {@link PointSymbolizerFactory#iterator()} <br />
     * Useful to assign colors when classification is made with unique values.
     * @param colorMapID known color map id (color map is a set of predefined colors)
     * @param size number of colors wanted (if color map is too small, random colors are used to complete)
     */
    public PointSymbolizerFactory(final int colorMapID, final int size) {
        
        _colors = SymbolizerUtils.uniqueValues(colorMapID, size);
        _iterator = new ColorsIterator();
    }

    /**
     * Gets the iterator to access the created PointSymbolizer objects. Its size can inferred from the number 
     * of classes passed by argument in {@link PointSymbolizerFactory#PointSymbolizerFactory(int, int, int)}.
     */
    public Iterator<Symbolizer> iterator() {
        return _iterator;
    }
    
    /**
     * Change symbols color. Default: Color.RED
     * @param color New Symbol color
     */
    public void setColor(final Color color) {
        _color = color;
    }
    
    /**
     * Change symbols size. Default: 1
     * @param size New Symbol size
     */
    public void setSize(final double size) {
        _size = size;
    }
    
    /**
     * Change symbols opacity. Default: 1.0
     * @param opacity Value between 0 and 1.0
     * @throws IllegalArgumentException If forbidden value
     */
    public void setOpacity(final double opacity) {
        if(opacity > 1.0 || opacity < 0.0) {
            throw new IllegalArgumentException();
        }
        _opacity = opacity;
    }
    
    /**
     * Change symbols shape. Default: StyleBuilder.MARK_CIRCLE
     * @param knownSymbol Must be one of the StyleBuilder MARK constants
     * @throws IllegalArgumentException If forbidden value
     */
    public void setSymbol(final String knownSymbol) {
        if(!isKnownSymbol(knownSymbol)) {
            throw new IllegalArgumentException(knownSymbol + " is not a symbol handled by this manager");
        }
        _knownSymbol = knownSymbol;
    }
    
    /**
     * Get interpolated sizes
     * @return List of sizes (in pixels)
     */
    public ArrayList<Integer> getSizes() {
        return _sizes;
    }
    
    /**
     * Get colors. Get interpolated colors if created with {@link PointSymbolizerFactory#PointSymbolizerFactory(int, Color, Color)} <br />
     * or get colors from palette if created with  {@link PointSymbolizerFactory#PointSymbolizerFactory(int, int)}
     * @return list of Color
     */
    public ArrayList<Color> getColors() {
        return _colors;
    }

    /**
     * Determines if requested symbol is a symbol shape name known by the OGC SLD specifications
     * @param knownSymbol
     * @return
     */
    private boolean isKnownSymbol(String knownSymbol) {
        // TODO : to be finished
        return true;
    }

}
