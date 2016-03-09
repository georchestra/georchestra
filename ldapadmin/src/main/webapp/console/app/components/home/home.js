angular.module('admin_console')
.controller('HomeController', HomeController);

function HomeController($injector) {
  this.$injector = $injector;
  var $translate = this.$injector.get('$translate');
  this.data = {};
  this.config = {
    requests : [ 'date', 'count' ]
  };
  this.data.requests = this.$injector.get('Analytics').get({
      service   : 'layersUsage',
      startDate : '15-01-01',
      endDate   : '16-03-09'
    }, function() {},
    this.$injector.get('Flash').create.bind(this, 'error', $translate('analytics.errorload'))
  );
}

HomeController.$inject = [ '$injector' ];
