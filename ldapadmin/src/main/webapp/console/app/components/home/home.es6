require('components/home/home.tpl')

class HomeController {

  static $inject = [ '$injector' ]

  constructor($injector) {

    const LOG_LIMIT = 15
    const PENDING   = 'PENDING'
    const EXPIRED   = 'TEMPORARY'

    this.$injector = $injector;

    $injector.get('Group').query(groups => {
      groups.forEach(group => {
        if (group.cn == PENDING) {
          this.pending = group
        }
        if (group.cn == EXPIRED) {
          this.expired = group
        }
      })
    })

    this.i18n = {}
    $injector.get('translate')('analytics.errorload', this.i18n)

    let error     = $injector.get('Flash').create.bind(
      $injector.get('Flash'), 'error', this.i18n.errorload
    )
    let Analytics = $injector.get('Analytics')
    let options   = {
      service   : 'distinctUsers',
      startDate : $injector.get('date').getFromDiff('week'),
      endDate   : $injector.get('date').getEnd()
    }

    this.connected  = Analytics.get(options, () => {}, error)
    options.service = 'combinedRequests'
    this.requests   = Analytics.get(options, () => {}, error)

    this.logs = this.$injector.get('Logs').query({
      limit: LOG_LIMIT,
      page: 0
    }, () => { this.logs.logs.reverse() }, error)

  }

}

angular.module('admin_console').controller('HomeController', HomeController)
