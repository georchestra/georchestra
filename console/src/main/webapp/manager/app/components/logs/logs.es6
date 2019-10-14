require('components/logs/logs.tpl')

class LogsController {
  static $inject = ['$injector']

  constructor ($injector) {
    this.$injector = $injector
    this.itemsPerPage = 15
    const i18n = {}
    this.$injector.get('translate')('logs.error', i18n)
    this.$injector.get('translate')('logs.alltarget', i18n)

    this.logs = $injector.get('Logs').query({
      limit: 100000,
      page: 0
    }, () => {
      // transform each logs changed value into json to find info during html construction
      this.logs.map(l => {
        l.changed = l.changed && l.changed.length ? JSON.parse(l.changed) : l.changed
      })
      let extract = (key) => [ ...new Set(this.logs.map(l => l[key])) ]
      this.senders = extract('admin')
      this.types = extract('type')
      this.targets = [{ key: 'all', value: i18n.alltarget }].concat(
        extract('target').map(g => ({ key: g, value: g }))
      )
    }, () => {
      $injector.get('Flash').create('danger', i18n.error)
    })

    this.target = 'all'

    this.date = {
      start: this.$injector.get('date').getDefault(),
      end: this.$injector.get('date').getEnd()
    }

    // get all orgs infos and orgs name
    this.orgsId = {}
    $injector.get('Orgs').query(orgs => {
      orgs.map(org => {
        this.orgsId[org.id] = org.name
      })
      this.orgs = orgs.map(o => o.name)
    })

    // get all role
    this.roles = []
    $injector.get('Role').query(roles => {
      this.roles = roles.map(role => role.cn)
    })

    // get all users
    this.users = []
    $injector.get('User').query(users => {
      this.users = users.map(user => user.uid)
    })
  }

  // get log info and return log target type or empty string
  getType (log) {
    let type = ''
    if (this.roles && this.orgsId && this.users && this.orgs) {
      if (log.type.indexOf('DELETED') > -1 || log.type.indexOf('REFUSED') > -1) {
        // avoid to create link for removed items
        return type
      } else if (this.roles.indexOf(log.target) > -1) {
        return 'ROLE'
      } else if (this.orgs.indexOf(log.target) > -1 || this.orgsId[log.target]) {
        return 'ORG'
      } else if (this.users.indexOf(log.target) > -1) {
        return 'USER'
      }
    }
    return type
  }

  isFiltered () {
    return this.admin || this.type || this.target !== 'all' ||
      this.date.start !== this.$injector.get('date').getDefault() ||
      this.date.end !== this.$injector.get('date').getEnd()
  }

  openLog (log) {
    // remove old log if not already deleted
    if (this.log) { delete this.log }
    // get messages for this user
    if (log.changed) {
      // transform string to json if not already done
      log.changed = log.changed && log.changed.length ? JSON.parse(log.changed) : log.changed
      if (log.changed.sender) {
        // only for mail
        log.trusted = this.$injector.get('$sce').trustAsHtml(log.changed.body)
      }
      this.log = log
    }
  }

  closeLog () {
    // remove log to avoir wrong behavior when log changed
    delete this.log
  }

  reset () {
    this.admin = undefined
    this.type = undefined
    this.target = 'all'
    this.date.start = this.$injector.get('date').getDefault()
    this.date.end = this.$injector.get('date').getEnd()
  }
}

const filterLogs = () => {
  return (logs, type, admin, target, date) => {
    if (!logs) { return }
    let filtered = logs.filter(log => {
      let valid = true
      if (type && log.type !== type) {
        valid = false
      }
      if (admin && log.admin !== admin) {
        valid = false
      }
      if (target !== 'all' && log.target !== target) {
        valid = false
      }
      if (date &&
        (moment(log.date).isBefore(date.start) ||
        moment(log.date).isAfter(date.end))) {
        valid = false
      }
      return valid
    })

    return filtered
  }
}

const logDateFilter = () => date => moment(date).format('YYYY-MM-DD HH:mm')

angular
  .module('manager')
  .controller('LogsController', LogsController)
  .filter('logs', filterLogs)
  .filter('logDate', logDateFilter)
