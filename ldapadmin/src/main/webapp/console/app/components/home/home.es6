class HomeController {

  constructor($injector) {

    const LOG_LIMIT = 10

    this.PENDING   = 'PENDING'
    this.EXPIRED   = 'SV'

    this.$injector = $injector;

    $injector.get('Group').query(groups => {
      groups.forEach(group => {
        if (group.cn == this.PENDING) {
          this.pending = group
        }
        if (group.cn == this.EXPIRED) {
          this.expired = group
        }
      })
    })

    let err_handler = $injector.get('Flash').create.bind(
      this, 'error', $injector.get('$translate')('analytics.errorload')
    )

    this.requests_conf = [ 'date', 'count' ];
    this.requests = $injector.get('Analytics').get({
        service   : 'layersUsage',
        startDate : '15-01-01',
        endDate   : this.getDate()
      }, function() {}, err_handler
    )

    this.connected = $injector.get('Analytics').get({
        service   : 'distinctUsers',
        startDate : '15-01-01',
        endDate   : this.getDate()
    // }, function() {},
    }, function() {
      // FAKEDATA
      this.connected = { "results" : [ { "user": "toto", "nb_requests": 520, "organization": "GIP ATGeRI" }, { "user": "toto2", "nb_requests": 250, "organization": "GIP ATGeRI" }, { "user": "toto3", "nb_requests": 221, "organization": "GIP ATGeRI" }, { "user": "toto4", "nb_requests": 221, "organization": "GIP ATGeRI" }, { "user": "toto5", "nb_requests": 215, "organization": "GIP ATGeRI" }, { "user": "toto6", "nb_requests": 209, "organization": "GIP ATGeRI" } ]};
    }.bind(this),
      err_handler
    )

    this.logs = this.$injector.get('Logs').query(
      {
        limit: LOG_LIMIT,
        page: 1
      },
      function() {}, err_handler
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
