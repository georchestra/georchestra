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

Ext.namespace("GEOR");

GEOR.nav = (function() {

    /*
     * Private
     */
    var tr = OpenLayers.i18n;
    var ok;
    var startPosition = 1;
    var numberOfRecordsMatched;
    var RESULTSPERPAGE = GEOR.config.RESULTS_PER_PAGE;

    var isMax = function() {
        return (startPosition == Math.floor((numberOfRecordsMatched - 1)/RESULTSPERPAGE) * RESULTSPERPAGE + 1);
    };

    var handleNavigation = function(store, bar) {
        numberOfRecordsMatched = store.getTotalCount();

        var max = isMax() ? numberOfRecordsMatched : parseInt(startPosition + RESULTSPERPAGE - 1);

        if (bar.items.length === 0) {
            bar.add({
                text: '<<',
                ref: 'first',
                disabled: true,
                handler: GEOR.nav.begin,
                tooltip: tr("go to first results"),
                width: 30
            },{
                text: '<',
                ref: 'previous',
                disabled: true,
                handler: GEOR.nav.previousPage,
                tooltip: tr("previous page"),
                width: 30
            },{
                text: '>',
                ref: 'next',
                handler: GEOR.nav.nextPage,
                tooltip: tr("next page"),
                width: 30
            },{
                text: '>>',
                ref: 'end',
                handler: GEOR.nav.end,
                tooltip: tr("go to last results"),
                width: 30
            }, new Ext.Toolbar.TextItem({
                text: '',
                ref: 'navText'
            }), '->', new Ext.Toolbar.TextItem({
                text: '',
                ref: 'selText'
            }));
            bar.ownerCt.doLayout();
        }

        if (numberOfRecordsMatched == 0) {
            bar.first.disable();
            bar.previous.disable();
            bar.next.disable();
            bar.end.disable();
            bar.navText.setText(tr("No result"));
        } else {
            if (numberOfRecordsMatched > 1) {
                bar.navText.setText(tr("Results N1 to N2 of N", {
                    'N1': startPosition,
                    'N2': max,
                    'N': numberOfRecordsMatched
                }));
            } else {
                bar.navText.setText(tr("Result N1 to N2 of N", {
                    'N1': startPosition,
                    'N2': max,
                    'N': numberOfRecordsMatched
                }));
            }
            // handle buttons activation/deactivation:
            bar.first.setDisabled(startPosition == 1);
            bar.previous.setDisabled(startPosition == 1);
            bar.next.setDisabled(isMax());
            bar.end.setDisabled(isMax());
        }
    };

    /*
     * Public
     */
    return {

        getParameters: function(options) {
            return {
                "startPosition": startPosition,
                "maxResults": startPosition + RESULTSPERPAGE - 1
            };
        },

        update: handleNavigation,

        reset: function() {
            startPosition = 1;
        },

        nextPage: function() {
            if (isMax()) {
                return;
            }
            startPosition += RESULTSPERPAGE;
            GEOR.observable.fireEvent("searchrequest");
        },

        previousPage: function() {
            if (startPosition == 1) {
                return;
            }
            startPosition -= RESULTSPERPAGE;
            GEOR.observable.fireEvent("searchrequest");
        },

        begin: function() {
            if (startPosition == 1) {
                return;
            }
            startPosition = 1;
            GEOR.observable.fireEvent("searchrequest");
        },

        end: function() {
            if (isMax()) {
                return;
            }
            startPosition = Math.floor((numberOfRecordsMatched - 1)/RESULTSPERPAGE) * RESULTSPERPAGE + 1;
            GEOR.observable.fireEvent("searchrequest");
        }
    };
})();