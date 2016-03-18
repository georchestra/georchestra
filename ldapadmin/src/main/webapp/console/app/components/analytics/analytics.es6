require('components/analytics/analytics.tpl')

require('services/analytics')
class AnalyticsController {

  constructor($injector, $routeParams) {

    this.$injector = $injector

    let startDate  = moment().format('YY-MM-DD')
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

    this.load(startDate)

  }

  load(startDate) {
    let Flash = this.$injector.get('Flash')
    let Analytics = this.$injector.get('Analytics')
    let $translate = this.$injector.get('$translate')

    let options = {
      service   : 'combinedRequests',
      startDate : startDate,
      endDate   : this.endDate
    }
    this.data.layers = Analytics.get(options, function() {},
      Flash.create.bind(this, 'error', $translate('analytics.errorload'))
    )
    options.service = 'layersUsage'
    this.data.requests = Analytics.get(options, function() {},
      Flash.create.bind(this, 'error', $translate('analytics.errorload'))
    )
  }

  activate($scope) {
    $scope.$watch('analytics.interval', (newVal, oldVal) => {
      if (newVal == oldVal) { return; }
      this.load(this.$injector.get('Util').getDateFromDiff(newVal.value))
    })
  }

}

AnalyticsController.$inject = [ '$injector', '$routeParams', 'Analytics' ]
AnalyticsController.prototype.activate.$inject = [ '$scope' ]

angular.module('admin_console').controller('AnalyticsController', AnalyticsController)
