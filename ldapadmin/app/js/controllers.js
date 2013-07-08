'use strict';

/* Controllers */
angular.module('ldapadmin.controllers', [])
  .controller('UsersCtrl', function UsersCtrl($scope, Restangular) {
    var baseUsers = Restangular.all('users');
    baseUsers.getList().then(function(users) {
      //users = users.sort(function(a, b) {
        //if (a.name.toLowerCase() > b.name.toLowerCase()) {
          //return 1;
        //}
        //if (a.name.toLowerCase() < b.name.toLowerCase()) {
          //return -1;
        //}
        //return 0;
      //});
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
    user.get().then(function(remote) {
      $scope.user = Restangular.copy(remote);

      $scope.save = function() {
        Restangular.one('users', user.id).put();
      };
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
      $scope.isClean = function() {
        return angular.equals(remote, $scope.user);
      };
    });
  })
  .controller('UserCreateCtrl', function($scope, Restangular, flash) {
      $scope.save = function() {
        Restangular.all('users').post({
          name: $scope.user.name,
          email: $scope.user.email
        }).then(function(user) {
          $scope.users.push(user);
          window.location = "#/users";
          flash.success = 'User correctly added';
        });
      };
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
