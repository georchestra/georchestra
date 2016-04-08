angular.module('admin_console')
.factory('Group', ['$resource', 'LDAP_BASE_URI', ($resource, baseUri) =>
  $resource(baseUri + 'groups', {}, {
    query: {
      cache: true,
      method:'GET',
      isArray:true
    }
  })
]).factory('groupAdminList', [ () => {
  var admin_groups = [
    'ADMINISTRATOR',
    'EDITOR',
    'PENDING_USERS',
    'SV_ADMIN',
    'SV_EDITOR',
    'SV_REVIEWER'/*,
    'SV_USER'*/
  ]
  return () => admin_groups
}]).factory('groupAdminFilter', [ 'groupAdminList', (groupAdminList)  =>
  (group) => groupAdminList().indexOf(group.cn) >= 0
])
