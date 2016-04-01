require('components/logs/logs.tpl')

class LogsController {

  static $inject = [ '$injector' ]

  constructor($injector) {

    this.$injector = $injector
    this.itemsPerPage = 5

    let msg   = 'Error while loading data'

    this.logs = $injector.get('Logs').query({
      limit : 100000,
      page  : 0
    }, () => {
      this.senders = [ ...new Set(this.logs.logs.map(l => l.admin)) ]
      this.types   = [ ...new Set(this.logs.logs.map(l => l.type)) ]
    }, $injector.get('Flash').create.bind(this, 'error', msg, ''))

    this.date = {
      start: this.$injector.get('Util').getDefaultDate()
    }

  }

  isFiltered() {
    return this.admin || this.type ||
      this.date.start != this.$injector.get('Util').getDefaultDate()
  }

  reset() {
    this.admin      = undefined
    this.type       = undefined
    this.date.start = this.$injector.get('Util').getDefaultDate()
  }

}

let filter_logs = () => {
  return (logs, type, admin, date) => {
    if (!logs) { return }

    let filtered = logs.filter(log => {
      let valid = true
      if (type && log.type != type)   {
        valid = false
      }
      if (admin && log.admin != admin)   {
        valid = false
      }
      if (date && moment(log.date).isBefore(date.start))   {
        valid = false
      }
      return valid
    })

    return filtered
  }
}

angular.module('admin_console').controller('LogsController', LogsController)
.filter('logs', filter_logs)
