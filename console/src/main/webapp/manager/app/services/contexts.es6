angular.module('manager')
  .factory('Contexts', ['$resource', 'VIEWER_SERVICES_PATH', ($resource, baseUri) =>
    $resource(baseUri + 'contexts', {}, {
      query: {
        cache: true,
        isArray: true
      }
    })
  ])
