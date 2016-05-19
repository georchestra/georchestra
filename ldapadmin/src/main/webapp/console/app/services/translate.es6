angular.module('admin_console')
.factory('translate', [ '$translate' , ($translate) => (str, dict) => {
  let promise = $translate(str)
  promise.then((v) => dict[str.split('.')[1]] = v)
  return promise
}])

