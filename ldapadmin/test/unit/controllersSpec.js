'use strict';

/* jasmine specs for controllers go here */

describe('controllers', function(){
  beforeEach(module('ldapadmin.controllers'));
  beforeEach(module('ldapadmin.services'));

  describe('FooCtrl', function() {
    it('should create foo', inject(function($rootScope, $controller) {
      var scope = $rootScope.$new();
      var ctrl = $controller("FooCtrl", {$scope: scope});
      expect(scope.foo).toEqual('bar');
    }));
  });

  describe('UsersCtrl', function() {
    var $httpBackend,
        scope,
        ctrl;
    beforeEach(inject(function(_$httpBackend_, $rootScope, $controller) {
      $httpBackend = _$httpBackend_;
      $httpBackend.expectGET('data/users/all.json').
        respond([
            {
                "id": 0,
                "picture": "http://placehold.it/32x32",
                "name": "Patrica Barton",
                "company": "Tellifly",
                "email": "patricabarton@tellifly.com",
                "group": []
            },
            {
                "id": 1,
                "picture": "http://placehold.it/32x32",
                "name": "James Mcgee",
                "company": "Blurrybus",
                "email": "jamesmcgee@blurrybus.com",
                "group": []
            }
          ]);
      scope = $rootScope.$new();
      ctrl = $controller('UsersCtrl', {$scope: scope});
    }));
    it('should create groups with 5 items', inject(function($rootScope, $controller) {
      $httpBackend.flush();
      expect(scope.groups.length).toEqual(5);
    }));
  });
});
