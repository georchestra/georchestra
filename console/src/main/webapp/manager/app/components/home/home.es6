require('components/home/home.tpl')

class HomeController {
  static $inject = ['$injector']

  constructor ($injector) {
    const EXPIRED_ROLE = $injector.get('expiredRole')

    this.$injector = $injector

    $injector.get('Role').query(roles => {
      this.expired = roles.find(r => (r.cn === EXPIRED_ROLE))
    })
    this.pendingCount = 0
    $injector.get('User').query(users => {
      this.pendingCount = users.filter(u => u.pending).length
    })

    this.i18n = {}
    $injector.get('translate')('analytics.errorload', this.i18n)

    const flash = $injector.get('Flash')

    const platformInfos = $injector.get('PlatformInfos').get()

    if (platformInfos.analyticsEnabled) {
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
    }
  }
}

angular.module('manager').controller('HomeController', HomeController)
