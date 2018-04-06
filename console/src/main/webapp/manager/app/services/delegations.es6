angular.module('manager')
  .factory('Delegations', ['$resource', 'CONSOLE_PRIVATE_PATH', ($resource, baseUri) =>
    $resource(baseUri + 'delegation/delegations', {}, {
      query: {
        cache: true,
        method: 'GET',
        isArray: true
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
