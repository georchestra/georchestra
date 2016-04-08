angular.module('admin_console')
.factory('Logs', ['$resource', 'LDAP_BASE_URI', ($resource, baseUri) =>
  $resource(baseUri + 'admin_logs/:limit/:page', {}, {
    query     : {
      method  : 'GET',
      cache   : true,
      isArray : false
    }
  })
])
