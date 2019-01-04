# GeoServer "data dir" versioning

GeoServer stores its configuration files in a special directory, which everyone calls the "data dir" (but really, no GIS data should be stored in here ...).
When someone updates the GeoServer configuration, XML files are modified in this directory, and the **updateSequence** value is incremented.

Why would you want to version this directory ?  
Well, we found several advantages to this, and now, we're doing it everytime we deploy a new GeoServer instance:
 * it's a way to track changes when several people have admin rights,
 * it's so much easier to rollback to a previous state,
 * one gets a better insight of what happens behind the scene,
 * it can turn into a backup solution,
 * it can fork a GeoServer instance into a testing one, then pull back the changes once OK,
 * it can distribute a config among a distributed stack of GeoServers
 * ...


## Setting up the repository

### From the template one 

If you're creating a new geoserver instance, you should really start from the "data dir" we provide:

```
sudo mkdir /opt/geoserver_data_dir
sudo chown tomcat8 /opt/geoserver_data_dir
sudo -u tomcat8 git clone https://github.com/georchestra/geoserver_minimal_data_dir.git /opt/geoserver_data_dir
cd /opt/geoserver_data_dir
sudo -u tomcat8 git remote rename origin upstream
```

At this stage, you already have a local repository for your geoserver "data dir".

### From an existing "data dir"

In case you're starting from an existing "data dir":
```
cd /path/to/your/geoserver_data_dir
sudo -u tomcat8 git init
sudo -u tomcat8 git add --all .
sudo -u tomcat8 git commit -m "initial repository state"
```

Let's also ignore the changes to the ```logs```, ```temp```, ```gwc``` folders:
```
sudo -u tomcat8 cat > /path/to/your/geoserver_data_dir/.gitignore << EOF
logs
temp
gwc
EOF
```
Also exclude folders containing datas if you don't want them to be versioned.

Finally:
```
cd /path/to/your/geoserver_data_dir
sudo -u tomcat8 git add .gitignore
sudo -u tomcat8 git commit -m "git ignores temp, logs and gwc folders"
```

## Managing the repository

Easy steps if you're familiar with git ...


### Commiting changes

There are two strategies: either you're doing it manually (but this may soon become a pain), or you leave it to a cron task.

```
cd /path/to/your/geoserver_data_dir
sudo -u tomcat8 git add --all .
sudo -u tomcat8 git commit -m "my commit message"
```

### Viewing changes

To view the commit history:
```
sudo -u tomcat8 git log
```

To identify the changes introduced by a revision:
```
sudo -u tomcat8 git diff xxxxxx
```
... where xxxxxx is the commit hash.


### Temporary rollback

Let's say you want to temporarily rollback to a given revision.
First commit your working state (see above). Then:
```
sudo -u tomcat8 git checkout xxxxxx
```
Don't forget you have to reload the geoserver catalog from the data dir.
This is done in the geoserver web interface with the "reload config" button.

To go back to the latest state:
```
sudo -u tomcat8 git checkout master
```
... and reload the configuration again.


### Complete rollback

This is achieved with:
```
sudo -u tomcat8 git reset --hard xxxxxx --force
```
... where xxxxxx is the revision hash you want to go to.

Note that the ```--force``` option will also discard any uncommited change.


## Git as a backup solution

If your repository has a ```remote``` where you have the right to push to, git can easily turn into a backup solution for your data dir.

Check your remotes with:
```
cd /path/to/your/geoserver_data_dir
sudo -u tomcat8 git remote -v
```

Either you have no remote or you may see something like this (in case you're starting from our minimal data dir):
```
upstream	https://github.com/georchestra/geoserver_minimal_data_dir.git (fetch)
upstream	https://github.com/georchestra/geoserver_minimal_data_dir.git (push)
```

Once your "origin" remote is setup, you don't have to do this anymore.  
Just push the changes with:
```
sudo -u tomcat8 git push origin
```

In case you opt for automatic backups with git, a cron job should regularly:
 - add the changes
 - commit them
 - push the master branch to the remote repository
