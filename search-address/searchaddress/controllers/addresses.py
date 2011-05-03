# -*- coding: utf-8 -*-
import logging
import csv
import StringIO
import zipfile

from pylons import request, response, session, tmpl_context as c
from pylons.controllers.util import abort, redirect_to
from pylons.decorators import jsonify

from pylons import config
from pylons.i18n.translation import *

from searchaddress.lib.base import BaseController
from searchaddress.model.addresses import Address
from searchaddress.model.meta import Session

from mapfish.lib.filters import *
from mapfish.lib.protocol import Protocol, create_default_filter
from mapfish.lib.filters.spatial import Spatial

import locale

from datetime import datetime

from sqlalchemy.sql import and_

log = logging.getLogger(__name__)

class AddressesController(BaseController):
    readonly = False # if set to True, only GET is supported

    def __init__(self):
        self.protocol = Protocol(Session, Address, self.readonly, before_create = self.before_create)

    def index(self, format='json'):
        """GET /: return all features."""
        # If no filter argument is passed to the protocol index method
        # then the default MapFish filter is used. This default filter
        # is constructed based on the box, lon, lat, tolerance GET
        # params.
        #
        # If you need your own filter with application-specific params
        # taken into acount, create your own filter and pass it to the
        # protocol index method.
        #
        # E.g.
        #
        # default_filter = create_default_filter(
        #     request, Address
        # )
        # compare_filter = comparison.Comparison(
        #     comparison.Comparison.ILIKE,
        #     Address.mycolumnname,
        #     value=myvalue
        # )
        # filter = logical.Logical(logical.Logical.AND, [default_filter, compare_filter])
        # return self.protocol.index(request, response, format=format, filter=filter)
        #
        #
        # You can also create filters using sqlalchemy syntax.
        # It is possible for example to mix a custom sqlalchemy filter
        # with the default mapfish filter.
        #
        # E.g.
        #
        # from sqlalchemy.sql import and_
        #
        # default_filter = create_default_filter(
        #     request, Address
        # )
        # compare_filter = Address.mycolumnname.ilike('%myvalue%')
        # if default_filter is not None:
        #     filter = and_(default_filter.to_sql_expr(), compare_filter)
        # else:
        #     filter = compare_filter
        # return self.protocol.index(request, response, format=format, filter=filter)
        default_filter = create_default_filter(
              request, Address
        )
        # Convert attribute KVP to filter
        for column in Address.__table__.columns:
           if column.name in request.params:
              column_name = column.name
              column_value = request.params[column.name]
              # PGString, PGInteger are supported
              # PGDateTime, Geometry, NullType are not supported
              if str(column.type).find('PGInteger') > -1:
                 compareFilter = comparison.Comparison(
                        comparison.Comparison.EQUAL_TO,
                        Address.__table__.columns[column_name],
                        value=column_value
                     )
                 if default_filter is not None:
                    default_filter = and_(default_filter.to_sql_expr(), compareFilter)
                 else:
                    default_filter = compareFilter
              if str(column.type).find('PGString') > -1:
                 compareFilter = comparison.Comparison(
                        comparison.Comparison.LIKE,
                        Address.__table__.columns[column_name],
                        value=column_value
                     )
                 if default_filter is not None:
                    default_filter = and_(default_filter.to_sql_expr(), compareFilter)
                 else:
                    default_filter = compareFilter
        # Check query for full text search
        if 'query' in request.params:
           # http://lowmanio.co.uk/blog/entries/postgresql-full-text-search-and-sqlalchemy/
           terms = request.params.get('query').split()
           terms = ' & '.join([term + ('' if term.isdigit() else ':*')  for term in terms])

           if 'attrs' in request.params:
              attributes = request.params.get('attrs').split(',')
              if (len(attributes) == 3) and ('street' in request.params.get('attrs')) and ('city' in request.params.get('attrs')) and ('housenumber' in request.params.get('attrs')):
                 tsvector = 'tsvector_street_housenumber_city'
              elif (len(attributes) == 1) and ('street' in request.params.get('attrs')):
                 tsvector = 'tsvector_street'
              else:
                 attributes = " || ' ' ||".join([attribute for attribute in attributes])
                 tsvector = attributes
           else:
              tsvector = 'tsvector_street_housenumber_city'

           ftsFilter = "%(tsvector)s @@ to_tsquery('french', '%(terms)s')" %{'tsvector': tsvector, 'terms': terms}
           if default_filter is not None:
              filter = and_(default_filter.to_sql_expr(), ftsFilter)
           else:
              filter = ftsFilter

           if format == 'csv':
              return self.exportCsv(request,filter)
           if format == 'zip':
              return self.exportZip(request,filter)

           json = self.protocol.index(request, response, format=format, filter=filter)
           if 'callback' in request.params:
              response.headers['Content-Type'] = 'text/javascript; charset=utf-8'
              return request.params['callback'] + '(' + json + ');'
           else:
              response.headers['Content-Type'] = 'application/json'
              return json
        else:
           if format == 'csv':
              return self.exportCsv(request,default_filter)
           if format == 'zip':
              return self.exportZip(request,default_filter)
           return self.protocol.index(request, response, format=format, filter=default_filter)

    def createCsvFile(self,request,filter):
       io = StringIO.StringIO()
       writer = csv.writer(io, delimiter=';')
       objs = self.protocol._query(request, filter=filter)
       def removeNone(value):
          if len(value) == 4 and value == 'None':
             return ''
          else:
             return value
       for f in [self.protocol._filter_attrs(o.toFeature(), request) for o in objs if o.geometry]:
          row = map(lambda v : removeNone(str(v)) , f.properties.values())
          row.insert(0,str(f.id))
          if (f.geometry is not None):
             row.append(str(f.geometry.coordinates[0]))
             row.append(str(f.geometry.coordinates[1]))
          writer.writerow(row)
       output = io.getvalue()
       io.close()
       return output

    def exportCsv(self,request,filter):
       response.content_type = 'text/csv; charset=utf-8'
       response.content_disposition = 'attachment; filename="addresses.csv"'
       return self.createCsvFile(request,filter)

    def exportZip(self,request,filter):
       imz = InMemoryZip()
       imz.append("addresses.csv", self.createCsvFile(request,filter))
       response.content_type = 'application/zip; charset=utf-8'
       response.content_disposition = 'attachment; filename="addresses.zip"'
       return imz.read()

    def show(self, id, format='json'):
        """GET /id: Show a specific feature."""
        if (id == 'count'):
           return self.protocol.count(request)
        elif (id == 'countCreatedToday'):
           return self.countCreatedToday(request)
        elif (id == 'countUpdatedToday'):
           return self.countUpdatedToday(request)
        elif (id == 'statistic'):
           return self.statistic(request)
        elif (id == 'checkSession'):
           return self.checkSession()
        elif (id == 'createSession'):
           return self.createSession()
        else:
           return self.protocol.show(request, response, id, format=format)

    def create(self):
        """POST /: Create a new feature."""
        return self.protocol.create(request, response)


    def update(self, id):
        """PUT /id: Update an existing feature."""
        return self.protocol.update(request, response, id)

    def delete(self, id):
        """DELETE /id: Delete an existing feature."""
        return self.protocol.delete(request, response, id)

    def before_create(self,request,feature):
       feature.properties['ipaddress'] = request.environ['REMOTE_ADDR']
       if isinstance(feature.id, int):
           feature.properties['time_updated'] = datetime.now()
       else:
           feature.properties['time_updated'] = None

    def checkSession(self):
       return 'True'

    def createSession(self):
        return 'True'

    def countCreatedToday(self,request):

       # Create SQL Query
       sqlQuery = "select count(1) count from address where time_created::date=now()::date"

       # Execute query
       result = Session.execute(sqlQuery)

       for row in result:
          for column in row:
             return str(column)

    def countUpdatedToday(self,request):

       # Create SQL Query
       sqlQuery = "select count(1) from address where time_updated::date=now()::date"

       # Execute query
       result = Session.execute(sqlQuery)

       for row in result:
          for column in row:
             return str(column)

    def statistic(self,request):
       if 'lang' in request.params:
          c.lang = request.params.get('lang')
       else:
          c.lang = 'en'
       c.charset = 'utf-8'

       # Create SQL Query
       sqlQuery = "select created_by, count(1) as numberAddresses " \
          " from address where extract(week from time_created) = extract(week from now()) "\
          " and extract(year from time_created) = extract (year from now()) "\
          " group by created_by "\
          " order by numberAddresses DESC "\
          " LIMIT 20"

       # Execute query
       result = Session.execute(sqlQuery)

       weekCreator=[]
       for row in result:
          weekRow = []
          for column in row:
             weekRow.append(str(column))
          weekCreator.append(weekRow)

       c.weekCreator = weekCreator
       c.count = locale.format("%s", self.protocol.count(request), True)
       return render('/statistic.mako')

class InMemoryZip(object):
   def __init__(self):
       # Create the in-memory file-like object
       self.in_memory_zip = StringIO.StringIO()

   def append(self, filename_in_zip, file_contents):
       '''Appends a file with name filename_in_zip and contents of
          file_contents to the in-memory zip.'''
       # Get a handle to the in-memory zip in append mode
       zf = zipfile.ZipFile(self.in_memory_zip, "a", zipfile.ZIP_DEFLATED, False)

       # Write the file to the in-memory zip
       zf.writestr(filename_in_zip, file_contents)

       # Mark the files as having been created on Windows so that
       # Unix permissions are not inferred as 0000
       for zfile in zf.filelist:
           zfile.create_system = 0

       zf.close()
       return self

   def read(self):
       '''Returns a string with the contents of the in-memory zip.'''
       self.in_memory_zip.seek(0)
       return self.in_memory_zip.read()

   def writetofile(self, filename):
       '''Writes the in-memory zip to a file.'''
       f = file(filename, "w")
       f.write(self.read())
       f.close()