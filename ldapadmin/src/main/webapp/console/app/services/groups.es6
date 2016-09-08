angular.module('admin_console')
.factory('Group', ['$resource', 'LDAP_BASE_URI', ($resource, baseUri) =>
  $resource(baseUri + 'groups/:id', { id: '@cn' }, {
    query: {
      cache: true,
      method:'GET',
      isArray:true
    },
    update: {
      method: 'PUT'
    }
  })
]).factory('groupAdminList', [ () => {
  var admin_groups = [
    'ADMINISTRATOR',
    'PENDING',
    'MOD_ANALYTICS',
    'MOD_EXTRACTORAPP',
    'MOD_LDAPADMIN',
    'GN_ADMIN',
    'GN_EDITOR',
    'GN_REVIEWER',
    'USER',
    'TEMPORARY'
  ]
  return () => admin_groups
}]).factory('groupAdminFilter', [ 'groupAdminList', (groupAdminList)  =>
  (group) => groupAdminList().indexOf(group.cn) >= 0
])
