angular.module('manager')
  .factory('Logs', ['$resource', 'CONSOLE_PRIVATE_PATH', ($resource, baseUri) =>
    $resource(baseUri + 'admin_logs/:limit/:page', {}, {
      query: {
        method: 'GET',
        cache: true,
        isArray: true
      }
    })
  ])
  .factory('UserLogs', ['$resource', 'CONSOLE_PRIVATE_PATH', ($resource, baseUri) =>
    $resource(baseUri + 'admin_logs/:id/:limit/:page', {}, {
      query: {
        method: 'GET',
        cache: true,
        isArray: true
      }
    })
  ])
