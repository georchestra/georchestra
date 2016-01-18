var userServices = angular.module('userServices', ['ngResource']);

userServices.factory('User',
  ['$resource', 'LDAP_BASE_URI', function($resource, baseUri){
    return $resource(baseUri + 'users/:id', {}, {
      query: {
        cache: true,
        method:'GET',
        isArray:true
      }
    });
  }]
);
