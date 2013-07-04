'use strict';

/* Controllers */
function UsersCtrl($scope, Users) {
  var users = Users.query(null, function(users) {
    users = _.sortBy(users, function(o) {
      return o.name;
    });
    $scope.users = users;

    $scope.groups = ['Administrator', 'SV_XXX', 'SV_YYY', 'EL_XXX', 'EL_YYY'];
  });
}

function UsersListCtrl($scope, $routeParams) {
  //$scope.users is inherited from UsersCtrl's scope
  if ($routeParams.group) {
    $scope.groupFilter = {group: $routeParams.group};
  }
}

function UserEditCtrl($scope, $routeParams, Users) {
  $scope.user = Users.get({userId: "toto"/*$routeParams.userId*/}, function(user) {
  });
}

function UserCreateCtrl($scope, Users) {
}
