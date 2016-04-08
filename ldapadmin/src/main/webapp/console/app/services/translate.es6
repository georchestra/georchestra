angular.module('admin_console')
.factory('translate', [ '$translate' , ($translate) => (str, dict) => {
  $translate(str).then((v) => dict[str.split('.')[1]] = v)
}])

