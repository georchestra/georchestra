angular.module('admin_console')
.factory('User', ['$resource', 'LDAP_BASE_URI', ($resource, baseUri) =>
  $resource(baseUri + 'users/:id', { id: '@uid' }, {
    query: {
      cache   : true,
      method  : 'GET',
      isArray : true
    },
    get: {
      cache: true
    },
    update: {
      method: 'PUT'
    }
  })
]).factory('Email', ['$resource', 'LDAP_BASE_URI', ($resource, baseUri) =>
  $resource(baseUri + '../:id/emails', { id: '@id' }, {
    query: {
      method  : 'GET',
      isArray: false
    }
  })
]).factory('Orgs', ['$resource', 'LDAP_BASE_URI', ($resource, baseUri) =>
    $resource(baseUri + 'orgs', {}, {
        query: {
            cache: true,
            method:'GET',
            isArray:true
        }
    })
])
