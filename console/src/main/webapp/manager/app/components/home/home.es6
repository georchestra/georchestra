require('components/home/home.tpl')

class HomeController {
  static $inject = [ '$injector' ]

  constructor ($injector) {
    const LOG_LIMIT = 15
    const EXPIRED = 'TEMPORARY'

    this.$injector = $injector

    $injector.get('Role').query(roles => {
      roles.forEach(role => {
        if (role.cn === EXPIRED) {
          this.expired = role
        }
      })
    })
    this.pendingCount = 0
    $injector.get('User').query(users => {
      this.pendingCount = users.filter(u => u.pending).length
    })

    this.i18n = {}
    $injector.get('translate')('analytics.errorload', this.i18n)

    const flash = $injector.get('Flash')

    let Analytics = $injector.get('Analytics')
    let options = {
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
  }
}

angular.module('manager').controller('HomeController', HomeController)
