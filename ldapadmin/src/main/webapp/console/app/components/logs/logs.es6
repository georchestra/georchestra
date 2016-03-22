require('components/logs/logs.tpl')

class LogsController {

  constructor($injector, $routeParams) {

    this.itemsPerPage = 5
    this.$injector    = $injector

    let msg   = 'Error while loading data'

    this.logs    = $injector.get('Logs').query({
      limit: 100000,
      page: 0
    }, () => {
      this.senders = [ ...new Set(this.logs.logs.map(l => l.admin)) ]
      this.types   = [ ...new Set(this.logs.logs.map(l => l.type)) ]
    }, $injector.get('Flash').create.bind(this, 'error', msg, ''))

  }

}

LogsController.$inject = [ '$injector' ]

angular.module('admin_console').controller('LogsController', LogsController)
