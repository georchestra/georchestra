require('components/date/date.tpl')

class DateController {

  static $inject = [ '$injector', '$scope', '$element' ]

  constructor($injector, $scope, $element) {

    this.$injector = $injector

    this.options = [ 'day', 'week', 'month', '3month', 'year', 'custom' ].map(
      x => { return { value: x, label: 'date.' + x } }
    )
    this.option = this.options[this.options.length - 2]

    $scope.$watch('date.model.start', (newVal, oldVal) => {
      if (!newVal || this.option.value == 'custom') { return }
      this.option = this.options.filter(
        x => this.$injector.get('date').getFromDiff(x.value) == newVal
      )[0]
    })

    // Reload on custom date changes
    let dateChanged = (val, old) => {
      if (this.option.value == 'custom' && val != old) { this.callback() }
    }
    $scope.$watch('date.model.start' , dateChanged)
    $scope.$watch('date.model.end'   , dateChanged)

    $element.find('.input-daterange').datepicker({
      format: 'yyyy-mm-dd'
    })
  }

  change() {
    if (this.option.value !== 'custom') {
      this.model.start = this.$injector.get('date')
        .getFromDiff(this.option.value)
    }
    this.callback()
  }

}


angular.module('admin_console').component('date', {
  bindings     : {
    model      : '=',
    callback   : '&',
  },
  controller   : DateController,
  controllerAs : 'date',
  templateUrl  : 'components/date/date.tpl.html'
})
