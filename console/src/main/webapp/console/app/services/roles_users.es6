angular.module('admin_console')
.factory('GroupsUsers', ['$resource', 'LDAP_BASE_URI', ($resource, baseUri) =>
  $resource(baseUri + 'groups_users', {}, {})
])
