'use strict';

angular.module('ldapadmin.services', ['ngResource']).
    factory('Users', function($resource){
  return $resource('data/users/:userId.json', {}, {
    query: {method:'GET', params:{userId:'all'}, isArray:true}
  });
});
