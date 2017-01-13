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

import org.geotools.styling.Symbolizer;

/**
 * Sets the contract for all classes that want to create org.geotools.styling.Symbolizer objects <br />
 * Classes must provides an iterator to access those objects. <br />
 * Useful to build SLD files from scratch
 * @author yoann.buch@gmail.com
 */

public interface I_SymbolizerFactory extends Iterable<Symbolizer> {
}
