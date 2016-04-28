require('components/analytics/analytics.tpl')
require('components/date/date')

require('services/analytics')

class AnalyticsController {

  constructor($injector, $routeParams) {

    this.$injector = $injector

    this.group  = $routeParams.group
    this.groups = this.$injector.get('Group').query()

    this.date = {
      start : moment().subtract(1, 'year').format('YYYY-MM-DD'),
      end   : moment().format('YYYY-MM-DD')
    }

    this.data = {}
    this.config = {
      layers   : [ 'layer', 'count' ],
      requests : [ 'date', 'count' ]
    }

    this.load((this.group != 'all') ? this.group : undefined)
  }

  load(group) {
    let Flash     = this.$injector.get('Flash')
    let Analytics = this.$injector.get('Analytics')
    let msg       = 'Error loading data'

    let options = {
      service   : 'combinedRequests',
      startDate : this.date.start,
      endDate   : this.date.end
    }
    if (group && group!='all') {
      options.group = group
    }

    this.requests = Analytics.get(options, () => { },
      Flash.create.bind(Flash, 'error', msg)
    )
    options.service = 'layersUsage'
    options.limit   = 10
    this.layers     = Analytics.get(options, () => { },
      Flash.create.bind(Flash, 'error', msg)
    )
  }


  setGroup() {
    let $router = this.$injector.get('$router')
    $router.navigate($router.generate('analytics', { group: this.group}))
  }

}

AnalyticsController.$inject = [ '$injector', '$routeParams', 'Analytics' ]

angular.module('admin_console').controller('AnalyticsController', AnalyticsController)
