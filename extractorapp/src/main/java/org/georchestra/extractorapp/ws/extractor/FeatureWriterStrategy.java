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

package org.georchestra.extractorapp.ws.extractor;

import java.io.File;
import java.io.IOException;

/**
 * <p>
 * The class that implements this interface are responsible of writing a set of
 * features in a given format.
 * </p>
 * 
 * @author Mauricio Pazos
 *
 */
interface FeatureWriterStrategy {

    /**
     * Generates a files that maintain the vector data correspondent to a layer
     * 
     * @return the generated files
     * @throws IOException
     */
    File[] generateFiles() throws IOException;

}
