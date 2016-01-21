angular.module('admin_console')
.factory('Group', ['$resource', 'LDAP_BASE_URI',

  function($resource, baseUri){
    return $resource(baseUri + 'groups', {}, {
      query: {
        cache: true,
        method:'GET',
        isArray:true
      }
    });
  }]

).factory('groupAdminList', [

  function() {
    var admin_groups = [
      'ADMINISTRATOR',
      'EDITOR',
      'PENDING_USERS',
      'SV_ADMIN',
      'SV_EDITOR',
      'SV_REVIEWER'/*,
      'SV_USER'*/
    ];
    return function() {
      return admin_groups;
    };
  }

]).factory('groupAdminFilter', [ 'groupAdminList',

  function(groupAdminList) {
    return function (group) {
      return groupAdminList().indexOf(group.cn) >= 0;
    };
  }

]);
