require('components/analytics/analytics.tpl')

require('services/analytics')

class AnalyticsController {

  constructor($injector, $routeParams) {

    this.$injector = $injector

    this.group = $routeParams.group
    this.groups = this.$injector.get('Group').query()

    this.startDate  = moment().format('YY-MM-DD')
    this.endDate    = moment().format('YY-MM-DD')

    this.intervals = [
      'day', 'week', 'month', '3month', 'year'
    ].map(x => { return { value: x, label: 'analytics.' + x } })
    this.interval = this.intervals[0]

    this.data = {}
    this.config = {
      layers   : [ 'layer', 'count' ],
      requests : [ 'date', 'count' ]
    }

    this.load((this.group != 'all') ? this.group : undefined)
  }

  load(group) {
    let Flash = this.$injector.get('Flash')
    let Analytics = this.$injector.get('Analytics')
    let $translate = this.$injector.get('$translate')

    let options = {
      service   : 'combinedRequests',
      startDate : this.startDate,
      endDate   : this.endDate
    }
    if (group) {
      options.group = group
    }

    this.data.layers = Analytics.get(options, function() {},
      Flash.create.bind(this, 'error', $translate('analytics.errorload'))
    )
    options.service = 'layersUsage'
    this.data.requests = Analytics.get(options, function() {},
      Flash.create.bind(this, 'error', $translate('analytics.errorload'))
    )
  }

  setInterval() {
    this.startDate = this.$injector.get('Util')
        .getDateFromDiff(this.interval.value)
    this.load()
  }

  setGroup() {
    let $router = this.$injector.get('$router')
    $router.navigate($router.generate('analytics', { group: this.group}))
  }

}

AnalyticsController.$inject = [ '$injector', '$routeParams', 'Analytics' ]

angular.module('admin_console').controller('AnalyticsController', AnalyticsController)
