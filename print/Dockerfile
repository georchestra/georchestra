FROM camptocamp/mapfish_print:latest
MAINTAINER Camptocamp "info@camptocamp.com"

RUN rm -rf webapps/ROOT/print-apps
ADD print-apps ${CATALINA_HOME}/webapps/ROOT/print-apps
ADD WEB-INF/classes/*.xml ${CATALINA_HOME}/webapps/ROOT/WEB-INF/classes/
# ADD WEB-INF/lib/*.jar ${CATALINA_HOME}/webapps/ROOT/WEB-INF/lib/