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
import java.util.Random;

public class SymbolizerUtils {

    /**
     * Interpolates colors between an interval sets by firstColor and lastColor given a number of classes.
     * Those colors will be used to create PolygonSymbolizer objects accessible via the {@link PolygonSymbolizerFactory#iterator()}
     * @param classCount Number of classes (classification context)
     * @param firstColor First color to start the interpolation
     * @param lastColor Last color to end the interpolation
     */
    public static ArrayList<Color> choropleths(final int classCount, final Color firstColor, final Color lastColor) {
       
        // RGB interpolation
        ArrayList<Color> colors = new ArrayList<Color>();
        
        double redStepSize = (lastColor.getRed() - firstColor.getRed()) / (double)(classCount - 1);
        double greenStepSize = (lastColor.getGreen() - firstColor.getGreen()) / (double)(classCount - 1);
        double blueStepSize = (lastColor.getBlue() - firstColor.getBlue()) / (double)(classCount - 1);
        
        for(int currentClass = 0; currentClass < classCount; currentClass++) {
            Color interpolatedColor = new Color(
                    (int) (firstColor.getRed() + redStepSize * currentClass),
                    (int) (firstColor.getGreen() + greenStepSize * currentClass),
                    (int) (firstColor.getBlue() + blueStepSize * currentClass)
                    );    
            colors.add(interpolatedColor);
        }
        
        return colors;
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
     * @return 
     */
    public static ArrayList<Integer> propSymbols(final int classCount, final int minSize, final int maxSize) {
        
        ArrayList<Integer> sizes = new ArrayList<Integer>();
        
        // check if parameters are valid
        if(classCount <= 0) {
            throw new IllegalArgumentException("class count can't be negative or equals to 0");
        }
        if(minSize < 0 || maxSize < 0) {
            throw new IllegalArgumentException("sizes can't be negative");
        }
        if(maxSize <= minSize) {
            throw new IllegalArgumentException("max size can't be greater or equal than min size");
        }

        // special case: just one size
        if(classCount == 1) {
            sizes.add(minSize);
            return sizes;
        }
        
        // store all interpolated sizes. One size for one symbol
        int sizeStep = (maxSize - minSize) / (classCount - 1);
        for(int currentClass = 0; currentClass < classCount; currentClass++) {
            int size = minSize + sizeStep * currentClass;
            sizes.add(size);
        }
        
        return sizes;
    }
    
    /**
     * Sets colors used to create Symbolizer objects accessible then via {@link PolygonSymbolizerFactory#iterator()} <br />
     * Useful to assign colors when classification is made with unique values.
     * @param colorMapID known color map id (color map is a set of predefined colors)
     * @param size number of colors wanted (if color map is too small, random colors are used to complete)
     * @return 
     */
    public static ArrayList<Color> uniqueValues(final int colorMapID, final int size) {
        
        ArrayList<Color> colors = new ArrayList<Color>();
        
        colors.addAll(getPalette(colorMapID, size));
        
        // add more colors if needed
        while(colors.size() < size) {
            colors.add(getRandomColor());
        }
        
        return colors;
    }
    
    /**
     * Get a predefined palette
     * @param id palette id
     * @return <code>ArrayList<Color></code> or IllegalArgumentException if not found
     */
    private static ArrayList<Color> getPalette(int id, int neededSize) {

        // list of predefined palette
        int[][][] palettes = new int[][][] {
            {{141,211,199},{255,255,179},{190,186,218},{251,128,114},{128,177,211},{253,180,98},
                {179,222,105},{252,205,229},{217,217,217},{188,128,189},{204,235,197},{255,237,111}},

            {{166,206,227},{31,120,180},{178,223,138},{51,160,44},{251,154,153},{227,26,28},
                {253,191,111},{255,127,0},{202,178,214},{106,61,154},{255,255,153}},

            {{251,180,174},{179,205,227},{204,235,197},{222,203,228},{254,217,166},{255,255,204},
                {229,216,189},{253,218,236},{242,242,242}},

            {{228,26,28},{55,126,184},{77,175,74},{152,78,163},{255,127,0},{255,255,51},{166,86,40},
                {247,129,191},{153,153,153}},

            {{179,226,205},{253,205,172},{203,213,232},{244,202,228},{230,245,201},{255,242,174},
                {241,226,204},{204,204,204}},

            {{102,194,165},{252,194,165},{141,160,203},{231,138,195},{166,216,84},{255,217,47},
                {229,196,148},{179,179,179}},

            {{27,158,119},{217,95,2},{117,112,179},{231,41,138},{102,166,30},{230,171,2},
                {166,118,29},{102,102,102}},

            {{127,201,127},{190,174,212},{253,192,134},{255,255,153},{56,108,176},{240,2,127},
                {191,91,23},{102,102,102}}
        };  
        
        // try to get the palette requested by user
        int[][] rgbPalette;
        try {
            rgbPalette = palettes[id]; 
        }
        catch(IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Unknown palette id: " + id);
        }
        
        // build palette with Color objects
        ArrayList<Color> palette = new ArrayList<Color>();
        int i = 0;
        for(int[] color : rgbPalette) {
            palette.add(new Color(color[0], color[1], color[2]));
            i++;
            if(i >= neededSize) {
                break;
            } 
        }
       
        return palette;  
    }
    
    /**
     * Gets random RGB color
     * @return Color, random color
     */
    private static Color getRandomColor() {
        Random randomGenerator = new Random();
        int r = randomGenerator.nextInt(256);
        int g = randomGenerator.nextInt(256);
        int b = randomGenerator.nextInt(256);
        return new Color(r, g, b);
    }
}
