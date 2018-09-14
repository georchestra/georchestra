FROM georchestra/database

ENV DEBIAN_FRONTEND noninterative
ENV TERM linux

RUN echo Europe/Paris | tee /etc/timezone
RUN dpkg-reconfigure --frontend noninteractive tzdata

ADD 09-geosync.sql /docker-entrypoint-initdb.d/

ADD pg_hba.conf /docker-entrypoint-initdb.d/
RUN chmod o+r /docker-entrypoint-initdb.d/pg_hba.conf

ADD postgresql.conf /docker-entrypoint-initdb.d/
RUN chmod o+r /docker-entrypoint-initdb.d/postgresql.conf

ADD start.sh /docker-entrypoint-initdb.d/

CMD ["postgres"]

HEALTHCHECK --interval=30s --timeout=30s \
  CMD pg_isready -U postgres

