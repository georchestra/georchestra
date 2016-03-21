require('components/logs/logs.tpl')

// require('services/logs')

class LogsController {

  constructor($injector, $routeParams) {

    this.$injector = $injector

  }

}

LogsController.$inject = [ '$injector' ]

angular.module('admin_console').controller('LogsController', LogsController)
