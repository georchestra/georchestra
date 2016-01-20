FROM jetty:latest

MAINTAINER PSC "psc@georchestra.org"

RUN apt-get update && \
    apt-get install -y git && \
		rm -rf /var/lib/apt/lists/*
		
ADD . /

RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=jmx,jmx-remote,stats
RUN chmod +x /root/start.sh

CMD /root/start.sh
