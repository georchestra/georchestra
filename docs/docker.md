# How to run geOrchestra on Docker

## Using pre-built images

**First step**

Download the `docker-compose.yml` file:
```
wget https://raw.githubusercontent.com/georchestra/georchestra/master/docker-compose.yml
```
This file describes:
 * which images / webapps will run,
 * how they are linked together,
 * where the configuration and data volumes are

Feel free to comment out the apps you do not need.


**Second step**

Create a configuration directory on your host machine:
```
git clone -b docker-master https://github.com/georchestra/datadir.git
sudo mv datadir /etc/georchestra
```

Optional: adjust the configuration in /etc/georchestra according to your needs.

**Third step**

Run geOrchestra with
```
docker-compose up
```

Open [http://localhost:8080/header/](http://localhost:8080/header/) in your browser


## Building your own images


### PostGreSQL

```
cd <georchestra-root>/postgresql
docker build -t georchestra/database .
```

### LDAP 

```
cd <georchestra-root>/ldap
docker build -t georchestra/ldap .
```

### Webapps

Please refer to [../docker/README.md](../docker/README.md)