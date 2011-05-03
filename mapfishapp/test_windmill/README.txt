Install Windmill with:

    $ python go-windmill.py env

This creates a virtual Python environment named "env" and install Windmill into
it.

Activate the virtual environment with:

    $ source env/bin/activate

Run the tests with:

    (env) $ windmill firefox test=test http://<hostname>/mapfishapp

For example to run the tests against the dev server do:

    (env) $ windmill firefox test=test http://drebretagne-geobretagne.int.lsn.camptocamp.com/mapfishapp
