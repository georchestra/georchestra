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
      let extract  = (key) => [ ...new Set(this.logs.logs.map(l => l[key])) ]
      this.senders = extract('admin')
      this.types   = extract('type')
      this.targets = extract('target')
    }, $injector.get('Flash').create.bind(this, 'error', msg, ''))

    this.date = {
      start : this.$injector.get('date').getDefault(),
      end   : this.$injector.get('date').getEnd()
    }

  }

  isFiltered() {
    return this.admin || this.type || this.target ||
      this.date.start != this.$injector.get('date').getDefault() ||
      this.date.end != this.$injector.get('date').getEnd()
  }

  reset() {
    this.admin      = undefined
    this.type       = undefined
    this.target     = undefined
    this.date.start = this.$injector.get('date').getDefault()
    this.date.end   = this.$injector.get('date').getEnd()
  }

}

let filter_logs = () => {
  return (logs, type, admin, target, date) => {
    if (!logs) { return }

    let filtered = logs.filter(log => {
      let valid = true
      if (type && log.type != type)   {
        valid = false
      }
      if (admin && log.admin != admin)   {
        valid = false
      }
      if (target && log.target != target)   {
        valid = false
      }
      if (date &&
        (moment(log.date).isBefore(date.start) ||
        moment(log.date).isAfter(date.end)) )   {
        valid = false
      }
      return valid
    })

    return filtered
  }
}

angular.module('admin_console').controller('LogsController', LogsController)
.filter('logs', filter_logs)
