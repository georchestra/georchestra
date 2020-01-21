require('components/logs/logs.tpl')

class LogsController {
  static $inject = ['$injector']

  constructor ($injector) {
    this.$injector = $injector
  }
}

angular
  .module('manager')
  .controller('LogsController', LogsController)
