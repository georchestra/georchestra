require('components/home/home.tpl')

class HomeController {

  static $inject = [ '$injector' ]

  constructor($injector) {

    const LOG_LIMIT = 10
    const PENDING   = 'PENDING'
    const EXPIRED   = 'SV'

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

    let flash     = $injector.get('Flash')
    let error     = flash.create.bind(flash, 'error', this.i18n.errorload)
    let Analytics = $injector.get('Analytics')
    let date      = $injector.get('date')
    let options   = {
      service   : 'distinctUsers',
      startDate : date.getFromDiff('week'),
      endDate   : date.getEnd()
    }

    this.requests_conf = [ 'date', 'count' ]

    this.connected  = Analytics.get(options, () => {}, error)

    options.service = 'combinedRequests'
    this.requests = Analytics.get(options, () => {}, error)

    this.logs = this.$injector.get('Logs').query({
      limit: LOG_LIMIT,
      page: 0
    }, () => {}, error)

  }

}

angular.module('admin_console').controller('HomeController', HomeController)
