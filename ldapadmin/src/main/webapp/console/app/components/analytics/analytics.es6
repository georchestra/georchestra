require('components/analytics/analytics.tpl')
require('components/date/date')

require('services/analytics')

class AnalyticsController {

  constructor($injector, $routeParams) {

    this.$injector = $injector

    this.group  = $routeParams.group
    this.groups = this.$injector.get('Group').query()
    let date    = this.$injector.get('date')

    this.date = {
      start : date.getFromDiff('year'),
      end   : date.getEnd()
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
    let err       = Flash.create.bind(Flash, 'error', 'Error loading data')

    let options = {
      service   : 'combinedRequests',
      startDate : this.date.start,
      endDate   : this.date.end
    }
    if (group && group!='all') {
      options.group = group
    }

    this.requests   = Analytics.get(options, () => {}, err)
    options.service = 'layersUsage'
    options.limit   = 10
    this.layers     = Analytics.get(options, () => {}, err)
  }


  setGroup() {
    let $router = this.$injector.get('$router')
    $router.navigate($router.generate('analytics', { group: this.group}))
  }

}

AnalyticsController.$inject = [ '$injector', '$routeParams', 'Analytics' ]

angular.module('admin_console').controller('AnalyticsController', AnalyticsController)
