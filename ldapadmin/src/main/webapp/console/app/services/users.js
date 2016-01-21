angular.module('admin_console').factory('User',
  ['$resource', 'LDAP_BASE_URI', function($resource, baseUri){
    return $resource(baseUri + 'users/:id', { id: '@uid' }, {
      query: {
        cache   : true,
        method  : 'GET',
        isArray : true
      },
      update: {
        method: 'PUT'
      }
    });
  }]
);
