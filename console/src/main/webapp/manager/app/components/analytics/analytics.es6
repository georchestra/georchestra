require('components/analytics/analytics.tpl')
require('components/date/date')

require('services/analytics')

class AnalyticsController {
  constructor ($injector, $routeParams) {
    this.$injector = $injector
    this.i18n = {}
    this.$injector.get('translate')('analytics.all', this.i18n)

    this.role = $routeParams.role || 'all'
    this.roles = this.$injector.get('Role').query(() => {
      this.roles = [ { cn: 'all' } ].concat(this.roles).map(g => {
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
      extractions: [ 'layer', 'count' ]
    }

    this.load((this.role !== 'all') ? this.role : undefined)
  }

  load (role) {
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
    if (role && role !== 'all') {
      options.role = role
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

    this.extractions = Analytics.get(extractionOptions, () => {}, err)
    this.extractionOptions = { ...extractionOptions }
    delete this.extractionOptions.limit
    this.extractionOptions.service = 'layersExtraction.csv'
  }

  setRole () {
    let $router = this.$injector.get('$router')
    $router.navigate($router.generate('analytics', { role: this.role }))
  }
}

AnalyticsController.$inject = [ '$injector', '$routeParams', 'Analytics' ]

angular.module('manager').controller('AnalyticsController', AnalyticsController)
