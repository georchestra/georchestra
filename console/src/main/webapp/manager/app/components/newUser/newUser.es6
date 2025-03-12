require('components/newUser/newUser.tpl')

require('templates/userForm.tpl')
require('services/translate')
require('services/users')

class NewUserController {
  static $inject = ['$injector', 'translate', 'User']

  constructor ($injector, translate, User) {
    this.$injector = $injector

    this.user = new User({})
    this.i18n = {}
    translate('user.created', this.i18n)
    translate('user.error', this.i18n)

    this.users = User.query()
    this.required = $injector.get('UserRequired').get()
  }

  save () {
    const flash = this.$injector.get('Flash')
    const $router = this.$injector.get('$router')
    this.user.$save(
      () => {
        flash.create('success', this.i18n.created)
        $router.navigate($router.generate('user', {
          id: this.user.uid,
          tab: 'infos'
        }))
      },
      () => { flash.create('danger', this.i18n.error) }
    )
  }
}

angular.module('manager').controller('NewUserController', NewUserController)
