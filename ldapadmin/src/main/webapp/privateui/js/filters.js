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

'use strict';

/* Filters */

angular.module('ldapadmin.filters', []).
  filter('count', [function() {
    return function(users, group) {
      if (users) {
        var i,
            len = users.length,
            count = 0;
        for (i = 0; i < len; i++) {
          if (users[i].groups) {
            count += users[i].groups.indexOf(group) != -1 ? 1 : 0;
          }
        }
        return count;
      }
    };
  }]);
  //filter('interpolate', ['version', function(version) {
    //return function(text) {
      //return String(text).replace(/\%VERSION\%/mg, version);
    //}
  //}]);
