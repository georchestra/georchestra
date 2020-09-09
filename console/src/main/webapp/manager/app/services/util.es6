angular.module('manager')
  .factory('date', () => {
    const format = 'YYYY-MM-DD'

    return {

      getFromDiff: (interval) => {
        let m = moment().add(1, 'day')
        if (interval === 'day') {
          m = m.subtract(1, 'day')
        }
        if (interval === 'week') {
          m = m.subtract(1, 'weeks')
        }
        if (interval === 'month') {
          m = m.subtract(1, 'months')
        }
        if (interval === '3month') {
          m = m.subtract(3, 'months')
        }
        if (interval === 'year') {
          m = m.subtract(1, 'year')
        }
        return m.format(format)
      },

      getDefault: () => moment().add(1, 'day').subtract(1, 'month').format(format),

      getEnd: () => moment().add(1, 'day').format(format)

    }
  })
  .factory('PlatformInfos', ['$resource', 'CONSOLE_PRIVATE_PATH', ($resource, baseUri) =>
    $resource(baseUri + 'platform/infos', {}, {
      query: {
        method: 'GET',
        isArray: false
      }
    })
  ])
