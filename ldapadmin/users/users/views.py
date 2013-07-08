from pyramid.view import view_config
from pyramid.response import Response

import json

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
        user = int(self.request.matchdict['user'])
        for i in _users:
            if i['id'] == user:
                return _users[user]
        return 'user not found' 

    @view_config(route_name='users', request_method='POST')
    def post(self):
        return Response('post')

    @view_config(route_name='users', request_method='DELETE')
    def delete(self):
        return Response('delete')
