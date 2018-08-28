# à partir d'une image ubuntu, à cause de owncloudcmd
FROM ubuntu:latest

# Set the env variable DEBIAN_FRONTEND to noninteractive
ENV DEBIAN_FRONTEND noninteractive
ENV TERM linux

# install locales
# https://stackoverflow.com/questions/28405902/how-to-set-the-locale-inside-a-docker-container
RUN apt-get update && \
    apt-get install -y locales locales-all \
 && rm -rf /var/lib/apt/lists/*
RUN update-locale LC_ALL=C.UTF-8 LANG=fr_FR.UTF-8
ENV LC_ALL='C.UTF-8' LANG='fr_FR.UTF-8'

# environment variables
ENV GEO_USER_PASS  georchestra-ouvert

# install packages
RUN apt-get update && apt-get install -y \
    cron \
    python \
    python-dev \
    python-owslib \
    python-lxml \
    python-requests \
    python-httplib2 \
    libxml2-utils \
    ca-certificates \
    curl \
    gdal-bin \
    postgis \
    default-jre-headless \
    libsaxonb-java \
    dbview \
    owncloud-client-cmd \
    less vim colordiff openssh-client \
 && rm -rf /var/lib/apt/lists/*

# bash_profile and bashrc 
ADD dot.bash_profile /root/.bash_profile
ADD dot.bashrc /root/.bashrc

# create geosync group users
RUN groupadd --gid 991 geosync

# add geosync user : georchestra-ouvert
ADD georchestra-ouvert /tmp/georchestra-ouvert
RUN useradd -s /bin/bash \
            -p $(echo "print crypt("${GEO_USER_PASS:-password}", "salt")" | perl) \
            --uid 992 --gid 991 georchestra-ouvert
RUN adduser georchestra-ouvert crontab

# add geosync user georchestra-restreint
ADD georchestra-restreint /tmp/georchestra-restreint
RUN useradd -s /bin/bash \
            -p $(echo "print crypt("${GEO_USER_PASS:-password}", "salt")" | perl) \
            --uid 993 --gid 991 georchestra-restreint
RUN adduser georchestra-restreint crontab

# add geosync source code
COPY geosync /usr/local/geosync

# entrypoint
COPY entry.sh /

ENTRYPOINT ["/entry.sh"]

# Start cron daemon in the foreground
CMD ["/usr/sbin/cron","-f"]

