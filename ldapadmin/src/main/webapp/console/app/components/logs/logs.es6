require('components/logs/logs.tpl')

// require('services/logs')

class LogsController {

  constructor($injector, $routeParams) {

    const LOG_LIMIT = 10
    this.$injector  = $injector

    let msg   = 'Error while loading data'
    let error = $injector.get('Flash').create.bind(this, 'error', msg, '')

    this.logs = $injector.get('Logs').query({
      limit: LOG_LIMIT,
      page: 0
    }, () => {}, error)

  }

}

LogsController.$inject = [ '$injector' ]

angular.module('admin_console').controller('LogsController', LogsController)
