angular.module('manager')
  .factory('User', ['$resource', 'CONSOLE_PRIVATE_PATH', ($resource, baseUri) =>
    $resource(baseUri + 'users/:id', { id: '@uid' }, {
      query: {
        cache: true,
        method: 'GET',
        isArray: true
      },
      get: {
        cache: true
      },
      update: {
        method: 'PUT'
      }
    })
  ]).factory('Email', ['$resource', 'CONSOLE_PRIVATE_PATH', ($resource, baseUri) =>
    $resource(baseUri + '../:id/emails', { id: '@id' }, {
      query: {
        method: 'GET',
        isArray: false
      }
    })
  ]).factory('Profile', ['$resource', 'LDAP_BASE_URI', ($resource, baseUri) =>
    $resource(baseUri + 'users/profile', {}, {
      query: {
        method: 'GET',
        isArray: false
      }
    })
  ]).factory('UserRequired', ['$resource', 'CONSOLE_PUBLIC_PATH', ($resource, baseUri) =>
    $resource(baseUri + 'users/requiredFields', {}, {
      get: {
        method: 'GET',
        cache: true,
        transformResponse: (data) => {
          let response = {}
          JSON.parse(data).forEach(key => { response[key] = true })
          return response
        }
      }
    })
  ])
