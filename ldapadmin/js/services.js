'use strict';

angular.module('ldapadminServices', ['ngResource']).
    factory('Users', function($resource){
  return $resource('data/users/:userId.json', {}, {
    query: {method:'GET', params:{userId:'all'}, isArray:true}
  });
});
