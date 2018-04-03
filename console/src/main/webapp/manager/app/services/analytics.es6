angular.module('manager').factory('Analytics', [
  '$resource', 'ANALYTICS_BASE_URI', ($resource, baseUri) => $resource(
    baseUri + ':service', { service: '@service' },
    {
      get: {
        method: 'POST',
        cache: true,
        isArray: false
      },
      download: {
        method: 'POST',
        headers: {
          accept: 'application/csv'
        },
        responseType: 'arraybuffer',
        cache: false,
        transformResponse: (data, headers) => {
          let csv = null
          if (data) {
            csv = new Blob([data], {
              type: 'application/csv'
            })
          }

          return {
            response: {
              blob: csv,
              fileName: 'export.csv'
            }
          }
        }
      }
    }
  )
])
