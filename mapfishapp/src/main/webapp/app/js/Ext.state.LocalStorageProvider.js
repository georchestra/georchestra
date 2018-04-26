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

Ext.ns('Ext.state');

// Got from https://gist.github.com/malteo/1468207 on 2014-09-05
// with minor updates

/**
 * @class Ext.state.LocalStorageProvider
 * @extends Ext.state.Provider
 * A Provider implementation which saves and retrieves state via the HTML5 localStorage object.
 * If the browser does not support local storage, an exception will be thrown upon instantiating
 * this class.
 * <br />Usage:
 <pre><code>
   Ext.state.Manager.setProvider(new Ext.state.LocalStorageProvider({prefix: 'my-'}));
 </code></pre>
 * @cfg {String} prefix The application-wide prefix for the stored objects
 * @constructor
 * Create a new LocalStorageProvider
 * @param {Object} config The configuration object
 */
Ext.state.LocalStorageProvider = Ext.extend(Ext.state.Provider, {

    constructor: function (config) {
        Ext.state.LocalStorageProvider.superclass.constructor.call(this);
        Ext.apply(this, config);
        this.store = this.getStorageObject();
        this.state = this.readLocalStorage();
    },

    readLocalStorage: function () {
        var store = this.store,
            i = 0,
            len = store.length,
            prefix = this.prefix,
            prefixLen = prefix.length,
            data = {},
            key;

        for (; i < len; ++i) {
            key = store.key(i);
            if (key.substring(0, prefixLen) == prefix) {
                data[key.substr(prefixLen)] = this.decodeValue(store.getItem(key));
            }
        }

        return data;
    },

    set: function (name, value) {
        if (typeof value == "undefined" || value === null) {
            this.clear(name);
            return;
        }
        try {
            this.store.setItem(this.prefix + name, this.encodeValue(value));
            Ext.state.LocalStorageProvider.superclass.set.call(this, name, value);
        } catch (e) {
            if (e.name.toUpperCase() === 'QUOTA_EXCEEDED_ERR' || 
                e.name.toUpperCase() === 'NS_ERROR_DOM_QUOTA_REACHED') {

                alert('localStorage quota exceeded !');
            } else {
                alert('localStorage failed to store value.');
            }
        }
    },

    // private
    clear: function (name) {
        this.store.removeItem(this.prefix + name);

        Ext.state.LocalStorageProvider.superclass.clear.call(this, name);
    },

    getStorageObject: function () {
        try {
            var supports = 'localStorage' in window && window['localStorage'] !== null;
            if (supports) {
                return window.localStorage;
            }
        } catch (e) {
            return false;
        }
        alert('LocalStorage is not supported by the current browser');
    }
});