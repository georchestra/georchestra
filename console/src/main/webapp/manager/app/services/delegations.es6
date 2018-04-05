angular.module('admin_console')
  .factory('Delegations', ['$resource', 'LDAP_BASE_URI', ($resource, baseUri) =>
    $resource(baseUri + 'delegation/delegations', {}, {
      query: {
        cache: true,
        method: 'GET',
        // isArray: true
        isArray: false
      },
      get: {
        isArray: false
      },
      update: {
        url: baseUri + 'delegation/:uid',
        params: { uid: '@uid' },
        method: 'POST'
      },
      'delete': {
        url: baseUri + 'delegation/:uid',
        params: { uid: '@uid' },
        method: 'DELETE'
      }
    })
  ])
