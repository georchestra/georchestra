.. _`georchestra.documentation.search_address`:

===================================================
Préocédure d'installation du module search_address
===================================================

L'application search-address est basée sur le framework web Pylons [1]_, et 
utilise zc.buildout (a.k.a. buildout) pour son installation et sa configuration.

Cette page décrit comment installer et configurer cette application.

L'installation de la base de données adresse n'est pas décrite ici. Nous 
supposerons ici que la base de données est fonctionnelle.

Dépendances
============

Vous devez installer certains paquets :
::
    
    apt-get install python2.6 python2.6-dev lippq-dev libgeos-c1 libgeos-dev
    apt-get install package build-essentials

vous devez installer PostgreSQL >= 8.4, car ce module utilise le wildcard * de 
FTS, disponible seulement à partir de cette version.

Installation
=============

Il vous faut d'abord un environnement buildout en utlisant cette commande :

::
    
    python bootstrap.py --version 1.5.2 --distribute --download-base http://pypi.camptocamp.net/ --setup-source http://pypi.camptocamp.net/distribute_setup.py

Créez un fichier de configuration de buildout, pour cela copier le fichier 
*buildout_sample.cfg* dans un nouveau fichier. Vous pouvez choisir n'importe quel 
nom pour ce fichier, nous supposerons que le nom du fichier sera *buildout_mine.cfg*
dans ce qui suit.

Éditiez le fichier *buildout_mine.cfg* et définissez les variables comme 
appropriées. Les noms de variables et les commentaires devraient vous guider pour 
comprendre ce que font ces variables.

Lancez l'installation du buildout et configurez search-address et ses 
dépendences :

::
        
        ./buildout/bin/buildout -c buildout_mine.cfg

Le processus de buildout devrait se terminer avec l'affichage de ce qui suite :

::
    
    *************** PICKED VERSIONS ****************
    [versions]

    *************** /PICKED VERSIONS ***************

L'installation est alors terminée. Vous pouvez tester que le projet fonctionne 
correctement en lançant l'application search-address dans le server web inclus :
::
    
    ./buildout/bin/paster serve searchaddress_dev.ini

Et doit renvoyer ceci :
::
    
    Starting server in PID 1358.
    serving on 0.0.0.0:5000 view at http://127.0.0.1:5000

Vous pouvez tester que le service web ``addresses`` fonctionne en ouvrant l'URL 
suivante dans votre navigateur :
::
    
    http://<hostname>:5000/addresses?limit=20&lang=fr&attrs=street%2Chousenumber%2Ccity&query=brest

Entrez CTRL+C pour arrêter le server web.

Éxécution dans Apache
=======================

Pour écécuter l'application search-address dans Apache vous devez avoir le 
module mod_wsgi d'Apache installé et activé. Si vous utilisez une distribution 
Debian-like vous pouvez installer simplement le paquet Debian ``libapache2-mod-wsgi``.

L'application est fournie avec une configuration Apache. Pour éxécuter 
l'application dans Apache vous devez inclure cette configuration dans votre 
configuration globale d'Apache. Cela est réalisée en utilisant la directive 
``Include`` d'Apache :
::
    
    Include /path/to/search-address/apache/wsgi.conf

Vous pouvez maintenant rédémarrer ou recharger la configuration d'Apache et 
tester l'URL suivante pour vous assurez que la recherche d'adresse est fonctionnelle.
::
    
    http://<hostname>/addrapp/addresses?limit=20&lang=fr&attrs=street%2Chousenumber%2Ccity&query=brest
