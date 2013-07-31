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

    it('should render a list of 25 users', function() {
      expect(repeater('.users tr').count()).toEqual(25);
    });
    it('should sort the list of users', function() {
      expect(repeater('.users tr').row(0)).toEqual(["Downs","Alfreda","Xanide"]);
    });
  });

  describe('group', function() {

    it('should render a list of 3 users', function() {
      browser().navigateTo('#/groups/Administrator');
      expect(repeater('.users tr').count()).toEqual(4);
    });

    it('should select a group', function() {
      element('.groups div:nth-child(2) a').click();
      expect(browser().location().url()).toBe('/groups/SV_YYY');
      expect(repeater('.users tr').count()).toEqual(3);
    });
  });

  describe('create user', function() {
    it('should show the show the edit view', function() {
      element('#new_user').click();
      expect(browser().location().url()).toBe('/users/new');
      expect(input('user.givenName').val()).toBe('');

      input('user.givenName').enter('Dupont');
      input('user.sn').enter('toto');
      input('user.mail').enter('toto@mail.zzz');
      element('.save').click();
      expect(repeater('.users tr').count()).toEqual(26);
    });
  });

  describe('delete user', function() {
    it('should show the show the edit view', function() {
      element('.users tr:nth-child(1) td a').click();
      element('.delete').click();
      expect(repeater('.users tr').count()).toEqual(25);
    });
  });

  describe('select users', function() {
    it('should select all users', function() {
      browser().navigateTo('#/groups/Administrator');

      // check the check all checkbox
      element('#checkAll').click();
      expect(element('.users input[type=checkbox]:checked').count()).toEqual(4);
      expect(element('#checkAll').prop('indeterminate')).toBeFalsy();
      expect(element('.btn.groups').attr('class')).not().toContain('hide');

      // uncheck the check all checkbox
      element('#checkAll').click();
      expect(element('.users input[type=checkbox]:checked').count()).toEqual(0);
      expect(element('.btn.groups').attr('class')).toContain('hide');

      // manually select some users
      element('.users tr:nth-child(1) input[type=checkbox]').click();
      element('.users tr:nth-child(2) input[type=checkbox]').click();
      expect(element('#checkAll').prop('indeterminate')).toBeTruthy();
      expect(element('.btn.groups').attr('class')).not().toContain('hide');

      // add those two users to the SV_YYY group (one should already be part of
      // it)
      element('.btn.groups').click();
      expect(element('.dropdown-menu.groups>li:last-child').attr('class')).toContain('disabled');
      element('ul.groups li:nth-child(3) a', 'SV_YYY group item').click();
      element('.dropdown-menu.groups>li:last-child a:eq(0)').click();
      browser().navigateTo('#/groups/SV_YYY');
      expect(repeater('.users tr').count()).toEqual(5);
    });
  });
  describe('create group', function() {
    it('should show the group edit view', function() {
      element('#new_group').click();
      expect(browser().location().url()).toBe('/groups/new');
      expect(input('group.cn').val()).toBe('');

      input('group.cn').enter('Name for group');
      element('.save').click();
      expect(repeater('.groups a').count()).toEqual(9);
    });
  });
  describe('delete group', function() {
    it('should remove the group', function() {
      browser().navigateTo('#/groups/SV_YYY');

      element('.delete').click();
      expect(repeater('.groups a').count()).toEqual(8);
    });
  });
});
