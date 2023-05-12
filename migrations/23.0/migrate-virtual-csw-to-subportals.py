#!/usr/bin/env python3

import psycopg2
from datetime import datetime

conn = psycopg2.connect("dbname=georchestra user=georchestra host=localhost port=5432")
cur = conn.cursor()

MAPPED_ES_FIELDS =  {
            "_source": "harvesterUuid",
            "keyword": "tag.default"
            }

def grab_serviceparams_criterias(service_id):
    cur.execute("SELECT * FROM geonetwork.serviceparameters WHERE service = %s", (service_id,))
    rs = cur.fetchall()
    calculated_criteria = ""
    for i, cur_sp in enumerate(rs):
        calculated_criteria += "+{}: {} ".format(MAPPED_ES_FIELDS[cur_sp[1]] or cur_sp[1], cur_sp[3])

    return calculated_criteria


def morph_criteria(criteria):
    crit = criteria.replace("_source=", "+harvesterUuid: ")
    crit = crit.replace("type=", "+resourceType: ")

    return crit.strip()

def source_already_created(uuid):
    cur.execute("SELECT * FROM geonetwork.sources WHERE uuid = %s", (uuid,))
    rs = cur.fetchall()

    return True if len(rs) > 0 else False


def create_source(name, lucenefilter):
    insert = """
    INSERT INTO
        geonetwork.sources ( uuid, name, creationdate, filter, groupowner, logo, type, uiconfig, servicerecord )
    VALUES
        ( %s, %s, %s, %s, NULL, NULL, 'subportal', NULL, NULL)
    """
    now = datetime.now().isoformat()
    cur.execute(insert, (name, name, now, lucenefilter))



cur.execute("SELECT * FROM geonetwork.services")
all_services = cur.fetchall()



for i in all_services:

    sce_id, sce_class, sce_desc, sce_explicitquery, sce_name = i

    print(sce_name)
    crit = None
    if sce_explicitquery is None or sce_explicitquery == '':
        print("\tNo explicit query defined for service id {}, need to grab the params from the geonetwork.serviceparameters table".format(sce_id))
        crit = grab_serviceparams_criterias(sce_id)
    else:
        crit = sce_explicitquery

    crit = morph_criteria(crit)
    print("\tMorphed criteria: '{}'".format(crit))

    if source_already_created(sce_name):
        print("\tsource '{}' already exists in the geonetwork.source table, skipping.".format(sce_name))
    else:
        print("\t'{}' does not exist yet, creating ...".format(sce_name))
        create_source(sce_name, crit)

conn.commit()
cur.close()
conn.close()
