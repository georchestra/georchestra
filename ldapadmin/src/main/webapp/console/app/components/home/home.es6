class HomeController {

  constructor($injector) {

    const LOG_LIMIT = 10;

    this.$injector = $injector;
    let Analytics  = $injector.get('Analytics');
    let $translate = $injector.get('$translate');
    let Flash      = $injector.get('Flash');

    this.requests_conf = [ 'date', 'count' ];
    this.requests = Analytics.get({
        service   : 'layersUsage',
        startDate : '15-01-01',
        endDate   : this.getDate()
      }, function() {},
      Flash.create.bind(this, 'error', $translate('analytics.errorload'))
    )

    this.connected = Analytics.get({
        service   : 'distinctUsers',
        startDate : '15-01-01',
        endDate   : this.getDate()
    // }, function() {},
    }, function() {
      // FAKEDATA
      this.connected = { "results" : [ { "user": "toto", "nb_requests": 520, "organization": "GIP ATGeRI" }, { "user": "toto2", "nb_requests": 250, "organization": "GIP ATGeRI" }, { "user": "toto3", "nb_requests": 221, "organization": "GIP ATGeRI" }, { "user": "toto4", "nb_requests": 221, "organization": "GIP ATGeRI" }, { "user": "toto5", "nb_requests": 215, "organization": "GIP ATGeRI" }, { "user": "toto6", "nb_requests": 209, "organization": "GIP ATGeRI" } ]};
    }.bind(this),
      Flash.create.bind(this, 'error', $translate('analytics.errorload'))
    )

    this.logs = this.$injector.get('Logs').query(
      {
        limit: LOG_LIMIT,
        page: 1
      },
      function() {},
      Flash.create.bind(this, 'error', $translate('analytics.errorload'))
    )
  }

  getDate() {
    let today = new Date()
    return this.$injector.get('$filter')('date')(today, 'yy-MM-dd')
  }
}

HomeController.$inject = [ '$injector' ]


angular.module('admin_console')
.controller('HomeController', HomeController);
