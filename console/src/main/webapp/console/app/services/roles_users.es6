angular.module('admin_console')
.factory('RolesUsers', ['$resource', 'LDAP_BASE_URI', ($resource, baseUri) =>
  $resource(baseUri + 'groups_users', {}, {})
])
