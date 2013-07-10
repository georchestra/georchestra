'use strict';

/* Controllers */
angular.module('ldapadmin.controllers', [])
  .controller('GroupsCtrl', function($scope, $rootScope, Restangular) {
    Restangular.all('groups').getList().then(function(groups) {
      $rootScope.groups = groups;
    }, function errorCallback() {
      flash.error = 'Oops error from server :(';
    });
  })
  .controller('UsersCtrl', function UsersCtrl($scope, Restangular) {
    var baseUsers = Restangular.all('users');
    baseUsers.getList().then(function(users) {
      $scope.users = users;
    }, function errorCallback() {
      flash.error = 'Oops error from server :(';
    });
  })
  .controller('UsersListCtrl', function($scope, $rootScope, $routeParams) {
    //$scope.users is inherited from UsersCtrl's scope
    var group = $routeParams.group;
    $scope.groupFilter = group ? {groups: group} : null;
    $rootScope.selectedGroup = group;
  })
  .controller('UserEditCtrl', function($scope, $routeParams, Restangular, flash) {
    var user = Restangular.one('users', $routeParams.userId);
    user.get().then(function(remote) {
      $scope.user = Restangular.copy(remote);

      $scope.user_groups = angular.copy($scope.groups);
      $.each($scope.user_groups, function(index, value) {
        $scope.user_groups[index].hasUser = _.contains($scope.user.groups, value.name);
      });
      var original_groups = angular.copy($scope.user_groups);
      $scope.groupsChanged = false;

      $scope.save = function() {
        $scope.user.put().then(function() {
          remote = Restangular.copy($scope.user);
          flash.success = 'User correctly updated';
          var index = findByAttr($scope.users, 'id', $routeParams.userId);

          if (index !== false) {
            $scope.users[index] = $scope.user;
          }
        });
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
      $scope.selectGroup = function(group) {
        group.hasUser = !group.hasUser;
        // check whether the list of groups changed
        $scope.groupsChanged = !angular.equals(original_groups, $scope.user_groups);
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

$(document)
  .on('click.dropdown-menu', '.dropdown-menu.noclose > li', function (e) {
    e.stopPropagation();
  });
