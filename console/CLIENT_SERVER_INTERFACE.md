# Client-server interface

This file describes the interface between client and server.
It lists the different requests and responses.

## User

### CREATE

Request

    POST users

Body parameters

    {
        "facsimileTelephoneNumber": "fsdfdf",
        "givenName": "GIRAUD",
        "l": "dfdf",
        "mail": "pierre.giraud@gmail.com",
        "postalAddress": "dfdf ze5zec ze315",
        "postOfficeBox": "dfdf",
        "postalCode": "dfdf",
        "sn": "Pierre",
        "street": "fdsf",
        "telephoneNumber": "fdsfd",
        "o": "Zogak",
        "title': "dsfds",
        "description': "fdzd z zdsfds"
    }

Response

    {
        "telephoneNumber": "fdsfd",
        "postOfficeBox": "dfdf",
        "uid": "pgiraud",
        "facsimileTelephoneNumber": "fsdfdf",
        "l": "dfdf",
        "street": "fdsf",
        "sn": "Pierre",
        "postalAddress": "dfdf ze5zec ze315",
        "postalCode": "dfdf",
        "mail": "pierre.giraud@gmail.com",
        "givenName": "GIRAUD",
        "o": "Zogak",
        "title': "dsfds",
        "description': "fdzd z zdsfds"
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
            "uid": "watkins"
        },
            ...
    ]

### READ

Request

    GET users/:uid

Response

    {
        "uid": "pgiraud",
        "street": "Brooklyn Avenue",
        "homePostalAddress": "Crawford Avenue 10622 Woodlake",
        "title": "",
        "facsimileTelephoneNumber": "(997) 508-3901",
        "postalCode": 11847,
        "mail": "alfredadowns@bitendrex.com",
        "postalAddress": "",
        "postOfficeBox": 47,
        "description": "ut velit ut aliquip eiusmod ea deserunt nisi incididunt ut dolor ut cupidatat sint quis consequat consequat cupidatat aliqua occaecat esse sunt labore dolore voluptate excepteur reprehenderit velit anim labore sunt proident est ea deserunt deserunt id incididunt ut excepteur nisi aute consectetur anim consectetur dolore culpa velit sunt ex",
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

    PUT users/:uid

Body parameters

    {
        "facsimileTelephoneNumber": "fsdfdf"
        "givenName": "GIRAUD"
        "l": "dfdf"
        "mail": "pierre.giraud@gmail.com"
        "postalAddress": "dfdf ze5zec ze315",
        "postOfficeBox": "dfdf"
        "postalCode": "dfdf"
        "sn": "Pierre"
        "street": "fdsf"
        "telephoneNumber": "fdsfd",
        "o": "Zogak",
        "title': "dsfds",
        "description': "fdzd z zdsfds"
    }

Note: *Fields that are not present in the parameters should remain untouched
on the server.*

Response

    200 OK

### DELETE

Request

    DELETE users/:uid

Response

    200 OK

## GROUP

### CREATE

Create role

Request

    POST roles

Body parameters

    {
        "cn": "Name of the role",
        "description": "Description for the role"
    }

Reponse

    {
        "cn": "Name of the role",
        "description": "Description for the role"
    }
    or
    { "success": false }

### READ (Complete list of roles)

Request

    GET roles

Response

    [{
        "cn": "Administrator",
        "users": ['pgiraud']
    }, {
        "cn": "Name of the role",
        "description": "Description for the role",
        "users": []
    }]


### READ

Request

    GET roles/:cn

Response

    {
        "cn": "Name of the role",
        "description": "Description for the role",
        "users": ["uid0","uid1"]
    }

### UPDATE

Modify role

Request

    PUT roles/:cn

Body parameters

    {
        "cn": "New name of the role",
        "description": "Modified description for the role"
    }

Note: *Fields that are not present in the parameters should remain untouched
on the server.*

Reponse

    200 OK

### DELETE

Delete role

Request

    DELETE roles/:cn

Response

    200 OK

## Group users

### UPDATE

Add/Remove users to/from the given role

Request

    POST roles_users

Body parameters

    {
        users: ["uid0","uid1",..,"uidN"],
        PUT: ["cn0","cn1",..,"cnN"],
        DELETE: ["cn3","cn4",..,"cnM"]
    }

Response

    { "success": true }
    or
    { "success": false }

## Use of curl

Curl can be used to test the client-server interface. To get the list of users:

    curl --request GET http://georchestra.mydomain.org/console/private/users/

To create a new role

* create a temporary file

```
nano /tmp/newrole.json
```

```
{
    "cn": "Name of the role",
    "description": "Description for the role"
}
```

*  post this file

    curl -H "Content-Type: application/json" -d @/tmp/newrole.json --request POST http://georchestra.mydomain.org/console/private/roles
