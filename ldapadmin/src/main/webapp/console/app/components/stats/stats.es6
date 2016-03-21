require('components/stats/stats.tpl')

class StatsController {

  constructor($element, $scope) {
    this.data.$promise.then(this.initialize.bind(this, $element, $scope));
  }

  initialize($element, $scope) {
    var options;
    this.labels = this.data.results.map(x => x[this.config[0]]);
    this.series = [this.data.results.map(x => x[this.config[1]])];

    if (this.type == 'bar') {
      options = {
        seriesBarDistance: 10,
        reverseData: true,
        horizontalBars: true,
        axisY: {
          offset: 200
        }
      };
    } else {
      options = {
        fullWidth: true,
        axisY: {
          offset: 45
        },
        axisX: {
          labelInterpolationFnc: function(value, index) {
            return (parseInt(value.split('-')[1]) % 3 == 1) ? value : null;
          }
        }
      };
    }
    this.lines = new Chartist[this.type=='bar' ? 'Bar' : 'Line'](
      $element.find('.chartist')[0],
      {
        labels: this.labels,
        series: this.series,
      },
      options
    );
    this.view = 'graph';

    $scope.$watch('stats.data', (newVal, oldVal) => {
      if (oldVal == newVal) { return; }
      newVal.$promise.then(() => {
        this.lines.update({
          labels : this.data.results.map(x => x[this.config[0]]),
          series : [ this.data.results.map(x => x[this.config[1]]) ]
        })
      })
    })
  }

  switchView() {
    this.view = (this.view=='graph') ? 'table' : 'graph';
  }

}

StatsController.$inject = [ '$element', '$scope' ];


angular.module('admin_console')
.component('stats', {
  bindings    : {
    data   : '=',
    type   : '=',
    config : '=',
    title  : '='
  },
  controller   : StatsController,
  controllerAs : 'stats',
  templateUrl  : 'components/stats/stats.tpl.html'
})
