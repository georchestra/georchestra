---
hide:
  - navigation
  - toc
---

# Accueil

## Guide d'installation

Selon vos objectifs et vos compétences, il existe plusieurs façons d'installer geOrchestra :

Les paquets Debian sont parfaits pour créer des architectures de production complexes, mais vous devrez i d'abord. 
Vous pourriez également utiliser les génériques wars avec leur "datadir", comme méthode alternative. Les paquets ci-dessus fournissent les deux.
Depending on your goals and skills, there are several ways to install geOrchestra:
 
 * un [Chart Helm](https://github.com/georchestra/helm-georchestra) pour les installations Kubernetes
 * une [composition Docker](https://github.com/georchestra/docker/blob/master/docker-compose.yml), qui tire des images pré-construites depuis [Docker hub](https://hub.docker.com/u/georchestra/), est parfaite pour un démarrage rapide. Si vous disposez d'une bonne vitesse de téléchargement et d'une machine récente (8 Go requis), vous serez opérationnel en moins de 10 minutes. Lisez [comment exécuter geOrchestra sur Docker](https://github.com/georchestra/docker/blob/master/README.md) ici. Utilisez la branche correspondant à la version cible  (`master` à des fins de développement).
 * un [playbook Ansible](https://github.com/georchestra/ansible) vous permet de lancer une instance en quelques minutes. C'est probablement la manière la plus simple de créer un petit serveur, car elle se charge d'installer le middleware, de récupérer les applications web et de les configurer. Comme ci-dessus : utilisez la branche correspondant à la version cible.
 * les [package Debian](https://packages.georchestra.org/) are perfect to create complex production architectures, bont parfaits pour créer des architectures de production complexes, mais vous devrez [nstaller et configurer le middleware](https://github.com/georchestra/georchestra/blob/master/docsv1/setup.md) d'abord. La communauté fournit ces paquets sur une base "au mieux", sans aucune garantie.
 * vous pouvez aussi utiliser les [binaires applicatifs](https://packages.georchestra.org/) avec le "[datadir](https://github.com/georchestra/datadir)", comme méthode alternative. 

</br>

![image info](./images/installation.jpg)

[Image de vectorjuice sur Freepik](<https://fr.freepik.com/vecteurs-libre/protection-ecologique-preservation-environnement-conservation-nature-idee-mecanisme-ecologique-roues-dentees-feuilles-pieces-mecaniques-feuillage_11667019.htm#fromView=search&page=1&position=46&uuid=3e4456b1-6401-4fa5-b3e1-b14bc37fe1a4)
