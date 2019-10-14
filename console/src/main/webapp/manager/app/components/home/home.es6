require('components/home/home.tpl')

class HomeController {
  static $inject = ['$injector']

  constructor ($injector) {
    const LOG_LIMIT = 15
    const EXPIRED_ROLE = $injector.get('expiredRole')

    this.$injector = $injector

    $injector.get('Role').query(roles => {
      this.expired = roles.find(r => (r.cn = EXPIRED_ROLE))
    })
    this.pendingCount = 0
    $injector.get('User').query(users => {
      this.pendingCount = users.filter(u => u.pending).length
    })

    this.i18n = {}
    $injector.get('translate')('analytics.errorload', this.i18n)

    const flash = $injector.get('Flash')

    const Analytics = $injector.get('Analytics')
    const options = {
      service: 'distinctUsers',
      startDate: $injector.get('date').getFromDiff('day'),
      endDate: $injector.get('date').getEnd()
    }

    this.connected = Analytics.get(options, () => {}, () => {
      flash.create('danger', this.i18n.errorload)
    })
    this.requests = Analytics.get({
      ...options,
      service: 'combinedRequests.json',
      startDate: $injector.get('date').getFromDiff('week')
    }, () => {}, () => {
      flash.create('danger', this.i18n.errorload)
    })

    this.logs = this.$injector.get('Logs').query({
      limit: LOG_LIMIT,
      page: 0
    }, () => {}, () => {
      flash.create('danger', this.i18n.errorload)
    })

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
  // usefull to get clean and functionnal link
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
}

angular.module('manager').controller('HomeController', HomeController)
