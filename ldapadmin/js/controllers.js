'use strict';

/* Controllers */
function UsersCtrl($scope, Users) {
  var users = Users.query(null, function(users) {
    users = _.sortBy(users, function(o) {
      return o.name;
    });
    $scope.users = users;
  });
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
