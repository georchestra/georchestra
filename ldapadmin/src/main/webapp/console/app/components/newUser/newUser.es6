require('components/newUser/newUser.tpl')

require('templates/userForm.tpl')

class NewUserController {

  static $inject = [ '$injector', 'User' ]

  constructor($injector, User) {
    this.$injector = $injector
    this.flash = this.$injector.get('Flash')

    this.error = $translate('user.error')
    this.success = $translate('user.created')
  }

  save() {
    let $translate = this.$injector.get('$translate')
    this.user.$update(() => {
        this.flash.create.bind(this, 'success', this.success)
      },
      this.flash.create.bind(this, 'error', this.error)
    )
  }

}

angular.module('admin_console').controller('NewUserController', NewUserController)

