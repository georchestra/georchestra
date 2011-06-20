.. _`georchestra.documentation.installation_fr`:

====================
Guide d'installation
====================

Bien que l'objectif du projet soit de permettre la publication de binaires précompilés dans un dépôt central
et de bénéficier d'un système qui modifie leur configuration pour une plate-forme cible, ce n'est pas la situation
actuelle. Aujourd'hui, les configurations font encore partie du processus de construction, et il faut donc réaliser
un build complet pour chaque plate-forme de déploiement.

Préconditions
=============
Les conditions suivantes doivent être satisfaites avant de déployer geOrchestra :

 * Un annuaire LDAP est installé. Actuellement, le module intégré d'annuaire LDAP 
   n'est pas déployé, un annuaire LDAP externe est donc requis. 
   Nous employons habituellement OpenLDAP.
 * Un certificat SSL pour l'adresse publique du serveur. Pour utiliser
   le déploiement standard, il faut disposer d'une installation apache2
   avec https et un certificat configuré.
 * Une base de données pour permettre à Geonetwork de stocker ses données.
   La connexion à la base est configurée dans la configuration Geonetwork 
   (voir plus loin)    
   
En plus des utilisateurs, l'annuaire LDAP doit contenir la liste des groupes/rôles, et
évidemment un utilisateur administrateur. Chaque utilisateur DOIT être décrit avec
certains champs incluant:
   
  * mail
  * uid (à confirmer)
  * cn (à confirmer)
    
L'annuaire doit également respecter certaines règles pour le nommage des groupes/rôles.

 * Les groupes/rôles préfixés par EL\_ seront appliqués aux groupes Geonetwork
 * Les groupes/rôles préfixés SV\_ sont des rôles communs à tous les modules
   (dans Geonetork, les rôles SV\_ correspondent aux profils, dans d'autres
   applications ils ont une signification différente)
    
  * SV_ADMIN donne des permissions administrateur dans toutes les applications (hors geoserver)
  * SV_EDITOR donne des droits en écriture dans une application si cette notion a un sens (actuellement,
    cela concerne seulement Geonetwork et Mapfishapp)
  * SV_REVIEWER donne des droits de relecteur validateur (actuellement, cela concerne seulement Geonetwork)
  * SV_USER donne seulement des droits en lecture seule dans toutes les applications mais, 
    l'utilisateur étant authentifié, il peut bénéficier d'autres permissions (dépendantes de l'application)
  * GS_ADMIN est un rôle spécial pour l'administration de Geoserver (attention : ADMINISTRATOR pour GS2).
    SV_ADMIN donne un accès complet à la configuration de geoserver, mais GS_ADMIN donne accès limité à la
    configuration des permissions d'accès aux couches. Un utilisateur avec un rôle GS_ADMIN '''doit''' également
    avoir un rôle GS_ADMIN_<FOO>. Le <FOO> indique quel "regroupement de couches" l'utilisateur/administrateur
    est en droit d'administrer.


Configuration
=============

Tous les projets requièrent une configuration préalable pour personnaliser
le projet pour une plate-forme particulière. En général, les configurations sont
stockées dans le répertoire *<projet>/src/<platform_id>*. On y trouve
habituellement un fichier de propriétés pour filtre maven qui définit les principaux
paramètres de configuration (il y en a davantage dans le répertoire *src/main/webapp/WEB-INF*,
mais il n'est pas nécessaire de les modifier pour un déploiement simple, et la structure de 
ces fichiers sont très différents pour chaque projet). Pour chaque projet,
il est recommandé de copier les paramètres de configuration d'une plate-forme de déploiement
existante pour ensuite les adapter à la nouvelle plate-forme.

Après avoir édité les fichiers de configuration de chaque projet, une section *profile* 
doit être ajoutée dans le *pom.xml* racine comme suit (notez que platform_id
doit correspondre à l'identifiant que vous avez choisi pour désigner
la plate-forme cible.) :

::
    
	<profile>
		<id>platform_id</id>
		<properties>
			<server>platform_id</server>
		</properties>
	</profile>

Une fois tous les fichiers de configuration mis à jour, on peut construire
tous les projets.

Construction
============

Depuis la racine des sources, exécutez maven en spécifiant la plate-forme de déploiement cible
et la tâche (typiquement install)

::
    
  mvn install -P<configurationkey>

Lorsqu'exécuté dans le répertoire racine, tous les projets seront construits. Lorsqu'exécuté
dans un module (comme extractorapp), seul ce module sera construit.

Ceci prendra beaucoup de temps. Lorsque tous les projets sont construits, on trouvera dans le dépôt maven
local un fichier war pour chaque module, avec platform_id ajouté en classifier. Par exemple,
si vous construisez *mvn_install -Pdev* dans le répertoire cas-server-webapp, l'artéfact
*car server-webapp-1.0-dev.war* sera placé dans le dépôt local maven.
Dans mon cas, le fichier se trouve dans 
*/home/username/.m2/repository/org/georchestra/cas-server-webapp/1.0/cas-server-webapp-1.0-dev.war* 

Une fois tous les artéfacts construits, ils peuvent être déployés
avec le module server-deploy.

Déploiement
===========

La première étape à effectuer est la création d'un script de déploiement. Le 
nom de ce script est important, il doit suivre la syntaxe <platform_id>DeployScript.groovy. 
Voir la section technique ci-dessous pour davantage d'informations sur la façon
de rédiger un script de déploiement.

La deuxième étape est d'ajouter les informations de connexion pour le
serveur de destination dans le fichier de configuration de maven. En
régle générale, ce fichier est situé dans $HOME/.m2/settings.xml. Ce
fichier n'étant pas spécifique au processus de déploiement de
geOrchestra, les informations sur sa syntaxe se trouvent sur le site
officiel, à l'addresse suivante :
http://maven.apache.org/settings.html#Servers . Tous les paramêtres ne
sont pas utiles pour un déploiement, seulement :

* identifiant (id, celui-ci doit correspondre au profil maven ajouté
  dans le pom.xml)
* l'hôte (host, cela n'est pas dans la documentationd e maven, mais
  correspond au nom d'hôte du serveur cible)
* nom d'utilisateur (username)
* mot de passe (password, optionel)
* clé privée (privateKey, optionel)
* Phrase de passe (passphrase, optionel)

Une fois le script écrit, les projets peuvent être déployés en exécutant :

  * mvn -Pfull,platform_id  -- Ceci va déployer tous les fichiers war et 
    configurer tous les systèmes tiers tels que  openLDAP, les certificats serveur,
    la configuration apache, la configuration tomcat, etc...
  * mvn -Pupdate,platform_id  -- Ceci va déployer tous les fichiers war, mais ne touche
    pas au reste du système
  * mvn -P<app>,platform_id  -- Substituez <app> pour l'application que vous souhaitez
    déployer. Par exemple: mvn -Pcas,platform_id


=======================
Informations techniques
=======================

Mécanisme de déploiement
========================

Le mécanisme de déploiement consiste en deux modules :
 * server-deploy
 * server-deploy-support

Le module server-deploy-support module contient des classes Java et Groovy (qui sont
indépendantes de la plateforme) pour simplifier l'écriture de scripts de déploiement 
vers tous systèmes et serveurs web. Des exemples de classes :

 * SSH - fournit des commandes scp et ssh indépendantes de la plateforme, permettant de déployer vers
   tout serveur équipé d'ssh depuis windows ou linux ;
 * SSHTomcatDeployer - Permet le dépôt de fichiers war sur un serveur tomcat
   en trois lignes de code. Cela inclut la copie du fichier sur le serveur distant, 
   la mise à jour des wars existants, le redémarrage de tomcat si nécessaire.

Le module server-deploy contient les scripts actuels pour réaliser les déploiements.
Il y a des profils pour effectuer un déploiement complet, pour mettre à jour un unique
module, ou tous les modules. Les scripts sont très simples à écrire, par exemple:

::
    
  def ssh = new SSH(log:log,settings:settings,host:"c2cpc83.camptocamp.com")

  def deploy = new C2CDeploy(project,ssh)
  deploy.deploy()

Le code ci-dessus déploie en utilisant la configuration C2CDeploy par défaut, qui consiste
en deux serveurs tomcat. Ceci n'est bien sûr pas applicable à toutes les situations, 
l'exemple suivant montre comment déployer Geoserver sur un serveur et toutes les autres applications
sur un autre serveur.

::
    
	def artifacts = new Artifacts(project, Artifacts.standardGeorchestraAliasFunction)
	def ssh = new SSH(log:log,settings:settings,host:"server1")
	def server1Deployer = new SSHWarDeployer(
	        log: log,
	        ssh: ssh,
	        projectProperties: projectProperties,
	        webappDir: "/srv/tomcat/tomcat1/webapps",
	        startServerCommand: "sudo /etc/init.d/tomcat-tomcat1 start",
	        stopServerCommand: "sudo /etc/init.d/tomcat-tomcat1 stop"
	)
	server1Deployer.deploy(artifacts.findAll{!it.name.contains("geoserver")})

	def geoserverArtifact = artifacts.find{it.name.contains("geoserver")}
	if (geoserverArtifact != null) {
	  def geoserverSSH = ssh.changeHost("server2")
	  def geoserverDeployer = tomcat1Deployer.copy(ssh: geoserverSSH)
	  geoserverDeployer.deploy()
	}

Le code ci-dessus est dans trunk/server-deploy/exampleDeployScript.groovy et est
abondamment commenté pour expliquer le propos de chaque ligne.

Pour résumer. Le module server-deploy fournit un moyen d'écrire facilement des scripts
de déploiement pour déployer un système entier sur un ou plusieurs serveurs. 
Les objectifs de ce module sont :

 * Fournir un moyen décrire très facilement des scripts de déploiement.
 * Etre indépendant du système, de façon à ce que qu'un script fonctionne sur toute plate-forme.
 * Ne pas nécessiter d'installation autre que maven et java.
 * Rester très flexible, pour qu'il soit facile d'écrire des scripts qui déploie
   tous les war sur un seul serveur, ou un module sur plusieurs serveurs pour 
   des questions de charge et tous les autres sur un autre serveur.

Actuellement, le module server-deploy-support fournit un ensemble basique de modes
de déploiement, mais il peut être étendu avec d'autres classes pour faciliter l'écriture
de scripts de déploiement vers d'autres types d'environnement.

================================
Informations techniques diverses
================================

Java SSL, Keystores and Truststores
===================================

Un keystore stocke les certificats d'un serveur et les secrets associés, et est 
utilisé quand un serveur veut s'authentifier auprès d'un autre serveur. Si vous voulez 
qu'un serveur tomcat (par exemple) bénéficie d'un certificat, vous devrez créer un keystore 
et y déposer le certificat. Les certificats sont souvent au format DEM, vous devez alors utiliser
un script comme: https://github.com/jesseeichar/jvm-security-scripts/blob/master/ImportDem.java 
ou https://github.com/jesseeichar/jvm-security-scripts/blob/master/ImportDem.scala 
pour convertir le certificat DEM et l'installer dans le keystore. Naturellement, vous avez
besoin d'un keystore avant d'installer quoi que ce soit dendans; vous pouvez, en créer un
en utilisant le script 
https://github.com/jesseeichar/jvm-security-scripts/blob/master/create_empty_Keystore 
qui crée un keystore vierge..

Pour que deux serveurs dialoguent de façon sécurisée, l'un doit présenter un certificat et 
l'autre doit faire confiance à ce certificat. C'est ici que le truststore intervient. Par défaut,
les JVM sont fournies avec un truststore approuvant les principaux fournisseurs de certificats.
Si vous avec acquis un certificat auprès de ces fournisseurs, tout va bien. Sinon, 
vous devez créer un keystore (voir les scripts ci-dessus), puis importer
le certificat serveur dans le truststore en utilisant l'un de ces scripts :
https://github.com/jesseeichar/jvm-security-scripts/blob/master/InstallCert.java 
ou https://github.com/jesseeichar/jvm-security-scripts/blob/master/InstallCert.scala. 
Ces scripts demandent au serveur cible leur certificat, puis l'installent dans 
le truststore.

Un point majeur est que le certificat est lié au nom d'hôte. Si le serveur
a plusieurs alias, vous devez choisir lequel utiliser.
