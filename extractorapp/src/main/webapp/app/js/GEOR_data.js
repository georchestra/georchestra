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

Ext.namespace("GEOR");

GEOR.data = (function() {

    /*
     * Public
     */
    return {

        /**
         * Property: layers
         * Initial layers data used as extractorapp inputs
         * can be dynamically set in index.jsp
         */
        layers: [],

        /**
         * Property: services
         * Initial services data used as extractorapp inputs
         * can be dynamically set in index.jsp
         */
        services: [],

        /**
         * Property: anonymous
         * dynamically set in index.jsp
         */
        anonymous: true,

        /**
         * Property: email
         * Email can be overriden dynamically in index.jsp
         */
        email: '',

        /**
         * Property: username
         * Username can be overriden dynamically in index.jsp
         */
        username: '',

        first_name: '',
        last_name: '',
        company: '',
        tel: '',

        /**
         * Property: debug
         * Debug can be overriden dynamically in index.jsp
         */
        debug: false,

        /**
         * Property: jettyrun
         * Boolean: are we dev/testing the standalone app with Jetty
         */
        jettyrun: false
    };

})();
