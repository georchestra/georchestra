'use strict';

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

describe('my app', function() {

  beforeEach(function() {
    browser().navigateTo('../../app/index.html');
  });


  it('should automatically redirect to /users when location hash/fragment is empty', function() {
    expect(browser().location().url()).toBe("/users");
  });


  describe('users', function() {

    beforeEach(function() {
      browser().navigateTo('#/users');
    });

    it('should render a list of 1000 users', function() {
      expect(repeater('.users tr').count()).toEqual(1000);
    });
    it('should sort the list of users', function() {
      expect(repeater('.users tr').row(0)).toEqual(["Abby Watkins","Rodeocean"]);
    });
  });

  describe('group', function() {

    it('should render a list of 89 users', function() {
      browser().navigateTo('#/groups/Administrator');
      expect(repeater('.users tr').count()).toEqual(89);
    });

    it('should select a group', function() {
      element('.groups div:nth-child(2) a').click();
      expect(browser().location().url()).toBe('/groups/SV_XXX');
      expect(repeater('.users tr').count()).toEqual(166);
    });
  });

  describe('create user', function() {
    it('should show the show the edit view', function() {
      element('#new_user').click();
      expect(browser().location().url()).toBe('/users/new');
      expect(input('user.name').val()).toBe('');

      input('user.name').enter('toto');
      input('user.email').enter('toto@mail.zzz');
      element('.save').click();
      expect(repeater('.users tr').count()).toEqual(1001);
    });
  });

  describe('delete user', function() {
    it('should show the show the edit view', function() {
      element('.users tr:nth-child(1) td a').click();
      element('.delete').click();
      expect(repeater('.users tr').count()).toEqual(1000);
    });
  });
});
