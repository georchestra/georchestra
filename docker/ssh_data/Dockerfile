FROM debian:stretch

MAINTAINER PSC "psc@georchestra.org"

RUN apt-get update && \
    apt-get install -y openssh-server rsync && \
		rm -rf /var/lib/apt/lists/*

# add a user 'geoserver' with default password : 'geoserver'
# that can be overriden by environement variable $USER_PASS
RUN groupadd --gid 999 geoserver
RUN useradd -ms /bin/bash --home /home/geoserver -p $(echo "print crypt("${USER_PASS:-geoserver}", "salt")" | perl) --uid 999 --gid 999 geoserver

RUN mkdir /mnt/geoserver_geodata
RUN chown -R 999:999 /mnt/geoserver_geodata

ADD start.sh /root/
RUN chmod +x /root/start.sh

RUN mkdir /var/run/sshd
RUN chmod 0755 /var/run/sshd

EXPOSE 22

CMD /root/start.sh
