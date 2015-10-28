# GeoServer instances clustering

When one server is not enough to handle the load, or when **High Availability** & **High Performance** are expected, it is a good idea to setup a cluster of GeoServer instances.

In the proposed setup, one GeoServer instance is dedicated to administration, while the other ones (called "slaves") handle incoming requests. 

A typical setup could be:
 * **GeoServer main** installed on the same machine as the proxy + cas (but on a different tomcat instance)
 * **GeoServer slaves** installed on separate machines

All instances will have to share two things:
 * their configuration (aka the "data dir")
 * their geodata
 
There are lots of different ways to achieve this, and we will not go through all of them.
The most common setup is based on **NFS shares**, and that's the one we describe here, but we've seen [interesting things](https://gist.github.com/fvanderbiest/d93c6c7d2e58425c2b2e) based on git recently.



## Server setup

Every "slave" machine should host one tomcat instance containing a single webapp, deployed from the same GeoServer WAR.

We also take for granted that instances have been setup according to the [documentation regarding tomcat-geoserver](../setup/tomcat.md#tomcat-geoserver) and that the recommended [optimizations](../optimizations.md) have been applied (Marlin Renderer, JAI / ImageIO, TurboJPEG, ControlFlow, etc).



## NFS setup

On the machine hosting **GeoServer main**, we suppose that the "geoserver data dir" is located in ```/opt/geoserver_datadir```.
This directory will be exposed to the slaves through NFS:
```
apt-get install nfs-common nfs-kernel-server git-core
echo '/opt/geoserver_datadir IP_S1(sync,rw,no_subtree_check)' >> /etc/exports
echo '/opt/geoserver_datadir IP_S2(sync,rw,no_subtree_check)' >> /etc/exports
service nfs-kernel-server restart
```
... in which ```IP_S1``` and ```IP_S2``` are the IP addresses of the two slave machines (but there could be more).


On the slave instances:
```
mkdir /opt/geoserver_datadir
echo 'IP_MASTER:/opt/geoserver_datadir /opt/geoserver_datadir nfs _netdev 0 0' >> /etc/fstab
mount -a
```
... where ```IP_MASTER``` is the IP adress of the master instance.

Congrats, the data directory is now synchronized across all instances through NFS !


When GIS data are not stored in a database, the same steps should be repeated in order to create a shared geodata repository.



## Keeping configurations in sync

Every time GeoServer configuration changes (either through GeoServer admin GUI, or through its REST interface), the "data dir" is altered and its ```global.xml``` file is updated.
With ```incrond```, updates to this file are detected and slaves get notified through the use of a [simple python script](https://gist.github.com/fvanderbiest/f5d5e467c7ca004ce73b).

Here are the steps:
 * install ```incrond``` with ```apt-get install incron```
 * download this [script](https://gist.github.com/fvanderbiest/f5d5e467c7ca004ce73b) in /root, and register the slave IPs in it
 * allow root to use incrond: ```echo 'root' > /etc/incron.allow```

In the file opened by the ```incrontab -e``` command: 
```
/opt/geoserver_datadir/global.xml IN_MODIFY /root/refresh.py
```

Finally restart the service with ```service incron restart```



## Load-balancer

In a typical setup, the load balancer is installed on the same machine as tomcat-proxycas.

Here are two configuration examples:
 * one [based on HAProxy](https://gist.github.com/fvanderbiest/bb703531dc085427eed5) 
 * an other one [using Nginx](https://gist.github.com/fvanderbiest/310b380488d73347acbc)



## Security Proxy

In your configuration profile's ```GenerateConfig.groovy``` file, the ```proxy.mapping``` option should include the following: 
```
<entry key="geoserver" value="http://LOAD_BALANCER_IP:LOAD_BALANCER_PORT/geoserver/" />
```
instead of the default:
```
<entry key="geoserver"     value="geoserverTarget/geoserver/" />
```

Redeploy the security proxy to take into account this change, and test everything is working as expected.