angular.module('manager')
  .factory('translate', ['$translate', ($translate) => (str, dict) => {
    const promise = $translate(str)
    if (dict) {
      promise.then((v) => {
        dict[str.split('.')[1]] = v
        return v
      })
    }
    return promise
  }])
