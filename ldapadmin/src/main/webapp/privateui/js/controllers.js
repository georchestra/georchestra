/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

'use strict';

/* Controllers */
angular.module('ldapadmin.controllers', [])
  .controller('GroupsCtrl', function($scope, $rootScope, Restangular) {
    $rootScope.groups = [];
    Restangular.all('groups').getList().then(function(groups) {
      $rootScope.groups = groups;

      var tree = [];
      var prefix;
      angular.forEach($rootScope.groups, function(group, key) {
        addNode(tree, group);
      });
      $rootScope.groups_tree = tree;
    }, function errorCallback() {
      flash.error = 'Oops error from server :(';
    });
  })
  /**
   * Group Edit
   */
  .controller('GroupEditCtrl', function($scope, $routeParams, Restangular, flash) {
    var group = Restangular.one('groups', $routeParams.group);
    group.get().then(function(remote) {
      $scope.group = Restangular.copy(remote);

      // manually add an id field so that we can use Restangular without
      // changing the mapping to id field globally
      $scope.group.id = $scope.group.cn;

      $scope.save = function() {
        $scope.group.put().then(function() {
          flash.success = 'Group correctly updated';
          var index = findByAttr($scope.groups, 'cn', $routeParams.group);

          if (index !== false) {
            $scope.groups[index].cn = $scope.group.cn;
            remote = angular.copy($scope.group);
            window.location = '#/groups/' + $scope.group.cn + '/edit';
          }
        });
      };
      $scope.isClean = function() {
        return angular.equals(remote, $scope.group);
      };
      $scope.cancel = function() {
        window.location = '#/groups/' + $scope.group.cn;
      };
    });
  })
  /**
   * Group Create
   */
  .controller('GroupCreateCtrl', function($scope, Restangular, flash) {
      $scope.save = function() {
        Restangular.all('groups').post(
          $scope.group
        ).then(function(group) {
          $scope.groups.push(group);

          // update groups tree
          addNode($scope.groups_tree, group);

          window.location = "#/users";
          flash.success = 'Group correctly added';
        }, function errorCallback() {
            flash.error = 'Error creating the group :(';
        });
      };
  })
  .controller('UsersCtrl', function UsersCtrl($scope, Restangular) {
    var baseUsers = Restangular.all('users');
    baseUsers.getList().then(function(users) {
      $scope.users = users;
    }, function errorCallback() {
      flash.error = 'Oops error from server :(';
    });
  })

  /**
   * Users List
   */
  .controller('UsersListCtrl', function($scope, $rootScope, $routeParams, $filter, Restangular, flash) {
    var group;

    function selectGroup() {
      //$scope.users is inherited from UsersCtrl's scope
      if ($routeParams.group == 'none') {
        var groups = [
          $scope.groups[findByAttr($scope.groups, 'cn', 'ADMINISTRATOR')],
          $scope.groups[findByAttr($scope.groups, 'cn', 'SV_ADMIN')],
          $scope.groups[findByAttr($scope.groups, 'cn', 'SV_REVIEWER')],
          $scope.groups[findByAttr($scope.groups, 'cn', 'SV_EDITOR')],
          $scope.groups[findByAttr($scope.groups, 'cn', 'SV_USER')]
        ];
        $scope.groupFilter = function(item) {
          return groups[0].users.indexOf(item.uid) == -1 &&
            groups[1].users.indexOf(item.uid) == -1 &&
            groups[2].users.indexOf(item.uid) == -1 &&
            groups[3].users.indexOf(item.uid) == -1 &&
            groups[4].users.indexOf(item.uid) == -1;
        };
        $rootScope.selectedGroup = "none";
        $scope.allSelected = false;
      } else {
        var index = findByAttr($scope.groups, 'cn', $routeParams.group);
        group = $scope.groups[index];
        $scope.groupFilter = function(item) {
          if (group) {
            return group.users && group.users.indexOf(item.uid) != -1;
          } else {
            return true;
          }
        };
        $rootScope.selectedGroup = group;
        $scope.allSelected = false;
      }
    }

    // wait for groups to be loaded from service, prevents race condition
    $scope.$watch('groups', function() {
      selectGroup();
    });

    $scope.selectedUsers = function() {
      return _.filter($scope.users, function(user) {
        return user.selected === true &&
          (!group || group && group.users && group.users.indexOf(user.uid) != -1);
      });
    };

    function filteredUsers() {
      return _.filter($scope.users, function(user) {
        return !group || group.users && group.users.indexOf(user.uid) != -1;
      });
    }

    $scope.$watch('users', function() {
      var filtered = filteredUsers(),
          selected = $scope.selectedUsers();

      $scope.allSelected = filtered && selected &&
        selected.length == filtered.length &&
        filtered.length > 0;
    }, true);

    $scope.selectAll = function() {
      angular.forEach(filteredUsers(), function(user) {
        user.selected = $scope.allSelected;
      });
    };

    function hasUsers(group) {
      var total = $scope.selectedUsers().length;
      var uids = _.pluck($scope.selectedUsers(), 'uid');
      var inGroup = _.difference(uids, group.users);
      if (inGroup.length === total) {
        return false;
      }
      return inGroup.length === 0 ? 'all' : 'some';
    }
    $scope.selectGroup = function(group) {
      if (!group.hasUsers || group.hasUsers == 'some') {
        group.hasUsers = 'all';
      } else if (group.hasUsers == 'all') {
        group.hasUsers = false;
      }
      // check whether the list of groups changed
      $scope.groupsChanged = !angular.equals($scope.original_groups, $scope.user_groups);
    };

    // we wan't to initialize groups when the groups button is clicked
    $scope.initGroups = function() {
      // A copy of list of groups (w/ information on whether the user is part
      // of this group or not
      $scope.user_groups = angular.copy($scope.groups);

      var tree = [];
      var prefix;
      angular.forEach($scope.user_groups, function(group, key) {
        addNode(tree, group);
      });
      $scope.user_groups_tree = tree;

      angular.forEach($scope.user_groups, function(group, key) {
        group.hasUsers = hasUsers(group);
      });

      $scope.original_groups = angular.copy($scope.user_groups);

      $scope.groupsChanged = false;
    };

    // called when user submits modifications on groups list for a user
    $scope.apply = function() {
      postGroups($scope, $scope.selectedUsers(), Restangular, flash);
    };
    $scope.deleteGroup = function(group) {

      if(group == GEOR_config.virtualTemporaryGroupName) {
        alert("This group cannot be deleted because it's a virtual group !");
        return;
      }

      if (confirm('Do you really want to remove group "' + group + '"?')) {
        Restangular.one('groups', group).remove().then(function() {
          var index = findByAttr($scope.groups, 'cn', $routeParams.group);

          if (index !== false) {
            $scope.groups = $scope.groups.splice(index, 1);
            removeNode($scope.groups_tree, group);
          }
          window.location = '#/users';
          flash.success = 'Group correctly removed';
        }, function errorCallback() {
          flash.error = 'Error removing the group';
        });
      }
    };

    $scope.exportAsCsv = function() {
      var uids = _.pluck($scope.selectedUsers(), 'uid');
      var url = GEOR_config.publicContextPath + "/private/users.csv";
  
      var form = $('<form> </form>').attr("action", url).attr("method", 'POST').attr("id", '#exportAsCsvForm');
      var input = $('<input >').attr("name", 'users');
      
      form.append(input).appendTo('body');
      form.find("input[name=users]").val(JSON.stringify(uids));
      form.submit();
      form.remove();
    }

    $scope.exportAsVcard = function() {
      var uids = _.pluck($scope.selectedUsers(), 'uid');
      var url = GEOR_config.publicContextPath + "/private/users.vcf";

      var form = $('<form> </form>').attr("action", url).attr("method", 'POST').attr("id", '#exportAsVcfForm');
      var input = $('<input >').attr("name", 'users');

      form.append(input).appendTo('body');
      form.find("input[name=users]").val(JSON.stringify(uids));
      form.submit();
      form.remove();
    }
  })

  /**
   * User Edit
   */
  .controller('UserEditCtrl', function($scope, $routeParams, Restangular, flash) {
    var user = Restangular.one('users', $routeParams.userId);
    user.get().then(function(remote) {

      // manually add an id field so that we can use Restangular without
      // changing the mapping to id field globally
      remote.id = remote.uid;

      $scope.user = Restangular.copy(remote);

      $scope.groupsChanged = false;
      $scope.publicContextPath = GEOR_config.publicContextPath;

      $scope.save = function() {
        $scope.user.put().then(function() {
          flash.success = 'User correctly updated';
          var prevUserId = $routeParams.userId;
          var newUserId = $scope.user.uid;
          var index = findByAttr($scope.users, 'uid', prevUserId);

          if (index !== false) {
            $scope.users[index] = angular.copy($scope.user);
            remote = angular.copy($scope.user);

            // uid modified
            if (newUserId != prevUserId) {
              window.location = '#/users/' + newUserId;

              // Update the groups the user belongs to
              var i,
                  len = $scope.groups.length;
              for (i=0; i < len; i++) {
                var index2 = _.indexOf($scope.groups[i].users, prevUserId);
                if (index2 != -1) {
                  $scope.groups[i].users[index2] = newUserId;
                }
              }
            }
          }
        }, function(args) {
          flash.error = 'User could not be updated';
          if (args.data && args.data.error == 'duplicated_email') {
              flash.error = 'User could not be updated - email already in use';
          } else {
              flash.error = 'User could not be updated';
          }
        });
      };
      $scope.deleteUser = function() {
        if (confirm('Do you really want to remove this user?')) {
          Restangular.one('users', $scope.user.uid).remove().then(
            function() {
              var index = findByAttr($scope.users, 'uid', $routeParams.userId);

              if (index !== false) {
                $scope.users = $scope.users.splice(index, 1);
              }

              // Remove from all groups
              var i,
                  len = $scope.groups.length;

              for (i=0; i < len; i++) {
                $scope.groups[i].users = _.without($scope.groups[i].users, $routeParams.userId);
              }

              window.history.back();
              flash.success = 'User correctly removed';
            },
            function errorCallback() {
              flash.error = 'Oops error from server :(';
            }
          );
        }
      };
      $scope.isClean = function() {
        return angular.equals(remote, $scope.user);
      };
      $scope.selectGroup = function(group) {
        group.hasUsers = !group.hasUsers;
        // check whether the list of groups changed
        $scope.groupsChanged = !angular.equals($scope.original_groups, $scope.user_groups);
      };
      // we wan't to initialize groups when the groups button is clicked
      $scope.initGroups = function() {
        // A copy of list of groups (w/ information on whether the user is part
        // of this group or not
        $scope.user_groups = angular.copy($scope.groups);

        var tree = [];
        var prefix;
        angular.forEach($scope.user_groups, function(group, key) {
          addNode(tree, group);
        });
        $scope.user_groups_tree = tree;

        angular.forEach($scope.user_groups, function(group, key) {
          group.hasUsers = _.contains(group.users, $scope.user.uid);
        });

        $scope.original_groups = angular.copy($scope.user_groups);
        $scope.groupsChanged = false;
      };

      // called when user submits modifications on groups list for a user
      $scope.apply = function() {
        postGroups($scope, $scope.user, Restangular, flash);
      };
    });
  })
  .controller('UserCreateCtrl', function($scope, Restangular, flash) {
      $scope.publicContextPath = GEOR_config.publicContextPath;
      $scope.save = function() {
        Restangular.all('users').post(
          $scope.user
        ).then(function(user) {
          $scope.users.push(user);
          window.location = "#/users";
          flash.success = 'User correctly added';
        },
        function errorCallback(response) {
          if (response.status == 409) {
            flash.error = 'Error while creating the user: is the specified e-mail already used ?';
          } else {
            flash.error = 'Error while creating the user';
          }
        });
      };
  })
  .controller('FooCtrl', function($scope) {
    $scope.foo = "bar";
  });

function getPrefix(group) {
  return group.cn.indexOf('_') != -1 &&
         group.cn.substring(0, group.cn.indexOf('_'));
}

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

function postGroups($scope, users, Restangular, flash) {
  var i,
      len = $scope.user_groups.length,
      toPut = [],
      toDelete = [];
  users = _.isArray(users) ? users : [users];
  users = _.pluck(users, 'uid');

  // get the list of groups to put or delete for user
  for (i=0; i < len; i++) {
    var g = $scope.user_groups[i],
        og = $scope.original_groups[i];

    if (g.hasUsers != og.hasUsers) {
      if (g.hasUsers === 'all' || g.hasUsers === true) {
        toPut.push(g.cn);
      } else if (g.hasUsers === false){
        toDelete.push(g.cn);
      }
      // 'some' shouldn't be possible here
    }
  }

  // because the number of users can be important (for example when checkAll
  // checkbox is checked), body is less heavy this way
  var body = {
    "users": users,
    "PUT": toPut,
    "DELETE": toDelete
  };

  Restangular.all('groups_users').post(body).then(
    function() {
      angular.forEach(toPut, function(group) {
        var index = findByAttr($scope.groups, 'cn', group);
        group = $scope.groups[index];
        group.users = _.union(group.users || [], users);
      });
      angular.forEach(toDelete, function(group) {
        var index = findByAttr($scope.groups, 'cn', group);
        group = $scope.groups[index];
        group.users = _.difference(group.users, users);
      });

      flash.success = 'Modified successfully';

      var index = findByAttr($scope.groups, 'cn', 'a');
      var group = $scope.groups[index];
    },
    function errorCallback() {
      flash.error = 'Oops error from server :(';
    }
  );
}

function addNode(tree, node) {
  var prefix = getPrefix(node);

  if (prefix) {
    // creating a branch
    var branch = _.find(tree, function(obj) {return obj.name ==  prefix;});
    if (!branch) {
      branch = {name: prefix, nodes: []};
      tree.push(branch);
      tree.sort(function(a, b) {
        if (a.name.toLowerCase() > b.name.toLowerCase()) {
          return 1;
        } else {
          return -1;
        }
      });
    }
    branch.nodes.push({group: node});
    branch.nodes.sort(function(a, b) {
      if (a.group.cn.toLowerCase() > b.group.cn.toLowerCase()) {
        return 1;
      } else {
        return -1;
      }
    });
  } else {
    tree.push({name: node.cn, group: node});
  }
}

function removeNode(tree, nodeToRemove) {
  function loop(nodes) {
    angular.forEach(nodes, function(node, ndx) {
      if (node.nodes) {
        loop(node.nodes);
      } else {
        if (node.group.cn == nodeToRemove) {
          nodes = nodes.splice(ndx, 1);
        }
      }
    });
  }
  loop(tree, nodeToRemove);
}

$(document)
  .on('click.dropdown-menu', '.dropdown-menu > li.noclose', function (e) {
    e.stopPropagation();
  });
$('.groups').delegate('.accordion', 'show hide', function (n) {
    $(n.target).siblings('.accordion-heading').find('.accordion-toggle i').toggleClass('icon-chevron-down icon-chevron-right');
});
