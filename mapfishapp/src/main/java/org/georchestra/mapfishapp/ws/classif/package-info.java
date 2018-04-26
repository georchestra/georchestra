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

/**
* Provides classes to classify data coming from WFS and generate results as SLD files.
* <br /> <br />
* {@link org.georchestra.mapfishapp.ws.classif.SLDClassifier} with {@link org.georchestra.mapfishapp.ws.classif.ClassifierCommand} have to be used
* to classify set of data coming from a WFS. <br />
* {@link org.georchestra.mapfishapp.ws.classif.PointSymbolizerFactory}, {@link org.georchestra.mapfishapp.ws.classif.PolygonSymbolizerFactory} are used
* for presentation purpose (SLD Symbolizers). <br />
* {@link org.georchestra.mapfishapp.ws.classif.DiscreteFilterFactory}, {@link org.georchestra.mapfishapp.ws.classif.ContinuousFilterFactory} are used
* for filtering purpose (SLD filters)
* <br /> <br />
* @author yoann.buch@gmail.com
*/
package org.georchestra.mapfishapp.ws.classif;
