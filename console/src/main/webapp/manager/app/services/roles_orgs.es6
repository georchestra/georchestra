angular.module('manager')
  .factory('RolesOrgs', ['$resource', 'CONSOLE_PRIVATE_PATH', ($resource, baseUri) =>
    $resource(baseUri + 'roles_orgs', {}, {})
  ])
