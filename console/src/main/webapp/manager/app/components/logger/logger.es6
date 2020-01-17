require('components/logger/logger.tpl')

class LoggerController {
  static $inject = ['$element', '$scope', '$injector']
  constructor ($element, $scope, $injector) {
    this.$injector = $injector
    this.$element = $element
    this.$scope = $scope
  }

  $onInit () {
    this.itemsPerPage = 15
    const i18n = {}
    this.$injector.get('translate')('logs.error', i18n)
    this.$injector.get('translate')('logs.alltarget', i18n)
    this.$injector.get('translate')('logs.allsender', i18n)
    this.$injector.get('translate')('logs.alltype', i18n)
    // manage query params to get user's or complete logs
    let typeQuery = 'Logs'
    const params = {
      limit: 100000,
      page: 0
    }
    if (this.user) {
      params.id = this.user
      typeQuery = 'UserLogs'
    }

    this.logs = this.$injector.get(typeQuery).query(params, () => {
      // transform each logs changed value into json to find info during html construction
      this.logs.map(l => {
        l.changed = l.changed && l.changed.length ? JSON.parse(l.changed) : l.changed
      })
      const extract = (key) => [...new Set(this.logs.map(l => l[key]))]

      this.senders = [{ key: 'all', value: i18n.allsender }].concat(
        extract('admin').map(g => ({ key: g, value: g }))
      )
      this.types = [{ key: 'all', value: i18n.alltype }].concat(
        extract('type').map(g => ({ key: g, value: g }))
      )
      this.targets = [{ key: 'all', value: i18n.alltarget }].concat(
        extract('target').map(g => ({ key: g, value: g }))
      )
    }, () => {
      this.$injector.get('Flash').create('danger', i18n.error)
    })

    this.target = 'all'
    this.type = 'all'
    this.admin = 'all'

    this.date = {
      start: this.$injector.get('date').getDefault(),
      end: this.$injector.get('date').getEnd()
    }

    // get all orgs infos and orgs name
    this.orgsId = {}
    this.$injector.get('Orgs').query(orgs => {
      orgs.map(org => {
        this.orgsId[org.id] = org.name
      })
      this.orgs = orgs.map(o => o.name)
    })

    // get all role
    this.roles = []
    this.$injector.get('Role').query(roles => {
      this.roles = roles.map(role => role.cn)
    })

    // get all users
    this.users = []
    this.$injector.get('User').query(users => {
      this.users = users.map(user => user.uid)
    })
  }

  getTitle () {
    if (this.title === undefined) {
      return true
    }
    return this.title
  }

  // get log info and return log target type or empty string
  getType (log) {
    const type = ''
    if (log && this.roles && this.orgsId && this.users && this.orgs) {
      if (log.type.indexOf('DELETED') > -1 || log.type.indexOf('REFUSED') > -1) {
        // avoid to create link for removed items
        return type
      } else if (this.roles.indexOf(log.target) > -1) {
        return 'ROLE'
      } else if (this.orgsId[log.target]) {
        return 'ORG'
      } else if (this.users.indexOf(log.target) > -1) {
        return 'USER'
      }
    }
    return type
  }

  isFiltered () {
    return this.admin !== 'all' || this.type !== 'all' || this.target !== 'all' ||
      this.date.start !== this.$injector.get('date').getDefault() ||
      this.date.end !== this.$injector.get('date').getEnd()
  }

  openLog (log) {
    // remove old log if not already deleted
    if (this.log) { delete this.log }
    // get messages for this user
    if (log && log.changed) {
      // transform string to json if not already done
      log.changed = log.changed && log.changed.length ? JSON.parse(log.changed) : log.changed
      if (log.changed.sender) {
        // only for mail
        log.trusted = this.$injector.get('$sce').trustAsHtml(log.changed.body)
      }
      this.log = log
    }
  }

  getDescription (log) {
    if (log && this.log.changed) {
      if (log.changed.sender) {
        // only for mail
        return 'message'
      } else if (log.changed.field === 'cities') {
        return 'changeArea'
      } else if (log.changed.new && log.changed.old) {
        // old value changed for another
        return 'updateValue'
      } else if (log.changed.new && !log.changed.old) {
        // nothing to compare it's a new value
        return 'initValue'
      } else if (!log.changed.new && log.changed.old) {
        // replace value by nothing
        return 'clearValue'
      }
    }
  }

  closeLog () {
    // remove log to avoir wrong behavior when log changed
    delete this.log
  }

  reset () {
    this.admin = 'all'
    this.type = 'all'
    this.target = 'all'
    this.date.start = this.$injector.get('date').getDefault()
    this.date.end = this.$injector.get('date').getEnd()
  }
}

const filterLogs = () => {
  return (logs, type, admin, target, date) => {
    if (!logs) { return }
    const filtered = logs.filter(log => {
      let valid = true
      if (type !== 'all' && log.type !== type) {
        valid = false
      }
      if (admin !== 'all' && log.admin !== admin) {
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

angular.module('manager')
  .component('logger', {
    bindings: {
      filter: '=',
      title: '=',
      user: '='
    },
    controller: LoggerController,
    controllerAs: 'logger',
    templateUrl: 'components/logger/logger.tpl.html'
  })
  .filter('logs', filterLogs)
  .filter('logDate', logDateFilter)
