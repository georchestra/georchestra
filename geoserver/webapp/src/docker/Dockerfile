FROM georchestra/base

ADD . /
ADD https://github.com/georchestra/geoserver_minimal_datadir/archive/master.zip /tmp/datadir.zip

RUN chmod +x /root/start.sh

CMD /root/start.sh