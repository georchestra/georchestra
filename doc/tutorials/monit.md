# Monit

Monit is a powerful tool to monitor your geOrchestra instance.

It is able to:
 * watch the CPU, RAM, swap, network use
 * check services availability, eventually restarting them when required
 * alert when metrics exceed a predefined value, or when a service is down
 * and lots more...

## Monit installation

Monit has to be installed on each server you want to monitor, so on all of your instance server do the following :

```
apt-get install monit
cp /etc/monit/monitrc /etc/monit/monitrc_orig
cat /dev/null > /etc/monit/monitrc
```

And edit the **/etc/monit/monitrc** file as follow : 

```
set daemon 60
set logfile syslog facility log_daemon
set mailserver localhost
set mail-format { from: monit@example.fr }
set alert <email-adress-to-alert>
set httpd port 2812 and use address <server-IP>
allow admin:swordfish
```

Replace some elements to suit your own configuration.

**set daemon** correspond to the interval between checks.

And now use :

```
service monit restart
```

After what you can access your monit by **http://\<server-ip\>:2812** with login *admin* and password *swordfish*.

## Configure Monit monitoring rules

Previously, I gave you the mininmal Monit config in **/etc/monit/monitrc** file, but if you want Monit to perform checks, alerts and treatments, you'll need to say him what to to.

For that you have to append some rules in this **/etc/monit/monitrc** file, for example :

```
check process tomcat8 with pidfile /run/tomcat8.pid
  start program = "/etc/init.d/tomcat8 start"
  as uid tomcat8 gid tomcat8
  stop program = "/etc/init.d/tomcat stop"
  as uid tomcat8 gid tomcat8
  if cpu > 95% for 5 cycles then alert
  if totalmem > 80% then alert
  if failed port 8080 protocol http and request /mapfishapp/contexts/default.wmc then alert
   if failed port 8080 protocol http and request /cas/images/services/true.gif then alert
  if failed port 8080 protocol http and request /geoserver/public/wms?request=describelayer&layers=administrations&version=1.1.1 then alert
  if failed port 8080 protocol http and request /geonetwork/srv/fre/xml.info then alert
  if failed port 8080 for 5 cycles then restart
```

On the Security Proxy server running SP on port 8080, this rule will check if tomcat8 is running.
If CPU usage is over 95% for more than 5 conscutives checks it'll send an alert, also if memory usage is over 80% or if one of /mapfishapp, /geoserver or /geonetwork is unreachable.
If port 8080 doesn't respond for 5 cycles, it'll restart the security-proxy

We can have a similar one for geoserver's server : 

```
check process tomcat8 with pidfile /run/tomcat8.pid
  start program = "/etc/init.d/tomcat8 start"
  as uid tomcat8 gid tomcat8
  stop program = "/etc/init.d/tomcat stop"
  as uid tomcat8 gid tomcat8
  if cpu > 95% for 5 cycles then alert
  if totalmem > 80% then alert
  if failed port 8080 protocol http and request /geoserver/public/wms?request=describelayer&layers=administrations&version=1.1.1 then alert
  if failed port 8080 protocol http and request /geoserver/public/wms?request=describelayer&layers=administrations&version=1.1.1 for 3 cycles then restart
```

This one will test availability of /geoserver/public/wms?request=describelayer&layers=administrations&version=1.1.1 and alert if unreachable, or restart geoserver if unreachable for 3 times.

In order to receive alert by mail, you'll have to configure your mailserver on each instance.

You can see a lot of rules example on the M/Monit wiki : https://mmonit.com/wiki/Monit/ConfigurationExamples


## Centralize administration with M/Monit (Not free)

Over Monit, there is a non-free solution to centralize all of your monit instance in a web-portal by which you can manage your server :

- Generate utilisation charts
- Configure email notifications and users
- See the state of all your monit instances
- ...

You can get a free trial (1 month) of this on : https://mmonit.com/

### Install M/Monit

To install M/Monit, on a dedicated server (the way I did that, but you can also put it on an existing one) :

Get last M/Monit version
```
wget https://mmonit.com/dist/mmonit-3.5-linux-x64.tar.gz
```

Install it :
```
tar zxvf mmonit-3.5-linux-x64.tar.gz
rm mmonit-3.5-linux-x64.tar.gz
mv mmonit-3.5/ /opt/mmonit
```

Enable at startup : 

Create the file **/etc/init.d/mmonit** as follow : 
```
#! /bin/sh
# /etc/init.d/mmonit

case "$1" in
  start)
    echo "Starting mmonit"
    /opt/mmonit/bin/mmonit -d
    echo "mmonit is alive"
    ;;
  stop)
    echo "Stopping mmonit"
    /opt/mmonit/bin/mmonit stop
    echo "mmonit is dead"
    ;;
  *)
    echo "Usage: /etc/init.d/foobar {start|stop}"
    exit 1
    ;;
esac

exit 0
```

And use those commands : 
```
chmod 755 /etc/init.d/mmonit
update-rc.d mmonit defaults
```

And finnally start M/Monit : 
```
/opt/mmonit/bin/mmonit -d
```

You can access to your M/Monit portal by **http://<mmonit-server>:8080**, login *admin* password *swordfish*
Think about change default passwords in the admin panel.

### Add a Monit instance to M/Monit

In order to add new Monit instances to M/Monit, you have to redefine the **monit** user password in M/Monit admin panel (default is monit, so not secure).

After what in your Monit server you have to update the **/etc/monit/monitrc** file as follow :

```
set daemon 60
set logfile syslog facility log_daemon
set mailserver localhost
set mail-format { from: monit@example.fr }
set alert <email-adress-to-alert>
set mmonit http://monit:<monit_password>@<mmonit-server-IP>:8080/collector
set httpd port 2812 and use address <monit-server-IP>
allow localhost
allow <mmonit-server-IP>
allow admin:swordfish
```

And restart your monit instance, on monit server : 
```
service monit restart
```

Wait monit to restart and register automatically to M/Monit, after what you can refresh your M/Monit webpage and your new host shoud appear.
