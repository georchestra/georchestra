angular.module('admin_console')
.factory('Group', ['$resource', 'LDAP_BASE_URI', ($resource, baseUri) =>
  $resource(baseUri + 'groups/:id', {}, {
    query: {
      cache: true,
      method: 'GET',
      isArray: true
    },
    get: {
      isArray: false
    },
    update: {
      params: { id: '@cn' },
      method: 'PUT'
    },
    delete: {
      params: { id: '@cn' },
      method: 'DELETE'
    }
  })
]).factory('groupAdminList', [ () => {
  const adminGroups = [
    'ADMINISTRATOR',
    'PENDING',
    'MOD_ANALYTICS',
    'MOD_EXTRACTORAPP',
    'SUPERUSER',
    'GN_ADMIN',
    'GN_EDITOR',
    'GN_REVIEWER',
    'USER',
    'TEMPORARY'
  ]
  return () => adminGroups
}]).factory('groupAdminFilter', [ 'groupAdminList', (groupAdminList) =>
  (group) => groupAdminList().indexOf(group.cn) >= 0
])
