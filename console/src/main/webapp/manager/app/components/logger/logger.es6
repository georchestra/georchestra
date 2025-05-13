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
    this.i18n = {}
    ;[
      'error',
      'alltarget',
      'allsender',
      'alltype',
      'added',
      'removed',
      'set',
      'replace',
      'to',
      'clear',
      'pendingusercreated',
      'pendinguserrefused',
      'pendinguseraccepted',
      'pendingorgcreated',
      'pendingorgrefused',
      'pendingorgaccepted',
      'roledeleted',
      'roldecreated',
      'orgcreated',
      'orgdeleted',
      'userpasswordchanged',
      'usercreated',
      'userdeleted',
      'system',
      'custom',
      'roleadded',
      'roleremoved',
      'emailrecoverysent',
      'rolecreated',
      'oauth2usercreated'
    ].forEach(tr => this.$injector.get('translate')('logs.' + tr, this.i18n))
    // manage query params to get user's or complete logs
    let typeQuery = 'Logs'
    const params = {
      limit: 500,
      page: 0
    }
    if (this.user) {
      params.id = this.user
      typeQuery = 'UserLogs'
    }

    this.getCitiesLog = function (log) {
      let res = ''
      res += log.changed.added ? `${log.changed.added} ${this.i18n.added}${log.changed.new ? `: ${log.changed.new}.` : '.'}` : ''
      res += log.changed.removed ? `${log.changed.removed} ${this.i18n.removed}${log.changed.old ? `: ${log.changed.old}.` : '.'}` : '.'
      return res
    }

    this.getAttrLog = function (log) {
      let res = ''
      if (log.changed.field === 'logo') {
        res += log.changed.new ? this.i18n.set : this.i18n.clear
        res = log.changed.old && log.changed.new ? this.i18n.replace : res
      } else {
        res += log.changed.new && !log.changed.old ? `${this.i18n.set} ${log.changed.new}.` : ''
        res += log.changed.old && !log.changed.new ? `${this.i18n.clear} ${log.changed.old}.` : ''
        res += log.changed.old && log.changed.new ? `${this.i18n.replace} ${log.changed.old} ${this.i18n.to} ${log.changed.new}.` : ''
      }
      return res
    }

    this.logs = this.$injector.get(typeQuery).query(params, () => {
      // transform each logs changed value into json to find info during html construction
      this.logs.forEach(l => {
        l.changed = JSON.parse(l.changed)
        // message
        if (l.type === 'EMAIL_SENT') { l.title = 'msg.sent' }
        // attributs
        if (l.type.indexOf('_ATTRIBUTE_CHANGED') >= 0 && l.changed) {
          l.title = (l.changed.field === 'cities')
            ? this.getCitiesLog(l)
            : this.getAttrLog(l)
          l.changed.fieldI18nKey = l.type.split('_').shift().toLowerCase() + '.' + l.changed.field
        } else {
          l.title = this.i18n[l.type.split('_').join('').toLowerCase()]
        }
        // role
        if (l.type.indexOf('CUSTOM') >= 0 || l.type.indexOf('SYSTEM') >= 0) {
          // get type for a role as custom or system
          const i18nType = this.i18n[l.type.split('_')[0].toLowerCase()]
          // action added or removed
          const i18nAction = this.i18n[l.type.split('_').slice(1, 3).join('').toLowerCase()]
          l.title = `${i18nType} ${l.changed?.field} ${i18nAction} ${l.target}`
        }
        // get icon name
        let iconName = ''
        if (l.type.indexOf('CREATED') > -1 || l.type.indexOf('ADDED') > -1) {
          iconName = l.type.indexOf('PENDING') > -1 ? 'plus' : 'plus-sign'
        } else if (l.type.indexOf('REFUSED') > -1) {
          iconName = 'remove'
        } else if (l.type.indexOf('ACCEPTED') > -1) {
          iconName = 'ok'
        } else if (l.type.indexOf('DELETED') > -1 || l.type.indexOf('REMOVED') > -1) {
          iconName = 'minus-sign'
        } else {
          iconName = 'edit'
        }
        l.icon = iconName
      })

      const extract = (key) => [...new Set(this.logs.map(l => l[key]))]

      this.senders = [{ key: 'all', value: this.i18n.allsender }].concat(
        extract('admin').map(g => ({ key: g, value: g }))
      )
      this.types = [{ key: 'all', value: this.i18n.alltype }].concat(
        extract('type').map(g => ({ key: g, value: g }))
      )
      this.targets = [{ key: 'all', value: this.i18n.alltarget }].concat(
        extract('target').map(g => ({ key: g, value: g }))
      )
    }, () => {
      this.$injector.get('Flash').create('danger', this.i18n.error)
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
    this.$injector.get('Orgs').query({ logos: false }, (orgs) => {
      orgs.forEach(org => {
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
    this.usersNames = {}
    this.$injector.get('User').query(users => {
      this.users = users.map(user => user.uid)
      this.usersNames = users.reduce((acc, u) => {
        acc[u.uid] = u.sn + ' ' + u.givenName
        return acc
      }, {})
    })
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
