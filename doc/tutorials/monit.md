# Monit

Monit is a powerful tool to monitor geOrchestra instances.

It is able to:
 * watch the CPU, RAM, swap, network use
 * check services availability, eventually restarting them when required
 * alert when metrics exceed a predefined value, or when a service is down
 * and lots more...


## Setup

On every server to monitor:
```
apt-get install monit
```

In the ```/etc/monit/monitrc``` file: 
```
set daemon 60
set logfile syslog facility log_daemon
set mailserver localhost port 25
set mail-format { from: monit@example.fr }
set alert <email-adress-to-alert> but not on { instance, pid, ppid }
set httpd port 2812 and use address localhost allow XXXX
include /etc/monit/conf.enabled/*
```
... where:
 * ```set daemon``` value corresponds to the interval between checks in seconds
 * ```set mailserver``` allows one to use a specific email server (for alerts)
 * ```set mail-format``` allows email customization (read the [doc](https://mmonit.com/monit/documentation/monit.html#Message-format) or have a look at a [better email template](https://gist.github.com/fvanderbiest/af59a8431af5e3f751e0#file-monitrc))
 * ```set alert``` configures when and who to alert. 
 * ```set httpd``` configures an embedded web service to listen on the given port. Access to this service is restricted to host named XXXX or protected with a basic auth if XXXX = admin:randompassword

Once the service is restarted (```service monit restart```), the UI should be visible on http://\<server-ip\>:2812.


## Checks

With the above configuration, files describing checks belong to the ```/etc/monit/conf.enabled/``` directory.

Eg, in ```/etc/monit/conf.enabled/tomcat-proxycas```:
```
check process tomcat-proxycas with pidfile /var/run/tomcat-proxycas.pid
    start program = "/etc/init.d/tomcat-proxycas start"
    with uid tomcat8 gid tomcat8
    stop program = "/etc/init.d/tomcat-proxycas stop"
    with uid tomcat8 gid tomcat8

    if failed host 127.0.0.1 port 8180 for 3 cycles then restart
    if memory is greater than 1 GB then restart
    if cpu usage > 99% for 3 cycles then restart

    # restart security proxy if header page is not ok
    if failed (url http://127.0.0.1:8180/header/
        and content = "html")
        for 3 cycles
    then restart
```

In ```/etc/monit/conf.enabled/tomcat-georchestra```:
```
check process tomcat-georchestra with pidfile /var/run/tomcat-georchestra.pid
    start program = "/etc/init.d/tomcat-georchestra start"
    with uid tomcat8 gid tomcat8
    stop program = "/etc/init.d/tomcat-georchestra stop"
    with uid tomcat8 gid tomcat8

    if failed host 127.0.0.1 port 8280 for 3 cycles then restart
    if memory is greater than 2 GB then restart
    if cpu usage > 99% for 10 cycles then restart

    # restart if csw getrecords fails
    if failed (url http://127.0.0.1:8280/geonetwork/srv/eng/csw?service=CSW&version=2.0.2&request=GetRecords&constraintlanguage=CQL_TEXT&typeNames=csw:Record
        and content = "SearchResults")
        for 3 cycles
    then restart
```

In ```/etc/monit/conf.enabled/tomcat-geoserver0```:
```
check process tomcat-geoserver0 with pidfile /var/run/tomcat-geoserver0.pid
    start program = "/etc/init.d/tomcat-geoserver0 start"
    with uid tomcat8 gid tomcat8
    stop program = "/etc/init.d/tomcat-geoserver0 stop"
    with uid tomcat8 gid tomcat8

    if totalmem > 80% then alert

    if failed host 127.0.0.1 port 8380 for 3 cycles then restart
    if memory is greater than 2 GB then restart
    if cpu usage > 99% for 10 cycles then restart

    # restart geoserver if capabilities are wrong
    if failed (url http://127.0.0.1:8380/geoserver/geor/wms?SERVICE=WMS&REQUEST=GetCapabilities
        and content = "WMS_Capabilities")
        for 3 cycles
    then restart
```

Feel free to customize these examples according to your setup (particularly the RAM limits).
There are lots of other [configuration examples](https://mmonit.com/wiki/Monit/ConfigurationExamples) in the Monit wiki.

With Monit comes [M/Monit](https://mmonit.com/), a proprietary solution to centralize all Monit UIs in a unique web-portal.
This can be handy if your SDI is spread over a large number of servers.
