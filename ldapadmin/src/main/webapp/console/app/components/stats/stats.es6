class StatsController {

  constructor($element, $scope) {
    this.data.$promise.then(this.initialize.bind(this, $element, $scope));
  }

  initialize($element, $scope) {
    var options;
    if (this.type=='bar') { // FAKEDATA
      this.data = {"results": [ { "count": 205, "layer": "cigal:cigal_lignesfrontieres_250000_utm32" }, { "count": 174, "layer": "gn:ne_50m_coastline" }, { "count": 170, "layer": "gn:ne_50m_boundary_lines_land" }, { "count": 152, "layer": "gn:ne_50m_boundary_da" }, { "count": 151, "layer": "gn:world" }, { "count": 64, "layer": "default_pmauduit:ign_pleiade_test_tif_l93" }, { "count": 59, "layer": "osm:google" }, { "count": 58, "layer": "default_pmauduit:73-savoie" }, { "count": 55, "layer": "pmauduit:cigal_pleiade_colmar_2014_tif_l93" }, { "count": 22, "layer": "test_layer_group" } ]};
    } else {
      this.data = { "granularity": "WEEK", "results": [ { "count": 653, "date": "2015-01" }, { "count": 864, "date": "2015-02" }, { "count": 136, "date": "2015-03" }, { "count": 6, "date": "2015-04" }, { "count": 254, "date": "2015-05" }, { "count": 90, "date": "2015-06" }, { "count": 90, "date": "2015-07" }, { "count": 198, "date": "2015-08" }, { "count": 145, "date": "2015-09" }, { "count": 3, "date": "2015-10" }, { "count": 12, "date": "2015-11" }, { "count": 17, "date": "2015-12" }, { "count": 266, "date": "2015-13" }, { "count": 330, "date": "2015-14" }, { "count": 324, "date": "2015-15" }, { "count": 507, "date": "2015-16" } ]};
    }
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
        let data;
        if (this.type=='bar') { // FAKEDATA
          data = {"results": [ { "count": 205, "layer": "cigal:cigal_lignesfrontieres_250000_utm32" }, { "count": 174, "layer": "gn:ne_50m_coastline" }, { "count": 170, "layer": "gn:ne_50m_boundary_lines_land" }, { "count": 152, "layer": "gn:ne_50m_boundary_da" }, { "count": 151, "layer": "gn:world" }, { "count": 64, "layer": "default_pmauduit:ign_pleiade_test_tif_l93" }, { "count": 59, "layer": "osm:google" }, { "count": 58, "layer": "default_pmauduit:73-savoie" }, { "count": 55, "layer": "pmauduit:cigal_pleiade_colmar_2014_tif_l93" }, { "count": 22, "layer": "test_layer_group" } ]};
        } else {
          data = { "granularity": "WEEK", "results": [ { "count": 653, "date": "2015-01" }, { "count": 864, "date": "2015-02" }, { "count": 136, "date": "2015-03" }, { "count": 6, "date": "2015-04" }, { "count": 254, "date": "2015-05" }, { "count": 90, "date": "2015-06" }, { "count": 90, "date": "2015-07" }, { "count": 198, "date": "2015-08" }, { "count": 145, "date": "2015-09" }, { "count": 3, "date": "2015-10" }, { "count": 12, "date": "2015-11" }, { "count": 17, "date": "2015-12" }, { "count": 266, "date": "2015-13" }, { "count": 330, "date": "2015-14" }, { "count": 324, "date": "2015-15" }, { "count": 507, "date": "2015-16" } ]};
        }
        data.results.pop()
        this.lines.update({
          labels: data.results.map(x => x[this.config[0]]),
          series: [data.results.map(x => x[this.config[1]])]
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
