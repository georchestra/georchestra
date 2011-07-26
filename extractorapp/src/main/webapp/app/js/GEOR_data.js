/*
 * Copyright (C) 2009  Camptocamp
 *
 * This file is part of geOrchestra
 *
 * MapFish Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

Ext.namespace("GEOR");

GEOR.data = (function() {

    /*
     * Public
     */
    return {

        /**
         * Property: layers
         * Initial layers data used as extractorapp inputs
         * can be dynamically in index.jsp
         */
        layers: [],

        /**
         * Property: services
         * Initial services data used as extractorapp inputs
         * can be dynamically in index.jsp
         */
        services: [],

        /**
         * Property: anonymous
         * dynamically in index.jsp
         */
        anonymous: true,
        
        /**
         * Property: email
         * Email can be overriden dynamically in index.jsp
         */
        email: null,
        
        /**
         * Property: username
         * Username can be overriden dynamically in index.jsp
         */
        username: null,
        
        /**
         * Property: debug
         * Debug can be overriden dynamically in index.jsp
         */
        debug: null
    };
    
})();
