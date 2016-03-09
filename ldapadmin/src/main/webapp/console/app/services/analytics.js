angular.module('admin_console').factory('Analytics',
  ['$resource', 'ANALYTICS_BASE_URI', function($resource, baseUri) {
    return $resource(baseUri + ':service', { service: '@service' }, {
      get     : {
        method  : 'POST',
        cache   : true,
        isArray : false
      }
    });
  }]
);
