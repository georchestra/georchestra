require('components/newUser/newUser.tpl')

require('templates/userForm.tpl')
require('services/translate')

class NewUserController {

  static $inject = [ '$injector', 'translate', 'User' ]

  constructor($injector, translate, User) {
    this.$injector = $injector

    this.user = new User({})
    this.i18n = {}
    translate('user.created' , this.i18n)
    translate('user.error'   , this.i18n)
  }

  save() {
    let flash   = this.$injector.get('Flash')
    let $router = this.$injector.get('$router')
    this.user.$save(
      () => {
        flash.create('success', this.i18n.created)
        $router.navigate($router.generate('user', {
          id  : this.user.uid,
          tab : 'infos'
        }))
      },
      flash.create.bind(flash, 'error', this.i18n.error)
    )
  }

}

angular.module('admin_console').controller('NewUserController', NewUserController)

