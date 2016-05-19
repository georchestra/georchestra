angular.module('admin_console')
.factory('Contexts', ['$resource', 'MF_BASE_URI', ($resource, baseUri) =>
  $resource(baseUri + 'contexts', {}, {
    query: {
      cache   : true,
      isArray : true
    }
  })
])
