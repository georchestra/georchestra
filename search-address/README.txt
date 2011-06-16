The search-address application is based on the Pylons web framework, and it
uses zc.buildout (a.k.a. buildout) for its installation and setup.

This README file describes how to install and set up the search-address
project.

The installation of the address database isn't described here. We assume here
that the database is up and running.

Install
=======

First set up the buildout environment using this command::

    python bootstrap.py --version 1.5.2 --distribute --download-base http://pypi.camptocamp.net/ --setup-source http://pypi.camptocamp.net/distribute_setup.py

Create a buildout config file. For this copy the buildout_sample.cfg file into
a new file. You can choose any name for this file, we assume the name
"buildout_mine.cfg" in the following.

Edit buildout_mine.cfg and set the variables as appropriate. The variables
names and the comments should be sufficient to understand what these variables
are about.

Run buildout to install and set up search-address and its dependencies::

    ./buildout/bin/buildout -c buildout_mine.cfg

The buildout process should end with the following output::

    *************** PICKED VERSIONS ****************
    [versions]

    *************** /PICKED VERSIONS ***************

The installation is complete. You can test that the project is correctly
installed by executing the search-address application in the embedded web
server::

    ./buildout/bin/paster serve searchaddress_dev.ini

The output should be this::

    Starting server in PID 1358.
    serving on 0.0.0.0:5000 view at http://127.0.0.1:5000

You can test that the ``addresses`` web service works correctly by
opening the following URL in the browser::

    http://<hostname>:5000/addresses?limit=20&lang=fr&attrs=street%2Chousenumber%2Ccity&query=brest

Hit CTRL+C to stop the web server.

Execute in Apache
=================

To be able to execute the search-address application in Apache you have to have
the Apache mod_wsgi module installed and activated. If you use a Debian-like
distribution you can just install the ``libapache2-mod-wsgi`` Debian package.

The application comes with an Apache configuration. To execute the application
in Apache you just need to include that configuration from your global Apache
configuration. This is done by using the ``Include`` Apache directive::

    Include /path/to/search-address/apache/*.conf

You can now restart or reload Apache, and test the following URL to make sure
search-address is functional::

    http://<hostname>/addrapp/addresses?limit=20&lang=fr&attrs=street%2Chousenumber%2Ccity&query=brest
