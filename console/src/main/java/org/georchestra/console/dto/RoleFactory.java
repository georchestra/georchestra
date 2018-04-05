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

package org.georchestra.console.dto;

/**
 * This factory creates instance of {@link Role}.
 * 
 * 
 * @author Mauricio Pazos
 *
 */
public class RoleFactory {

	private RoleFactory(){}
	
	
	public static Role create() {
		
		return new RoleImpl();
	}

	public static Role create(String commonName, String description, Boolean isFavorite) {
		
		Role g = new RoleImpl();
		
		g.setName(commonName);
		g.setDescription(description);
		g.setFavorite(isFavorite);
		
		return g;
	}

}
