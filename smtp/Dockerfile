#
# Dockerfile for a catch-all SMTP server
#
FROM debian:jessie

MAINTAINER PSC "psc@georchestra.org"

ENV DEBIAN_FRONTEND noninterative

# Avoid ERROR: invoke-rc.d: policy-rc.d denied execution of start.
RUN echo "#!/bin/sh\nexit 0" > /usr/sbin/policy-rc.d

RUN apt-get update && \
    apt-get install -y postfix courier-base courier-imap

# add a user 'smtp' with password : 'smtp'
RUN useradd -ms /bin/bash -p PcdO6g4gV662A smtp

# Generate script to run at startup
RUN echo '#!/bin/bash' > /root/start.sh
RUN echo "/etc/init.d/courier-authdaemon start" >> /root/start.sh
RUN echo "/etc/init.d/courier-imap start" >> /root/start.sh
RUN echo "smtp-sink -c -d /home/smtp/Maildir/new/%M. -u smtp 0.0.0.0:25 10" >> /root/start.sh
RUN chmod +x /root/start.sh

# Run the rest of the commands as the ``smtp`` user
USER smtp
WORKDIR /home/smtp
RUN maildirmake Maildir

# Expose the IMAP port
EXPOSE 25 143

USER root
CMD ["/root/start.sh"]
