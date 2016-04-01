require('components/date/date.tpl')

class DateController {

  static $inject = [ '$injector', '$scope' ]

  constructor($injector, $scope) {
    this.$injector = $injector
    this.options = [ 'day', 'week', 'month', '3month', 'year' ].map(
      x => { return { value: x, label: 'date.' + x } }
    )
    this.option = this.options[this.options.length - 1]

    $scope.$watch('date.model.start', (newVal, oldVal) => {
      if (!newVal) { return }
      this.option = this.options.filter(
        x => this.$injector.get('Util').getDateFromDiff(x.value) == newVal
      )[0]
    })
  }

  change() {
    this.model.start = this.$injector.get('Util')
      .getDateFromDiff(this.option.value)
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
