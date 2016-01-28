angular.module('admin_console').factory('GroupsUsers',
  ['$resource', 'LDAP_BASE_URI', function($resource, baseUri) {
    return $resource(baseUri + 'groups_users', {}, { });
  }]
);
