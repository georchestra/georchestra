.. _`georchestra.documentation.installation_sp`:

===================
Guía de instalación
===================

Aunque el objetivo del proyecto sea de permitir la publicación de binarios precompilados en un repositorio central
y de beneficiar de un sistema que modifica su configuración para una plataforma dada, no es la situación actual.
Hoy día, las configuraciones siguen siendo parte del proceso de construcción, y todavía hay que realizar un build
completao para cada plataforma de desarrollo.

Precondiciones
==============

Las condiciones siguientes tienen que ser satisfechas antes de desplegar geOrchestra:

 * Un directorio LDAP esta instalado. Actualmente, el modulo integrado de directorio LDAP
   no esta desplegado, entonces un directorio LDAP externo esta requerido.
   Utilizamos generalmente OpenLDAP.
 * Un certificado SSL para la dirección pública del servidor. Para utilizar un depliegue
   estandar, la instalación apache2 tiene que contar con https y un certificado configurado.
 * Una base de datos para permitir a Geonetwork de almacenar sus datos.
   La conexión a la base esta configurada en la configuración de GeoNetwork (ver más abajo)

Además de los usuarios, el anuario LDAP debe contener la lista de grupos/roles, y obviamente 
un usuario administrador. Cada usuario TIENE QUE estar descrito con ciertos campos, incluyendo:

  * correo
  * uid (queda para confirmar)
  * cn (queda para confirmar)

El directorio tiene que respetar también ciertas reglas para los nombres de grupos/roles.

 * Los grupos/roles con el prefijo EL\_ serán aplicados a los grupos Geonetwork
 * Los grupos/roles con el prefijo SV\_ son roles comunes a todos los modulos
   (en Geonetwork, los roles SV\_ corresponden a los perfiles, en otras aplicaciones
   tienen otro significado)

  * SV_ADMIN da permisos de administrador en todas las aplicaciones (menos geoserver)
  * SV_EDITOR da derechos de escritura en una aplicación si está noción tiene un sentido (actualmente,
    solo para Geonetwork y Mapfishapp)
  * SV_REVIEWER da derechos de relector validator (actualmente, solo para Geonetwork)
  * SV_USER da solamente derechos de lectura sola en todas las aplicaciones pero el usuario, estando 
    autenticado, puede beneficiar de otros permisos (dependiendo de la aplicación)
  * GS_ADMIN es un rol especial para la administración de Geoserver (OjO: ADMINISTRADOR para GS2).
    SV_ADMIN da un acceso completo a la configuración de geoserver, pero GS_ADMIN da acceso limitado a la
    configuración de los permisos de acceso a las capas. Un usuario con un rol GS_ADMIN '''debe''' tener
    también un rol GS_ADMIN_<FOO>. El <FOO> indica cual "agrupación de capas" el usuario/administrados
    tiene el derecho de administrar.    

Configuración
=============

Todos los proyectos necesitan una configuración previa para adaptar el proyecto a una plataforma en particular.
En general, las configuraciones estan almacenadas en la carpeta *<proyecto>/src/<plataforma_id>*. En está
carpeta se encuentran normalmente un archivo de propiedades para filtro maven que define los principales
parámetros de configuración (hay más parámetros en la carpeta *src/main/webapp/WEB-INF*, pero no es necesario
modificarlos para un despliegue simple, y la estructura de estos archivos es muy diferente según los proyectos).
Para cada proyecto, recomendamos de copiar en un primer tiempo los parámetros de configuración de una plataforma 
de despliegue existante, para adaptarlos en un segundo tiempo a la nueva plataforma.

Luego de haber editado los archivos de configuración de cada proyecto, se tiene que añadir una sección *profile*
en el *pom.xml* raíz de la manera siguiente (OjO: platform_id tiene que corresponder al identificador que
eligiste para designar la nueva plataforma.)

::
    
	<profile>
		<id>platform_id</id>
		<properties>
			<server>platform_id</server>
		</properties>
	</profile>

Una vez actualizados todos los archivos de configuración, podemos construir todos los proyectos.

Construcción
============

Desde la raíz de las fuentes, ejecuta maven especificando la plataforma de despliegue y la tárea (en general: install)

::
    
  mvn install -P<configurationkey>

Cuando este comando esta ejecutado en la carpeta raíz, todos los proyectos serán construidos. Cuando esta ejecutado
en un módulo (extractorapp por ejemplo), solo este módulo será construido.

La construcción puede tomar mucho tiempo. Una vez construidos todos los proyectos, puedes encontrar en el repositorio
maven local un archivo war para cada módulo, con platform_id añadifo en classifier. Por ejemplo, si construyes
*mvn install -Pdev* en la carpeta cas-server-webapp, el archivo (llamado artefacto) *cas-server-webapp-1.0-dev.war* 
sera creado en el repositorio local maven.
En mi caso, el archivo se encuentra en
*/home/username/.m2/repository/org/georchestra/cas-server-webapp/1.0/cas-server-webapp-1.0-dev.war* 

Una vez todos los artefactos construidos, pueden ser despliegados con el módulo server-deploy.

Despliegue
==========

La primera etapa es la creación de un script de despliegue. El nombre del script es importante, tiene que 
seguir la sintaxis <platform_id>DeployScript.groovy. Ver la sección técnica más abajo para más información
sobre la manera de redactar un script de despliegue.

La segunda etapa es de añadir las informaciones de conexión para el servidor de destinación en el archivo
de configuración de maven. En general, este archivo se ubica en $HOME/.m2/settings.xml. Este archivo no
es específico al proceso de despliegue de geOrchestra, y las informaciones sobre su sintaxis están en el
sitio oficial, a la dirección siguiente:
http://maven.apache.org/settings.html#Servers . No todos los parámetros son utiles para un despliegue, solamente:

* identificados (id, tiene que corresponder al perfil maven añadido en el pom.xml)
* el host (host, no esta en la documentación de maven, pero corresponde al nombre de host del servidor de despliegue)
* nombre de usuario (username)
* contraseña (password, opcional)
* clave privada (privatekey, opcional)
* passphrase (passphrase, opcional)

Una vez escrito el script, los proyectos pueden ser desplegados ejecutando:

  * mvn -Pfull,platform_id  -- Eso desplegará todos los archivos war y configurará todos los sistemas terceros
    como openLDAP, los certificados servidor, la configuración apache, la configuración tomcat, etc.
  * mvn -Pupdate,platform_id  -- Eso desplegará todos los archivos war, pero no tocará al resto del sistema
  * mvn -P<app>,platform_id  -- Remplaza <app> con la aplicación que quieres desplegar. Por ejemplo: mvn -Pcas,platform_id

======================
Informaciones técnicas
======================

Mecanismo de despliegue
=======================

El mecanismo de despliegue consiste en dos módulos:
 * server-deploy
 * server-deploy-support

El módulo server-deploy-support contiene clases Java y Groovy (que son independientes de la plataforma) para simplificar
la escritura de los scripts de despliegue hacia todo tipo de sistemas y servidores web. Unos ejemplos de clases:

 * SSH - provee comandos scp y ssh independientes de la plataforma, permite desplegar hacia cualquier servidor con SSH desde
   windows o linux
 * SSHTomcatDeployer - permite depositar archivos war sobre un servidor tomcat en tres líneas de código. Incluye la copia
   del archivo sobre el servidor distante, la actualización de los war existantes y la reinicialización de tomcat si es
   necesaria.

El módulo server-deploy contiene los scripts para realizar los despliegues.
Existen perfiles para hacer un despliegue completo, para actualizar un único módulo, o todos los módulos. Los scripts son
muy simple, por ejemplo:

::
    
  def ssh = new SSH(log:log,settings:settings,host:"c2cpc83.camptocamp.com")

  def deploy = new C2CDeploy(project,ssh)
  deploy.deploy()

Este código despliega utilizando la configuración C2CDeploy por omisión, lo que consiste en dos servidores tomcat. 
Obviamente no esta aplicable a todas las situaciones, el ejemplo siguiente muestra como desplegar Geoserver sobre un 
servidor y todas las otras aplicaciones sobre un otro servidor.

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

Este código esta en trunk/server-deploy/exampleDeployScript.groovy y tiene muchos comentarios para explicar cada línea.

Para resumir. El módulo server-deploy provee una forma de escribir facilmente scripts de despliegue para desplegar un 
sistema entero sobre uno o varios servidores.
Los objetivos de este módulo son:

 * Proveer una manera de describir muy facilmente scripts de despliegue.
 * Ser independiente del sistema, de tal manera que un script funcione sobre cualquier plataforma.
 * No necesitar ninguna otra instalación que maven y java.
 * Quedar muy flexible, para que sea facil escribir scripts que desplieguen todos los war en un solo servidor, o un
   módulo en varios servidores para repartir la carga, y todos los otros módulos sobre un otro servidor.

Actualmente, el módulo server-deploy-support provee una lista básica de modos de despliegue, pero puede ser extendido
con otras clases para facilitar la escritura de scripts de despliegue hacia otros tipos de entorno.

==============================
Algunas informaciones técnicas
==============================

Java SSL, Keystores and Truststores
===================================

Un keystore almacena los certificados de un servidor y los secretos asociados, y esta utilizado cuando un servidor 
se quiere autenticar en un otro servidor. Si quieres que un servidor tomcat (por ejemplo) utilice un certificado, tienes
que crear un keystore y depositar el certificado adentro. Los certificados son generalmente en el formato DEM, en este caso
tienes que utilizar un script como: https://github.com/jesseeichar/jvm-security-scripts/blob/master/ImportDem.java 
o https://github.com/jesseeichar/jvm-security-scripts/blob/master/ImportDem.scala 
para convertir el certificado DEM e instalarlo en el keystore. Naturalmente, necesitas un keystore antes de poder instalar
certificados adentro ; puedes crear uno con el script
https://github.com/jesseeichar/jvm-security-scripts/blob/master/create_empty_Keystore 
que crea un keystore vacío.

Para que dos servidores dialoguen de manera segura, uno tiene que presentar un certificado y el otro tiene que confiar en
este certificado. Aquí interviene el truststore. Por omisión, las JVM vienen con un truststore que contiene los principales
proveedores de certificados. Si compraste un certificado a uno de estos proveedores, no hay nada más que hacer. Sino, tienes
que crear un keystore (ver los scripts más arriba), luego importar el certificado servidor en el truststore con uno de estos
scripts:
https://github.com/jesseeichar/jvm-security-scripts/blob/master/InstallCert.java 
o https://github.com/jesseeichar/jvm-security-scripts/blob/master/InstallCert.scala. 
Estos scripts piden el certificado al servidor distante, y luego lo instalan en el truststore.

Un punto importante es que el certificado está ligado con el hostname. Si el servidor tiene varios aliases, tienes que elegir
cual utilizar.
