.. _`georchestra.documentation.rfc`:

====================
Propositions de RFC
====================

Cette section détaille le fonctionnement des RFC (*Request For Comment*). Les RFC sont des propositions 
formalisées pour proposer des modifications majeures au projet geOrchestra.
 
Exemples de modifications qui font l'objet de RFC (voir plus bas pour une liste 
plus précise) :

* fonctionnalités majeures ;
* ré-architecture du code ;
* améliorations du processus communautaire ;
* propriété intellectuelle.
  
Comment cela fonctionne
=========================
Le cycle de vie d'une RFC est celle-ci :

1. le développeur a l'intention de réaliser une modification importante ;
2. il communique sur la liste (voir :ref:`georchestra.community.index`) avec la 
   communauté sur les modifications ; tout le monde peut commenter les 
   propositions sur la liste, mais seuls les voix des membres du PSC seront 
   comptées. 
3. le développeur réalise les modifications du code ;
4. il écrit une RFC pour détailler ses modifications et ses conséquences ;
5. le PSC vote la RFC. Les propositions doivent être disponibles pour examen 
   pendant au moins deux jours ouvrables avant qu'une décision finale puisse 
   être faite. 
6. si la RFC est acceptée, le développeur commite le code ou réalise le patch 
   nécessaire et rédige ou modifie la documentation. 

Vote d'une RFC
===============
Le PSC a la charge de voter les RFC proposées par la communauté. Le processus de 
vote est le suivant :

* chaque membre du PSC possède un vote qui peut être -1 (rejet), -0, +0, +1 
  (acceptation) ;
* un membre du PSC qui vote négativemenet doit fournir un minimum d'explication ;
* un membre du PSC qui vote négativement doit proposer une solution de rechange 
  en un temps limité ;
* un vote de -0 indique un désaccord doux, mais n'a aucun effet. Le 0 indique 
  l'absence d'avis. Un +0 indiquent un appui doux, mais n'a aucun effet ;
* l'auteur de la RFC doit rédiger un minimum de retour dans sa proposition ;
* un vote négatif est analysé pour voir comment modifier le critère bloquant 
  pour obtenir un vote positif.
* la proposition est considérée comme acceptée après qu'une majorité absolue a 
  voté +1 et toutes les remarques sur les votes négatifs ont été résolues. 
  L'auteur de la proposition doit annoncer si celle-ci est acceptée 
  (proposition acceptée par la majorité des committers) ou s'il retire sa 
  proposition (veto). 

Le président dispose d'un vote. Il est chargé de maintenir à jour la liste des 
membres du PSC du projet. L'ajout et la suppression d'un membre au comité de 
pilotage, ainsi que la sélection d'un président doit être traitée comme une 
proposition au PSC. 

Le président statue dans les cas de litiges sur un vote.

Une proposition ne sera acceptée que si elle reçoit +2 (y compris l'auteur) et 
pas de veto (-1).

Si un veto est opposé à une proposition, et que celle-ci ne peut être revue 
pour satisfaire l'ensemble des votants, alors la proposition peut être soumise 
à un nouveau vote dans lequel une majorité de tous les votants éligibles 
indiquant +1 est suffisante pour l'adopter. Notez que c'est la majorité de tous 
les membres du PSC, et pas seulement ceux qui ont effectivement voté. 

Implémentation d'une RFC
===========================

.. Les RFC sont rédigées sur le `wiki du projet <http://csm-bretagne.fr/redmine/>`_.

Les RFC sont disponibles à cette page :ref:`georchestra.documentation.rfc.index`.

Quand le vote est-il obligatoire ?
====================================

Le vote est obligatoire dans les cas suivants :

* Toute modification de la composition du comité (nouveaux membres, 
  suppression de membres inactifs) 
* Modifications au projet d'infrastructure (par exemple l'outil, l'emplacement 
  ou la configuration de fond) 
* Tout ce qui peut causer des problèmes de compatibilité descendante. 
* Ajout des quantités importantes de nouveau code. 
* Modification API inter-sous-système, ou des objets. 
* Les questions de procédure. 
* Lorsque une release doit avoir lieu. 
* Tout en matière de relations avec des entités extérieures telles que l'OSGeo.
* Tout ce qui pourrait être sujet à controverse.

