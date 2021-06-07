FROM bellsoft/liberica-openjdk-alpine-musl:11

# run as an unpriviledged OS user
RUN mkdir -p /opt/app \
    && addgroup -S -g 630 geo \
    && adduser -s /bin/sh -h /opt/app -D -u 630 -G geo geo \
    && chown geo:geo /opt/app
    
# where to store file uploads, should be mapped to a volume
RUN mkdir /tmp/datafeeder && chown geo:geo /tmp/datafeeder

USER geo
EXPOSE 8080

ENV JAVA_OPTS=

ARG JAR_FILE=dummy
ADD ${JAR_FILE} /opt/app/datafeeder.jar

CMD exec java $JAVA_OPTS -jar /opt/app/datafeeder.jar
