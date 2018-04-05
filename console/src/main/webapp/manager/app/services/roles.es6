angular.module('manager')
  .factory('Role', ['$resource', 'CONSOLE_PRIVATE_PATH', ($resource, baseUri) =>
    $resource(baseUri + 'roles/:id', {}, {
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
  ]).factory('roleAdminList', [ () => {
    const adminRoles = [
      'SUPERUSER',
      'ADMINISTRATOR',
      'GN_ADMIN',
      'GN_EDITOR',
      'GN_REVIEWER',
      'EXTRACTORAPP',
      'USER',
      'PENDING',
      'TEMPORARY'
    ]
    return () => adminRoles
  }]).factory('roleAdminFilter', [ 'roleAdminList', (roleAdminList) =>
    (role) => roleAdminList().indexOf(role.cn) >= 0
  ])
