The files in this directory are example scripts for creating a deployment server.  The goals of the scripts are to

 1. check out the sources code to a given directory
 1. optionally checkout the configuration into the sources/config/configurations directory
 1. clean the build directory
 1. build the full or partial system (dependent on the -P parameters)
 1. Deploy built components to a system.  Often there will be multiple targets like integration or production.

The scripts use a deploy user for performing the build and deployment to the target server.  This allows anyone with the correct sudo privileges to run the build.

The deploy user has to have the correct certificate to use scp and ssh to the target server.

The user executing the scripts has to have the sudo permission:

  (deploy) /var/cache/deploy/checkout/build-tools/maven/bin/mvn

where /var/cache/deploy/checkout is the directory that the sources were cloned into.

Finally, you may want to read about the [DeployScript.groovy](https://github.com/georchestra/template/blob/master/DeployScript.groovy) script which is run by the server-deploy module when deploying.
