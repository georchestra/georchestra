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
