#!/bin/bash

#echo "starting cron"
/etc/init.d/cron start

#echo "starting saslauthd"
/etc/init.d/saslauthd start

echo "Starting slapd"
exec slapd -d 32768 -u openldap -g openldap

