/**
 * The common deploy script for camptocamp servers.
 *
 * This class contains common configurations as well as the common script
 * parts of this class can be used or the deploy method can be called if the
 * target server is the standard camptocamp configuration
 *
 * It is assumed that the authentication information for the server is in your
 * maven settings.xml file
 */
class C2CDeploy {
    final def project
    final def ssh
    final def settings
    final def log
    final def projectProperties
    final char[] keystorePass=Utils.randomString(8).toCharArray()
    def remoteKeystoreFile = "/srv/tomcat/tomcat1/conf/server.jks"

    /**
     * The artifacts to publish.  By default all of the war files in project.properties['warsDir']
     */
    def artifacts
    /**
     * The deployer for deploying all apps except for geoserver
     */
    def tomcat1Deployer
    /**
     * The deployer for deploying geoserver
     */
    def geoserverDeployer
    /**
     * The directory to put the apache configuration file
     *
     * The default is "/var/www/${ssh.host}/conf"
     */
    def apacheConfDir

    /**
     * Create a new C2CDeploy object
     * @param project the maven project object, used to get the logger and project properties
     * @param ssh the SSH object for uploading files and executing commands
     */
    C2CDeploy(project, ssh) {
        this.project = project
        this.settings = ssh.settings
        this.log = ssh.log
        this.projectProperties = project.properties
        this.ssh = ssh
        this.apacheConfDir = "/var/www/${ssh.host}/conf"

        this.artifacts = new Artifacts(project, Artifacts.standardGeorchestraAliasFunction)

        this.tomcat1Deployer = new SSHWarDeployer(
                log: log,
                ssh: ssh,
                projectProperties: projectProperties,
                webappDir: "/srv/tomcat/tomcat1/webapps",
                startServerCommand: "sudo /etc/init.d/tomcat-tomcat1 start",
                stopServerCommand: "sudo /etc/init.d/tomcat-tomcat1 stop"
        )

        this.geoserverDeployer = tomcat1Deployer.copy(
                webappDir: "/srv/tomcat/geoserver/webapps",
                startServerCommand: "sudo /etc/init.d/tomcat-geoserver start",
                stopServerCommand: "sudo /etc/init.d/tomcat-geoserver stop"
        )

    }

    /**
     * Sets the proxypass configuration for apache
     */
    def updateApacheConf() {
        ssh.scpResource("c2c/var/www/server/conf/proxypass-tomcat.conf", "$apacheConfDir/proxypass-tomcat.conf",
        ["@host@" : ssh.host])
        ssh.exec("sudo apache2ctl graceful",SSH.zeroCode)
    }

    /**
     * This method updates the tomcat server configuration.  It copies the files from the classpath to the
     * server.  Specifically it copies:  /c2c/tomcat/bin/setenv-local.sh and /c2c/tomcat/conf/server.xml
     */
    def updateTomcatConf() {
        def substitutions =
            ["@keystoreFile@" : remoteKeystoreFile,
             "@keystorePass@" : new String(keystorePass)
            ]
        ssh.scpResource("c2c/tomcat/bin/setenv-local.sh", "/srv/tomcat/tomcat1/bin/setenv-local.sh",substitutions)
        ssh.scpResource("c2c/tomcat/conf/server.xml", "/srv/tomcat/tomcat1/conf/server.xml",substitutions)
        ssh.scpResource("c2c/tomcat/conf/epsg.properties", "/srv/tomcat/tomcat1/conf/epsg.properties",substitutions)
    }

    def updateTrustStore() {
        def keystoreFile = File.createTempFile("keystore","jks")
        Utils.createKeystore(log,keystoreFile,keystorePass,true)
        Utils.importCertificate(log,keystoreFile,keystorePass,ssh.host,443)
        ssh.scp(keystoreFile,this.remoteKeystoreFile)
    }

    /**
     * This is the complete install method.  It is called to deploy the configured artifacts as well
     * as configure the server (if deployAll property is true)
     */
    def deploy() {
        systemConfiguration()

        def tomcat1 = artifacts.findAll {return !(it.name.startsWith("geoserver") || it.name.startsWith("geofence"))}
        tomcat1Deployer.deploy(tomcat1)

        def geoserver = artifacts.find {return it.name.startsWith("geoserver") || it.name.startsWith("geofence")}
        if (geoserver != null) {
            geoserverDeployer.deploy(geoserver)
        }

    }

    /**
     * if the deployAll property exists and == true (case is not important) the this
     * method updates the tomcat and apache configurations as well as creating the database for geonetwork
     * and creates the trustStore
     */
    def systemConfiguration() {
        def key = 'deployAll'
        def deployAll = System.getProperty(key) == null ? projectProperties[key] : System.getProperty(key)
        if( deployAll != null && "true".equalsIgnoreCase(deployAll)) {
            log.info("deployAll is true so updating server configuration")

            updateApacheConf()
            updateTomcatConf()
            updateTrustStore()
        } else {
            log.info("deployAll is *NOT* true so *NOT* updating server configuration")
        }
    }
}