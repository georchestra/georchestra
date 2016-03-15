angular.module('admin_console').factory('Util', () => {
  return {
    getDateFromDiff: (date, interval) => {
      let m = moment()
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
      return m.format('YY-MM-DD')
    }
  }
})
