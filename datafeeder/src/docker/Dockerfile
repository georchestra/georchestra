FROM openjdk:8-jre

# run as an unpriviledged OS user
RUN groupadd -r -g 630 geo && \
    useradd -r -g geo -u 630 geo&& \
    mkdir /opt/app && \
    chown geo:geo /opt/app

USER geo
EXPOSE 8080

ENV JAVA_OPTS=

ARG JAR_FILE=dummy
ADD ${JAR_FILE} /opt/app/datafeeder.jar

CMD exec java $JAVA_OPTS -jar /opt/app/datafeeder.jar
