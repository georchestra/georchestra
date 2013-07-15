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

      // A copy of list of groups (w/ information on whether the user is part
      // of this group or not
      $scope.user_groups = angular.copy($scope.groups);
      $.each($scope.user_groups, function(index, value) {
        $scope.user_groups[index].hasUser = _.contains($scope.user.groups, value.name);
      });

      // Initials values. Helps to check modifications.
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

      // called when user submits modifications on groups list for a user
      $scope.apply = function() {
        var i,
            len = $scope.user_groups.length,
            toPut = [],
            toDelete = [];

        // get the list of groups to put or delete for user
        for (i=0; i < len; i++) {
          var g = $scope.user_groups[i],
              og = original_groups[i];

          if (g.hasUser != og.hasUser) {
            if (g.hasUser === true) {
              toPut.push(g.name);
            } else {
              toDelete.push(g.name);
            }
          }
        }

        var body = {
          "users": [$routeParams.userId],
          "PUT": toPut,
          "DELETE": toDelete
        };

        Restangular.all('groups_users').post(body).then(
          function() {
            // modifications made server-side. Let's update the client
            var index = findByAttr($scope.users, 'id', $routeParams.userId);

            if (index !== false) {
              // add the groups
              $scope.user.groups = _.union($scope.user.groups, toPut);
              // remove the groups
              $scope.user.groups = _.difference($scope.user.groups, toDelete);

              $scope.users[index] = $scope.user;
              // Let's modified the initial values as well. Helps to know if
              // values changed.
              original_groups = angular.copy($scope.user_groups);
            }
            flash.success = 'Modified successfully';
          },
          function errorCallback() {
            flash.error = 'Oops error from server :(';
          }
        );
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
  .on('click.dropdown-menu', '.dropdown-menu > li.noclose', function (e) {
    e.stopPropagation();
  });
