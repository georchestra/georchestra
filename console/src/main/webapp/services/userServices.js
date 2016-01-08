var userServices = angular.module('userServices', ['ngResource']);

userServices.factory('User',
  ['$resource', 'LDAP_BASE_URI', function($resource, baseUri){
    return $resource(baseUri + 'users', {}, {
      query: {
        cache: true,
        method:'GET',
        // params:{userId:'users'},
        isArray:true
      }
    });
  }]
);

userServices.factory('Group',
  ['$resource', 'LDAP_BASE_URI', function($resource, baseUri){
    return $resource(baseUri + 'groups', {}, {
      query: {
        cache: true,
        method:'GET',
        // params:{userId:'users'},
        isArray:true
      }
    });
  }]
);
