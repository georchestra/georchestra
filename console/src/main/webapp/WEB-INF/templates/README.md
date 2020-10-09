# Console email templates

This folder hosts email templates which are sent to users and platform administrators at various times.

Either account creation is moderated or it is not.
This is set with the `moderatedSignup` variable from the [console.properties](/console/console.properties) file, which defaults to `true`.

When account creation is moderated:
 * [newaccount-requires-moderation-template.txt](newaccount-requires-moderation-template.txt) is sent to members of the SUPERUSER role and also to users holding a delegation (if any) for the organisation that was declared by the new user.
 * [account-creation-in-progress-template.txt](account-creation-in-progress-template.txt) is sent to the requesting user (this is an ACK mail, the account is pending moderation).
 * [newaccount-was-created-template.txt](newaccount-was-created-template.txt) is sent to the requesting user upon account validation, his account is now active.

When account creation is not moderated:
 * [newaccount-notification-template.txt](newaccount-notification-template.txt) is sent to members of the SUPERUSER role and also to users holding a delegation (if any) for the organisation that was declared by the new user.
 * [newaccount-was-created-template.txt](newaccount-was-created-template.txt) is sent to the requesting user (this is a welcoming email).

In both cases, it's the responsibility of the platform admin to inform the user that his account was granted roles.

[account-uid-renamed.txt](account-uid-renamed.txt) is sent to the user whose login have been modified by a platform admin.

[changepassword-email-template.txt](changepassword-email-template.txt) is sent when the user requests a new password with the "I lost my password" link from the CAS login page.
It is also sent to the user when the administrator triggers a password regeneration from the admin console.
