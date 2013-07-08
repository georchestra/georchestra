from pyramid.view import view_config
from pyramid.response import Response

import json
import uuid

json_data = open('data/all.json')
_users = json.load(json_data, parse_int=int, parse_float=float)
json_data.close()
#print f.read()
#_users = {
    #'foo': {
        #'name': 'toto'
    #},
    #'bar': {
        #'name': 'titi'
    #}
#}

class RESTView(object):
    def __init__(self, request):
        self.request = request

    @view_config(route_name='users', renderer='json', request_method='GET')
    def get(self):
        return _users

    @view_config(route_name='user', renderer='json', request_method='GET')
    def user(self):
        user = self.request.matchdict['user']
        for ndx, i in enumerate(_users):
            if str(i['id']) == user:
                return _users[ndx]
        return 'user not found'

    @view_config(route_name='users', renderer='json', request_method='POST')
    def post(self):
        id = str(uuid.uuid4())
        user = {
            'name': self.request.json_body['name'],
            'email': self.request.json_body['email'],
            'id': id
        }
        _users.append(user)
        return user

    @view_config(route_name='user', request_method='PUT')
    def put(self):
        user = self.request.matchdict['user']
        for ndx, i in enumerate(_users):
            if str(i['id']) == user:
                i['name'] = self.request.json_body['name']
                i['email'] = self.request.json_body['email']
        return Response('put')

    @view_config(route_name='user', request_method='DELETE')
    def delete(self):
        user = self.request.matchdict['user']
        for ndx, i in enumerate(_users):
            if str(i['id']) == user:
                del _users[ndx]
        return Response('delete')
