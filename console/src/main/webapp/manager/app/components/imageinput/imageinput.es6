require('components/imageinput/imageinput.tpl')

class ImageinputController {
  static $inject = [ '$element', '$scope' ]
  constructor ($element, $scope) {
    this.$element = $element
    this.$scope = $scope
  }

  $onInit () {
    if (!window.FileReader) {
      this.$element.hide()
    }
    const preview = this.$element.find('.preview')[0]
    const reader = new window.FileReader()
    const fileinput = this.$element.find('.image-input')

    fileinput.on('change', () => {
      const file = fileinput[0].files[0]
      reader.addEventListener('load', () => {
        preview.src = reader.result
        this.$scope.$apply(() => {
          this.model[this.attribute] = reader.result.split(',')[1]
        })
      })
      file && reader.readAsDataURL(file)
    })
  }

  delete() {
    this.model[this.attribute] = ''
  }
}

angular.module('manager').component('imageinput', {
  bindings: {
    model: '=',
    attribute: '='
  },
  controller: ImageinputController,
  controllerAs: 'imageinput',
  templateUrl: 'components/imageinput/imageinput.tpl.html'
})
