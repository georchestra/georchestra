angular.module('admin_console')
  .factory('Orgs', ['$resource', 'LDAP_BASE_URI', ($resource, baseUri) =>
    $resource(baseUri + 'orgs/:id', {}, {
      query: {
        cache: true,
        method: 'GET',
        isArray: true
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
  ]).factory('OrgsRequired', ['$resource', 'LDAP_PUBLIC_URI', ($resource, baseUri) =>
    $resource(baseUri + 'orgs/requiredFields', {}, {
      query: {
        method: 'GET',
        cache: true,
        transformResponse: (data) => {
          let response = {}
          JSON.parse(data).forEach(key => { response[key] = true })
          return response
        }
      }
    })
  ]).factory('OrgsType', ['$resource', 'LDAP_PUBLIC_URI', ($resource, baseUri) =>
    $resource(baseUri + 'orgs/orgTypeValues', {}, {
      query: {
        method: 'GET',
        cache: true,
        isArray: true
      }
    })
  ])
