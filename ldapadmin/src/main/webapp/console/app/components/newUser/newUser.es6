require('components/newUser/newUser.tpl')

require('templates/userForm.tpl')

class NewUserController {

  static $inject = [ '$injector', '$translate', 'User' ]

  constructor($injector, $translate, User) {
    this.$injector = $injector

    this.error   = $translate('user.error')
    this.success = $translate('user.created')
    this.user    = new User({})
    this.toto = { goujon: 'truite'}
  }

  save() {
    let flash = this.$injector.get('Flash')
    this.user.$save(
      flash.create.bind(this, 'success', this.success),
      flash.create.bind(this, 'error', this.error)
    )
  }

}

angular.module('admin_console').controller('NewUserController', NewUserController)

