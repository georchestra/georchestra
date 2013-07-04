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

function UsersListCtrl($scope, $rootScope, $routeParams) {
  //$scope.users is inherited from UsersCtrl's scope
  var group = $routeParams.group;
  $scope.groupFilter = {group: group};
  $rootScope.selectedGroup = group;
}

function UserEditCtrl($scope, $routeParams, Users) {
  $scope.user = Users.get({userId: "toto"/*$routeParams.userId*/}, function(user) {
  });
}

function UserCreateCtrl($scope, Users) {
}
