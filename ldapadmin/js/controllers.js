'use strict';

/* Controllers */
function UsersCtrl($scope, Users) {
  $scope.users = Users.query();
}

function UsersListCtrl($scope) {
  //$scope.users is inherited from UsersCtrl's scope
}

function UserEditCtrl($scope, $routeParams, Users) {
  $scope.user = Users.get({userId: "toto"/*$routeParams.userId*/}, function(user) {
  });
}

function UserCreateCtrl($scope, Users) {
}
