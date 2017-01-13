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

/*
 * Inspired from http://dev.sencha.com/deploy/ext-3.4.0/examples/shared/examples.js
 * which is copyright 2006-2011 Sencha Inc.
 */

/*
 * @include GEOR_localStorage.js
 */

Ext.namespace("GEOR");

GEOR.helper = function(){

    var DEFAULT_DURATION = 3;

    var msgCt;

    var createBox = function(t, s) {
        return [
            '<div class="help">',
            '<div class="x-box-tl"><div class="x-box-tr"><div class="x-box-tc"></div></div></div>',
            '<div class="x-box-ml"><div class="x-box-mr"><div class="x-box-mc"><h3>', t, '</h3>', s, '</div></div></div>',
            '<div class="x-box-bl"><div class="x-box-br"><div class="x-box-bc"></div></div></div>',
            '</div>'
        ].join('');
    };

    return {
        msg: function(title, text, duration) {
            if (GEOR.ls.get("no_contextual_help")) {
                return;
            }
            if (!msgCt){
                msgCt = Ext.DomHelper.insertFirst(document.body, {
                    id:'help-div'
                }, true);
            }
            msgCt.alignTo(document, 't-t');
            var m = Ext.DomHelper.append(msgCt, {
                html: createBox(title, text)
            }, true);
            m.slideIn('t').pause(duration || DEFAULT_DURATION).ghost("t", {
                remove: true
            });
        }
    };
}();