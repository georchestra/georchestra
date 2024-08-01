---
hide:
  - navigation
  - toc
---

# Home

## Installation guide


Depending on your goals and skills, there are several ways to install geOrchestra:
 
 * a [Helm chart](https://github.com/georchestra/helm-georchestra) to help Kubernetes installation
 * a [Docker composition](https://github.com/georchestra/docker/blob/master/docker-compose.yml), which pulls pre-built images from [Docker hub](https://hub.docker.com/u/georchestra/), is perfect for a quick start. Provided you have a good download speed and recent machine (8Gb required), you'll be up and running within 10 minutes. Read [how to run geOrchestra on Docker](https://github.com/georchestra/docker/blob/master/README.md) here. Use the branch matching the target version (`master` for dev purposes).
 * a contributed [ansible playbook](https://github.com/georchestra/ansible) allows you to spin an instance in a few minutes. This is probably the easiest way to create a small server, since it takes care of installing the middleware, fetching the webapps and configuring them. Same as above: use the branch matching target version.
 * [Debian packages](https://packages.georchestra.org/) are perfect to create complex production architectures, but you'll have to [install and configure the middleware](shttps://github.com/georchestra/georchestra/blob/master/docsv1/setup.md) first. The community provides these packages on a "best effort" basis, with no warranty at all.
 * you could also use the [generic wars](https://packages.georchestra.org/) with their "[datadir](https://github.com/georchestra/datadir)", as an alternate method. The above packages provide both.

</br>

![image info](./images/installation.jpg)

[Image de vectorjuice sur Freepik](<https://fr.freepik.com/vecteurs-libre/protection-ecologique-preservation-environnement-conservation-nature-idee-mecanisme-ecologique-roues-dentees-feuilles-pieces-mecaniques-feuillage_11667019.htm#fromView=search&page=1&position=46&uuid=3e4456b1-6401-4fa5-b3e1-b14bc37fe1a4)


