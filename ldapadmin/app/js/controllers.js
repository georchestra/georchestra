'use strict';

/* Controllers */
angular.module('ldapadmin.controllers', [])
  .controller('UsersCtrl', function UsersCtrl($scope, Restangular) {
    var baseUsers = Restangular.all('users');
    $scope.users = baseUsers.getList().then(function(users) {
      users = users.sort(function(a, b) {
        if (a.name > b.name) {
          return 1;
        }
        if (a.name < b.name) {
          return -1;
        }
        return 0;
      });
      $scope.users = users;

      $scope.groups = ['Administrator', 'SV_XXX', 'SV_YYY', 'EL_XXX', 'EL_YYY'];
    }, function errorCallback() {
      flash.error = 'Oops error from server :(';
    });
  })
  .controller('UsersListCtrl', function($scope, $rootScope, $routeParams) {
    //$scope.users is inherited from UsersCtrl's scope
    var group = $routeParams.group;
    $scope.groupFilter = group ? {group: group} : null;
    $rootScope.selectedGroup = group;
  })
  .controller('UserEditCtrl', function($scope, $routeParams, Restangular, flash) {
    var user = Restangular.one('users', $routeParams.userId);
    $scope.user = user.get();

    $scope.deleteUser = function() {
      Restangular.one('users', user.id).remove().then(
        function() {
          var index = findByAttr($scope.users, 'id', $routeParams.userId);

          if (index !== false) {
            $scope.users = $scope.users.splice(index, 1);
          }
          window.history.back();
          flash.success = 'User correctly removed';
        },
        function errorCallback() {
          flash.error = 'Oops error from server :(';
        }
      );
    };
  })
  .controller('UserCreateCtrl', function($scope) {
  })
  .controller('FooCtrl', function($scope) {
    $scope.foo = "bar";
  });

function findByAttr(collection, attribute, value) {
  var i,
      len = collection.length;
  for (i = 0; i < len; i++) {
    if (collection[i][attribute] == value) {
      return i;
    }
  }
  return false;
}
