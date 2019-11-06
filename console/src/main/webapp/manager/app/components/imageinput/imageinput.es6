require('components/imageinput/imageinput.tpl')

class ImageinputController {
  static $inject = [ '$element', '$scope' ]
  constructor ($element, $scope) {
    this.$element = $element
    this.$scope = $scope
  }

  $onInit () {
    if (!window.FileReader) return
    const reader = new window.FileReader()
    const fileinput = this.$element.find('.image-input')

    fileinput.on('change', () => {
      const file = fileinput[0].files[0]
      reader.addEventListener('load', () =>
        this.$scope.$apply(() => this.setValue(reader.result.split(',')[1]))
      )
      file && reader.readAsDataURL(file)
    })
    // Initial value
    const setValue = () => (this.value = this.model ? this.model[this.attribute] : null)
    if (this.model.$promise) this.model.$promise.then(setValue)
    else setValue()
  }

  delete () {
    this.setValue('')
  }

  setValue (value) {
    this.value = value
    if (this.model) this.model[this.attribute] = value
    if (this.target) document.querySelector(this.target).value = value
  }
}

angular.module('manager').component('imageinput', {
  bindings: {
    model: '=',
    attribute: '=',
    target: '='
  },
  controller: ImageinputController,
  controllerAs: 'imageinput',
  templateUrl: 'components/imageinput/imageinput.tpl.html'
})
