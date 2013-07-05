'use strict';

/* Controllers */
angular.module('ldapadmin.controllers', [])
  .controller('UsersCtrl', function UsersCtrl($scope, Users) {
    var users = Users.query(null, function(users) {
      users = users.sort(function(a, b) {
        return a.name > b.name;
      });
      $scope.users = users;

      $scope.groups = ['Administrator', 'SV_XXX', 'SV_YYY', 'EL_XXX', 'EL_YYY'];
    });
  })
  .controller('UsersListCtrl', function($scope, $rootScope, $routeParams) {
    //$scope.users is inherited from UsersCtrl's scope
    var group = $routeParams.group;
    $scope.groupFilter = group ? {group: group} : null;
    $rootScope.selectedGroup = group;
  })
  .controller('UserEditCtrl', function($scope, $routeParams, Users) {
    $scope.user = Users.get({userId: "toto"/*$routeParams.userId*/}, function(user) {
    });
  })
  .controller('UserCreateCtrl', function($scope, Users) {
  })
  .controller('FooCtrl', function($scope) {
    $scope.foo = "bar";
  });
