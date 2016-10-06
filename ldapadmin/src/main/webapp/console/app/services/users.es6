angular.module('admin_console')
.factory('User', ['$resource', 'LDAP_BASE_URI', ($resource, baseUri) =>
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
]).factory('Email', ['$resource', 'LDAP_BASE_URI', ($resource, baseUri) =>
  $resource(baseUri + '../:id/emails', { id: '@id' }, {
    query: {
      method: 'GET',
      isArray: false
    }
  })
]).factory('Orgs', ['$resource', 'LDAP_BASE_URI', ($resource, baseUri) =>
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
])
