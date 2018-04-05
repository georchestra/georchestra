angular.module('manager')
  .factory('Templates', ['$resource', 'CONSOLE_PRIVATE_PATH', ($resource, baseUri) =>
    $resource(baseUri + '../emailTemplates', {}, {
      query: {
        cache: true,
        isArray: false
      }
    })
  ]).factory('Mail', ['$resource', 'CONSOLE_PRIVATE_PATH', ($resource, baseUri) =>
    $resource(baseUri + '../:id/sendEmail', {id: '@id'}, {
      save: {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        transformRequest: function (data) {
          return $.param({
            subject: data.subject,
            content: data.content,
            attachments: data.attachments
          })
        }
      }
    })
  ]).factory('Attachments', ['$resource', 'CONSOLE_PRIVATE_PATH', ($resource, baseUri) =>
    $resource(baseUri + '../attachments', {}, {
      query: {
        cache: true,
        isArray: false
      }
    })
  ])
