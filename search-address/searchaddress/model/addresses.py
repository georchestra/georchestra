from sqlalchemy import Column, Table, types
from sqlalchemy.orm import mapper

from mapfish.sqlalchemygeom import Geometry
from mapfish.sqlalchemygeom import GeometryTableMixIn

from searchaddress.model.meta import metadata, engine

addresses_table = Table(
    'address', metadata,
    Column('geom', Geometry(4326)),
    autoload=True, autoload_with=engine)

class Address(GeometryTableMixIn):
    # for GeometryTableMixIn to do its job the __table__ property
    # must be set here
    __table__ = addresses_table

mapper(Address, addresses_table)
