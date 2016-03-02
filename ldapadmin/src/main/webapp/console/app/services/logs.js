angular.module('admin_console').factory('Logs',
  ['$resource', 'ANALYTICS_BASE_URI', function($resource, baseUri) {
    return $resource(baseUri + 'admin_logs', {}, {
      query     : {
        method  : 'GET',
        cache   : true,
        isArray : false
      }
    });
  }]
);
