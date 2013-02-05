LDAPADMIN
=========

A webapp with a public interface and a private one, which allows to manage users and groups.

All strings and templates should take into account i18n (3 langs by default: en/es/fr)

Public UI
---------

The public UIs will be accessed from the CAS login page, by the way of text links: "lost password ?" and "create account".

These pages should by light (no need to ship ExtJS).

### Lost Password

The page asks for user email.

If the given email matches one of the LDAP users, an email is sent to this user with a new **strong** password.
From this moment on, and for a configurable delay (say, one day by default), both passwords (old & new) will be considered as valid.

During this period:
 * if the user logs in with the new password, the old password is discarded and replaced by the new one.
 * if the user logs in with the old password, the new password is discarded.

If the user does not log in during this period, the new password is discarded.

If the given email does not match one from the LDAP, nothing happens (and no specific message is sent to the requestor - this is to prevent automated email recovery)

### Create account

The page shows a form with typical fields: name, org, role, geographic area, email, phone nb, details.

Once submitted, the form disappears and a (configurable) message says something like "Your request has been submitted to an administrator, and should be taken into account in the next hours"

Private UI
----------

The private UI will be available at /ldapadmin for members of SV_ADMIN and SV_ADMIN_USERS only.


Notes
-----

All emails sent by the application should be configurable by the way of templates, as for extractorapp.