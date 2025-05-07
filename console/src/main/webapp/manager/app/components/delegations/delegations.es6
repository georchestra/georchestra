import 'components/delegations/delegations.tpl'
import 'services/delegations'

class DelegationsController {
  static $inject = ['Delegations', 'Orgs', 'User']

  constructor (Delegations, Orgs, User) {
    this.delegations = Delegations.query()
    this.orgs = {}
    Orgs.query({ logos: false }, (orgs) => orgs.forEach(org => (this.orgs[org.id] = org)))
    this.q = ''
    this.itemsPerPage = 15
    this.users = {}
    User.query(users => {
      this.users = users.reduce((acc, u) => {
        acc[u.uid] = u.sn + ' ' + u.givenName
        return acc
      }, {})
    })
  }
}

angular
  .module('manager')
  .controller('DelegationsController', DelegationsController)
