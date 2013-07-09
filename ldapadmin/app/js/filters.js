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
          count += users[i].groups.indexOf(group) != -1 ? 1 : 0;
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
