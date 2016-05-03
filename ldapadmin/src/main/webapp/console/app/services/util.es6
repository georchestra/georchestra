angular.module('admin_console')
.factory('date', () => {

  let format = 'YYYY-MM-DD'

  return {

    getFromDiff: (interval) => {
      let m = moment().add(1, 'day')
      if (interval == 'day') {
        m = m.subtract(1, 'day')
      }
      if (interval == 'week') {
        m = m.subtract(1, 'weeks')
      }
      if (interval == 'month') {
        m = m.subtract(1, 'months')
      }
      if (interval == '3month') {
        m = m.subtract(3, 'months')
      }
      if (interval == 'year') {
        m = m.subtract(1, 'year')
      }
      return m.format(format)
    },

    getDefault : () => moment().subtract(1, 'year').format(format),

    getEnd     : () => moment().add(1, 'day').format(format)

  }

})
