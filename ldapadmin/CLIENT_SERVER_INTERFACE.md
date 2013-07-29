This file describes the interface between client and server.
It lists the different requests and responses.

## User

### CREATE

Request

    POST users/:userId

Body parameters

    {
        "facsimileTelephoneNumber": "fsdfdf"
        "givenName": "GIRAUD"
        "l": "dfdf"
        "mail": "pierre.giraud@gmail.com"
        "postOfficeBox": "dfdf"
        "postalCode": "dfdf"
        "sn": "Pierre"
        "street": "fdsf"
        "telephoneNumber": "fdsfd"
    }

Response

    {
        "telephoneNumber": "fdsfd",
        "postOfficeBox": "dfdf",
        "uid": "2a07825a-b36a-4ca2-8465-83dfa31a64c4",
        "facsimileTelephoneNumber": "fsdfdf",
        "l": "dfdf",
        "street": "fdsf",
        "sn": "Pierre",
        "postalCode": "dfdf",
        "mail": "pierre.giraud@gmail.com",
        "givenName": "GIRAUD"
    }
    or
    { "success": false }

### READ (Complete list of users)

Request

    GET users

Response

    [
        {
            "o": "Zogak",
            "givenName": "Walsh",
            "sn": "Atkins",
            "groups": ["5",
            "1"],
            "uid": "de840827-f31e-4bfa-80a2-a3b18b2b15e2"}
        }, 
            ...
    ]

### READ

Request

    GET users/:userId

Response

    {
        "uid": "bf303a79-ca65-4866-b69b-2924f338a9b5",
        "street": "Brooklyn Avenue",
        "homePostalAddress": "Crawford Avenue 10622 Woodlake",
        "title": "",
        "facsimileTelephoneNumber": "(997) 508-3901",
        "postalCode": 11847,
        "mail": "alfredadowns@bitendrex.com",
        "postalAddress": "",
        "postOfficeBox": 47,
        "description": "ut velit ut aliquip eiusmod ea deserunt nisi incididunt ut dolor ut cupidatat sint quis consequat consequat cupidatat aliqua occaecat esse sunt labore dolore voluptate excepteur reprehenderit velit anim labore sunt proident est ea deserunt deserunt id incididunt ut excepteur nisi aute consectetur anim consectetur dolore culpa velit sunt ex",
        "groups": ["5"],
        "homePhone": "(816) 450-3050",
        "telephoneNumber": "(907) 441-2066",
        "physicalDeliveryOfficeName": "",
        "mobile": "(853) 482-2492",
        "roomNumber": 128,
        "l": "Kipp",
        "o": "Xanide",
        "st": "Montana",
        "sn": "Downs",
        "ou": "Bitendrex",
        "givenName": "Alfreda"
    }

### UPDATE

Request

    PUT users/:userId

Body parameters

    {
        "facsimileTelephoneNumber": "fsdfdf"
        "givenName": "GIRAUD"
        "l": "dfdf"
        "mail": "pierre.giraud@gmail.com"
        "postOfficeBox": "dfdf"
        "postalCode": "dfdf"
        "sn": "Pierre"
        "street": "fdsf"
        "telephoneNumber": "fdsfd"
    }

Response

    200 OK

### DELETE

Request

    DELETE users/:userId

Response

    200 OK

## GROUP

### CREATE

Create group

Request

    POST groups

Body parameters

    {
        "name": "Name of the group"
        "description": "Description for the group"
    }

Reponse

    {
        "uid": "134d42ab-923d-4c11-a131-4106f1fa8acd",
        "name": "Name of the group",
        "description": "Description for the group"
    }
    or
    { "success": false }

### READ

Request

    GET groups

Response

    [{
        "uid": "4fa3d947-a7e3-4fcc-962b-aa69d211230c",
        "name": "Administrator"
    }, {
        "uid": "134d42ab-923d-4c11-a131-4106f1fa8acd",
        "name": "Name of the group",
        "description": "Description for the group"
    }]

### UPDATE

Modify group

Request

    PUT groups/:groupId

Body parameters

    {
        "name": "New name of the group"
        "description": "Modified description for the group"
    }

Reponse

    200 OK

### DELETE

Delete group

Request

    DELETE groups/:groupId

Response

    200 OK

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
