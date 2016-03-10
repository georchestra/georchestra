angular.module('admin_console').factory('Logs',
  ['$resource', 'LDAP_BASE_URI', function($resource, baseUri) {
    return $resource(baseUri + 'admin_logs/:limit/:page', {}, {
      query     : {
        method  : 'GET',
        cache   : true,
        isArray : false
      }
    });
  }]
);
