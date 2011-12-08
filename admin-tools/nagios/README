Nagios sample configuration

In this directory are stored some sample files that could be used to configure
and monitor a geOrchestra instance.

You need to understand 4 concepts of Nagios:

- hostgroups : you may have some monitored hosts into one hostgroup. Here we
  create a hostgroup named "geOrchestra"

- hosts : a host is a (potentially remote) server that is monitored by nagios

- services : a service is something that is currently monitored by nagios. A
  host can be associated to some services. We are defining a service for each
  geOrchesta components.

- commands : in order to monitor a specific service, a command has to be
  defined in nagios. This command would be basically launched by Nagios, and
  the return of this command would give us the current status (Ok, critical,
  warning ...) of a given service.

Thus, this directory contains 3 files:

- hostgroup_georchestra.cfg : defines the hostgroup

- hosts_georchestra.cfg : defines the hosts to be monitored

- services_georchestra.cfg : defines the services and the underlying commands

To set it up under a debian / ubuntu based distro, just copy these files into
/etc/nagios3/conf.d/, then edit hostgroup_georchestra.cfg and host_georchestra
to suit your need.

You will obviously need the nagios3 debian package.

