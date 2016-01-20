#!/bin/bash
set -e

if [ "$USER_PASS" ]; then
    echo geoserver:"$USER_PASS" |chpasswd
fi

chown -R 999:999 /home/geoserver


# start openssh server
/usr/sbin/sshd -D