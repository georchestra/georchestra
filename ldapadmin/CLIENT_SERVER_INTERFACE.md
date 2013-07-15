This file describes the interface between client and server.
It lists the different requests and responses.

## User

### CREATE

Request

    POST users/:userId

Body parameters

    name=...
    email=...

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

    GET users/:userId

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

    POST groups

Body parameters

    name=...
    description=...

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
            "name": "Administrator",
            "description": "une description"
        },
        {
            "name": "group_A",
            "description": "une description"
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

Add/Remove users to/from the given group

Request

    POST groups_users

Body parameters

    users=userId0,userId1,..,userIdN
    PUT=groupId0,groupId1,..,groupIdN
    DELETE=groupId3,groupId4,..,groupIdM

Response

    { "success": true }
    or
    { "success": false }
