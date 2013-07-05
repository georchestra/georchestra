This file describes the interface between client and server.
It lists the different requests and responses.

## User

### CREATE

Request

    POST users/:userId?name=...&email=...

Response

    { "success": true }
    or
    { "success": false }

### READ (Complete list of users)

Request

    GET users

Response

    [
        {
            "id": 0,
            "picture": "http://placehold.it/32x32",
            "name": "Patrica Barton",
            "company": "Tellifly",
            "email": "patricabarton@tellifly.com",
            "group": ['Administrator', 'groupA']
        },
        {
            "id": 1,
            "picture": "http://placehold.it/32x32",
            "name": "James Mcgee",
            "company": "Blurrybus",
            "email": "jamesmcgee@blurrybus.com",
            "group": ['groupA', 'groupB']
        },
        [...]
    ]

### READ

Request

    GET users

Response

    {
      "name": "toti",
      "email": "toti@domaine.com",
      "company": "the Company"
    }

### UPDATE

Request

    PUT users/:userId?name=...&email=...

Response

    { "success": true }
    or
    { "success": false }

### DELETE

Request

    DELETE users/:userId

Response

    { "success": true }
    or
    { "success": false }

## GROUP

### CREATE

Create group

Request

    POST groups?name=...&description=...

Reponse

    { "success": true }
    or
    { "success": false }

### READ

Request

    GET groups

Response

    [
        {
            "name": "Administrator"
        },
        {
            "name": "group_A"
        }
    ]

### DELETE

Delete group

Request

    DELETE groups/:groupId

Response

    { "success": true }
    or
    { "success": false }

## Group users

### UPDATE

Add users to the given group

Request

    PUT groups/:groupId/users/:[userId0..userIdN]

Response

    { "success": true }
    or
    { "success": false }

### DELETE

Remove users from the given group

Request

    DELETE groups/:groupId/users/:[userId0..userIdN]

Response

    { "success": true }
    or
    { "success": false }
