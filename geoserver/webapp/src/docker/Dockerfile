FROM jetty:9.3-jre8


RUN apt-get update && \
    apt-get install -y unzip && \
        rm -rf /var/lib/apt/lists/*

ADD . /
ADD https://github.com/georchestra/geoserver_minimal_datadir/archive/master.zip /tmp/datadir.zip

RUN chmod +x /root/start.sh

CMD /root/start.sh
