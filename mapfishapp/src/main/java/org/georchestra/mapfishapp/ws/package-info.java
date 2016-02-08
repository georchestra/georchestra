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

/**
* Provides RESTful services to store and load files. Besides that many treatments can be done as 
* validation, formatting, or interpretation. <br /> <br />
* New services can be added. In order to do that it must inherits {@link org.georchestra.mapfishapp.ws.A_DocService} and should be named as
* {DOCTYPE}DocService. {@link org.georchestra.mapfishapp.ws.A_DocService} contains common methods for all doc services. Few methods can be overridden from it
* to adapt some specific behaviors. Then the service should be registered in {@link org.georchestra.mapfishapp.ws.DocController} to provides RESTful entry points.
* <br /> <br />
* @author yoann.buch@gmail.com
*/
package org.georchestra.mapfishapp.ws;
