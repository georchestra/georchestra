angular.module('admin_console').factory('Templates',
  ['$resource', 'LDAP_BASE_URI', function($resource, baseUri) {
    return $resource(baseUri + '../emailTemplates', {}, {
      query: {
        cache   : true,
        isArray : false
      }
    });
  }]
).factory('Mail',
  ['$resource', 'LDAP_BASE_URI', function($resource, baseUri) {
    return $resource(baseUri + '../:id/sendEmail', {id: '@id'}, {
      save: {
        method: 'POST',
        headers : {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: function (data) {
          return $.param({
           subject     : data.subject,
           content     : data.content,
           attachments : data.attachments
          });
        }
      }
    });
  }]
).factory('Attachments',
  ['$resource', 'LDAP_BASE_URI', function($resource, baseUri) {
    return $resource(baseUri + '../attachments', {}, {
      query: {
        cache   : true,
        isArray : false
      }
    });
  }]
);
