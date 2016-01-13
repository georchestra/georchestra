#
# Dockerfile for the geOrchestra database service
#
# Use with caution: this is work in progress
# Based on http://docs.docker.com/examples/postgresql_service/

FROM debian:jessie

MAINTAINER PSC "psc@georchestra.org"

ENV DEBIAN_FRONTEND noninterative

RUN apt-get update && \
    apt-get install -y postgresql-9.4-postgis-2.1 wget

# Fetch the SQL code
# https://github.com/docker/compose/issues/1065
#ADD https://raw.githubusercontent.com/georchestra/georchestra/master/mapfishapp/database.sql /tmp/mapfishapp.sql
#ADD https://raw.githubusercontent.com/georchestra/georchestra/master/downloadform/database.sql /tmp/downloadform.sql
#ADD https://raw.githubusercontent.com/georchestra/georchestra/master/ldapadmin/database.sql /tmp/ldapadmin.sql
#ADD https://raw.githubusercontent.com/georchestra/georchestra/master/ogc-server-statistics/database.sql /tmp/ogcstatistics.sql
RUN wget --no-check-certificate https://raw.githubusercontent.com/georchestra/georchestra/master/mapfishapp/database.sql -O /tmp/mapfishapp.sql
RUN wget --no-check-certificate https://raw.githubusercontent.com/georchestra/georchestra/master/downloadform/database.sql -O /tmp/downloadform.sql
RUN wget --no-check-certificate https://raw.githubusercontent.com/georchestra/georchestra/master/ldapadmin/database.sql -O /tmp/ldapadmin.sql
RUN wget --no-check-certificate https://raw.githubusercontent.com/georchestra/georchestra/master/ogc-server-statistics/database.sql -O /tmp/ogcstatistics.sql
RUN chmod a+r /tmp/*.sql

# Run the rest of the commands as the ``postgres`` user
USER postgres

# Create a PostgreSQL role named ``georchestra`` with ``georchestra`` as the password and
# then create a database ``georchestra`` owned by the ``georchestra`` role.
RUN /etc/init.d/postgresql start && \
    psql -c "CREATE USER georchestra WITH SUPERUSER PASSWORD 'georchestra';" && \
    psql -c "CREATE USER geonetwork WITH SUPERUSER PASSWORD 'georchestra';" && \
    createdb -O georchestra georchestra && \
    psql -c "CREATE EXTENSION postgis;" -d georchestra && \
    psql -d georchestra -f /tmp/mapfishapp.sql && \
    psql -d georchestra -f /tmp/downloadform.sql && \
    psql -d georchestra -f /tmp/ldapadmin.sql && \
    psql -d georchestra -f /tmp/ogcstatistics.sql && \
    /etc/init.d/postgresql stop
# FIXME: no superuser here

# Adjust PostgreSQL configuration so that remote connections to the
# database are possible.
RUN echo "host all  all    0.0.0.0/0  md5" >> /etc/postgresql/9.4/main/pg_hba.conf

# And add ``listen_addresses``
RUN echo "listen_addresses='*'" >> /etc/postgresql/9.4/main/postgresql.conf

# Better query performance with:
RUN sed -i 's/shared_buffers = 128MB/shared_buffers = 1024MB/' /etc/postgresql/9.4/main/postgresql.conf

# Expose the PostgreSQL port
EXPOSE 5432

# Add VOLUMEs to allow backup of config, logs and databases
#~ VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]
VOLUME  ["/var/log/postgresql"]

# Set the default command to run when starting the container
CMD ["/usr/lib/postgresql/9.4/bin/postgres", "-D", "/var/lib/postgresql/9.4/main", "-c", "config_file=/etc/postgresql/9.4/main/postgresql.conf"]
