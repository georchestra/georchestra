require('components/home/home.tpl')

class HomeController {

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

    let msg       = 'Error while loading data'
    let error     = $injector.get('Flash').create.bind(this, 'error', msg, '')
    let Analytics = $injector.get('Analytics')
    let options   = {
      service   : 'distinctUsers',
      startDate : moment().subtract(19, 'week').format('YYYY-MM-DD'),
      endDate   : moment().format('YYYY-MM-DD')
    }

    this.requests_conf = [ 'layer', 'count' ]

    this.connected  = Analytics.get(options, () => {}, error)

    options.limit   = 10
    options.service = 'layersUsage'
    this.requests = Analytics.get(options, () => {}, error)

    this.logs = this.$injector.get('Logs').query({
      limit: LOG_LIMIT,
      page: 0
    }, () => {}, error)

  }

}

HomeController.$inject = [ '$injector' ]

angular.module('admin_console').controller('HomeController', HomeController)
