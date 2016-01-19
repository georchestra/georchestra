FROM jetty:latest

MAINTAINER PSC "psc@georchestra.org"

RUN apt-get update && \
    apt-get install -y git && \
		rm -rf /var/lib/apt/lists/*
		
ADD . /

RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=jmx,jmx-remote,stats

# initialise data dir from github
RUN echo "#!/bin/bash" > /root/start.sh
RUN echo "cd /var/local/geoserver" >> /root/start.sh
RUN echo "if [ -d .git ]" >> /root/start.sh
RUN echo "	then" >> /root/start.sh
RUN echo "    echo 'Datadir already initialised'" >> /root/start.sh
RUN echo "else " >> /root/start.sh
RUN echo "    echo 'Initialising datadir from github'" >> /root/start.sh
RUN echo "    git clone https://github.com/georchestra/geoserver_minimal_datadir.git ." >> /root/start.sh
RUN echo "    chown -R jetty:jetty /var/local/geoserver" >> /root/start.sh
RUN echo "fi" >> /root/start.sh
RUN echo "cd /var/lib/jetty" >> /root/start.sh
# finnaly launch jetty server at the end of the script
RUN echo "java -Djava.io.tmpdir=/tmp/jetty -DGEOSERVER_DATA_DIR=/var/local/geoserver/ -jar /usr/local/jetty/start.jar" >> /root/start.sh

RUN chmod +x /root/start.sh

# make jetty owner of datadir so jetty can write files into.
RUN chown -R jetty:jetty /var/local/geoserver/

CMD /root/start.sh
