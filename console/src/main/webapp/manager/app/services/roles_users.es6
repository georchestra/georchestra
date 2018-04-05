angular.module('manager')
  .factory('RolesUsers', ['$resource', 'CONSOLE_PRIVATE_PATH', ($resource, baseUri) =>
    $resource(baseUri + 'roles_users', {}, {})
  ])
