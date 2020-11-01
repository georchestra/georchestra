FROM openjdk:8-jre

# install sudo -------------------
RUN apt-get update && apt-get install -y \
    sudo \
&& chmod u+s /usr/bin/sudo

# Installed sudo so the 'geo' user can run admin commands if you ever need to enter a running container.
# Beware if the host's /var/lib/docker directory (or the configured docker graph directory) is on a filesystem 
# mounted with nosuid, trying to run a sudo command inside the container it will fail with the following error:
# "sudo: effective uid is not 0, is /usr/bin/sudo on a file system with the 'nosuid' option set or an NFS file system without root privileges?".
# For example, my docker graph (/etc/docker/daemon.json) is `{"graph": "/data2/docker_files"}`, and `/data2` is a nosuid filesystem.
# If that happens, you can remount the host filesystem with `sudo mount -n -o remount,suid /data2` (replace /data2 by your mount point)

RUN groupadd -r -g 630 geo && \
    useradd -r -g geo -G sudo -u 630 geo&& \
    mkdir /opt/app && \
    chown geo:geo /opt/app && \
    echo "geo ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers

USER geo
EXPOSE 8080

ENV JAVA_OPTS=

ARG JAR_FILE=dummy
ADD ${JAR_FILE} /opt/app/datafeeder.jar

CMD exec java $JAVA_OPTS -jar /opt/app/datafeeder.jar
