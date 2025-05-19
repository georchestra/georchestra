angular.module('manager')
  .factory('Orgs', ['$resource', 'CONSOLE_PRIVATE_PATH', ($resource, baseUri) =>
    $resource(baseUri + 'orgs/:id', {}, {
      query: {
        cache: true,
        method: 'GET',
        isArray: true,
        params: { logos: '@logos' }
      },
      get: {
        params: { id: '@id' },
        method: 'GET',
        cache: true,
        isArray: false
      },
      update: {
        params: { id: '@id' },
        method: 'PUT'
      },
      delete: {
        params: { id: '@id' },
        method: 'DELETE'
      }
    })
  ]).factory('OrgsRequired', ['$resource', 'CONSOLE_PUBLIC_PATH', ($resource, baseUri) =>
    $resource(baseUri + 'orgs/requiredFields', {}, {
      query: {
        method: 'GET',
        cache: true,
        transformResponse: (data) => {
          const response = {}
          JSON.parse(data).forEach(key => { response[key] = true })
          return response
        }
      }
    })
  ]).factory('OrgsType', ['$resource', 'CONSOLE_PUBLIC_PATH', ($resource, baseUri) =>
    $resource(baseUri + 'orgs/orgTypeValues', {}, {
      query: {
        method: 'GET',
        cache: true,
        isArray: true
      }
    })
  ]).factory('OrgsRoles', ['$resource', 'CONSOLE_PUBLIC_PATH', ($resource, baseUri) =>
    $resource(baseUri + 'orgs/orgTypeValues', {}, {
      query: {
        method: 'GET',
        cache: true,
        isArray: true
      }
    })
  ])
