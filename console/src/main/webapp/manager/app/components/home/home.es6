require('components/home/home.tpl')

class HomeController {
  static $inject = [ '$injector' ]

  constructor ($injector) {
    const LOG_LIMIT = 15
    const PENDING = 'PENDING'
    const EXPIRED = 'TEMPORARY'

    this.$injector = $injector

    $injector.get('Role').query(roles => {
      roles.forEach(role => {
        if (role.cn === PENDING) {
          this.pending = role
        }
        if (role.cn === EXPIRED) {
          this.expired = role
        }
      })
    })

    this.i18n = {}
    $injector.get('translate')('analytics.errorload', this.i18n)

    // FIX ME : use function instead if a bind
    let error = $injector.get('Flash').create.bind(
      $injector.get('Flash'), 'danger', this.i18n.errorload
    )
    let Analytics = $injector.get('Analytics')
    let options = {
      service: 'distinctUsers',
      startDate: $injector.get('date').getFromDiff('day'),
      endDate: $injector.get('date').getEnd()
    }

    this.connected = Analytics.get(options, () => {}, error)
    this.requests = Analytics.get({
      ...options,
      service: 'combinedRequests',
      startDate: $injector.get('date').getFromDiff('week')
    }, () => {}, error)

    this.logs = this.$injector.get('Logs').query({
      limit: LOG_LIMIT,
      page: 0
    }, () => {}, error)
  }
}

angular.module('admin_console').controller('HomeController', HomeController)
