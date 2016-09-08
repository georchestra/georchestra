angular.module('admin_console')
.factory('translate', [ '$translate' , ($translate) => (str, dict) => {
  let promise = $translate(str)
  if (dict) {
    promise.then((v) => dict[str.split('.')[1]] = v)
  }
  return promise
}])

