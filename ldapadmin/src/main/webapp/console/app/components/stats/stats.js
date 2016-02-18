angular.module('admin_console')
.component('stats', {
  bindings    : {
    data   : '=',
    type   : '=',
    config : '=',
    title  : '='
  },
  controller  : StatsController,
  templateUrl : 'components/stats/stats.tpl.html'
});

function StatsController($element) {
  var options;
  this.labels = this.data.map(x => x[this.config[0]]);
  this.series = [this.data.map(x => x[this.config[1]])];

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
      axisX: {
        labelInterpolationFnc: function(value, index) {
          return (parseInt(value.split('-')[1]) % 3 == 1) ? value : null;
        }
      }
    };
  }
  var lines = new Chartist[this.type=='bar' ? 'Bar' : 'Line'](
    $element.find('.chartist')[0],
    {
      labels: this.labels,
      series: this.series,
    },
    options
  );
  this.view = 'graph';
}

StatsController.$inject = [ '$element' ];

StatsController.prototype.switchView = function() {
  this.view = (this.view=='graph') ? 'table' : 'graph';
}
