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

/* Directives */


//angular.module('ldapadmin.directives', []).
  //directive('appVersion', ['version', function(version) {
    //return function(scope, elm, attrs) {
      //elm.text(version);
    //};
  //}]);j
angular.module('ldapadmin.directives', []).
  directive('ldapadminCheckAll', function() {
    // scope should expose 'allSelected' and 'allClear'
    return function(scope, elm, attrs) {
      scope.$watch(attrs.ldapadminCheckAll, function(value) {
        elm.prop('indeterminate', value);
      }, true);
    };
  })
  .directive('groupsDropdown', function() {
    return {
      replace: true,
      templateUrl: "partials/groups_dropdown.html"
    };
  });
