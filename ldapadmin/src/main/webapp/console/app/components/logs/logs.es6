require('components/logs/logs.tpl')

class LogsController {

  static $inject = [ '$injector' ]

  constructor($injector) {

    this.itemsPerPage = 5
    this.$injector    = $injector

    let msg   = 'Error while loading data'

    this.logs = $injector.get('Logs').query({
      limit : 100000,
      page  : 0
    }, () => {
      this.senders = [ ...new Set(this.logs.logs.map(l => l.admin)) ]
      this.types   = [ ...new Set(this.logs.logs.map(l => l.type)) ]
    }, $injector.get('Flash').create.bind(this, 'error', msg, ''))

  }

}

let filter_logs = () => {
  return (logs, type, sender) => {
    if (!logs) { return }

    let filtered = logs.filter(log => {
      if (type && sender) {
        return (log.type == type && log.admin == sender)
      }
      if (type)   { return (log.type == type) }
      if (sender) { return (log.admin == sender) }
      return true
    })

    return filtered
  }
}

angular.module('admin_console').controller('LogsController', LogsController)
.filter('logs', filter_logs)
