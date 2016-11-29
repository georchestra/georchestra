require('components/analytics/analytics.tpl')
require('components/date/date')

require('services/analytics')

class AnalyticsController {

  constructor ($injector, $routeParams) {
    this.$injector = $injector
    this.i18n = {}
    this.$injector.get('translate')('analytics.all', this.i18n)

    this.group = $routeParams.group || 'all'
    this.groups = this.$injector.get('Group').query(() => {
      this.groups = [ { cn: 'all' } ].concat(this.groups).map(g => {
        g.label = this.i18n[g.cn] || g.cn
        return g
      })
    })
    let date = this.$injector.get('date')

    this.date = {
      start: date.getDefault(),
      end: date.getEnd()
    }

    this.data = {}
    this.config = {
      layers: [ 'layer', 'count' ],
      requests: [ 'date', 'count' ],
      extraction: [ 'layer', 'count' ]
    }

    this.load((this.group !== 'all') ? this.group : undefined)
  }

  load (group) {
    let i18n = {}
    this.$injector.get('translate')('analytics.errorload', i18n)
    this.$injector.get('translate')('users.roleUpdateError', i18n)
    let Flash = this.$injector.get('Flash')
    let Analytics = this.$injector.get('Analytics')
    let err = Flash.create.bind(Flash, 'danger', i18n.errorload)

    let options = {
      service: 'combinedRequests',
      startDate: this.date.start,
      endDate: this.date.end
    }
    if (group && group !== 'all') {
      options.group = group
    }

    this.requests = Analytics.get(options, () => {}, err)

    let usageOptions = {
      ...options,
      service: 'layersUsage.json',
      limit: 10
    }

    this.layers = Analytics.get(usageOptions, () => {}, err)

    this.usageOptions = { ...usageOptions }
    delete this.usageOptions.limit
    this.usageOptions.service = 'layersUsage.csv'

    let extractionOptions = {
      ...options,
      service: 'layersExtraction.json',
      limit: 10
    }

    this.extraction = Analytics.get(extractionOptions, () => {}, err)
    this.extractionOptions = { ...extractionOptions }
    delete this.extractionOptions.limit
    this.extractionOptions.service = 'layersExtraction.csv'
  }

  setGroup () {
    let $router = this.$injector.get('$router')
    $router.navigate($router.generate('analytics', { group: this.group }))
  }

}

AnalyticsController.$inject = [ '$injector', '$routeParams', 'Analytics' ]

angular.module('admin_console').controller('AnalyticsController', AnalyticsController)
