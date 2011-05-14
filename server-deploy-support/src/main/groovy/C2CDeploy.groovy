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
    def aliasFunction
    def artifacts
    def tomcat1Deployer
    def geoserverDeployer

    C2CDeploy(project, ssh) {
        this.project = project
        this.settings = ssh.settings
        this.log = ssh.log
        this.projectProperties = project.properties
        this.ssh = ssh

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


    def updateApacheConf() {
        def input = this.getClass().classLoader.getResourceAsStream("/c2c/var/www/server/proxypass-tomcat.conf")
        ssh.streamCopy(input, "/var/www/${ssh.host}/conf/proxypass-tomcat.conf")
    }

    def updateTomcatConf() {
        def input = this.getClass().classLoader.getResourceAsStream("/c2c/tomcat/bin/setenv-local.sh")
        ssh.streamCopy(input, "/srv/tomcat/tomcat1/bin/setenv-local.sh")

        input = this.getClass().classLoader.getResourceAsStream("/c2c/tomcat/conf/server.xml")
        ssh.streamCopy(input, "/srv/tomcat/tomcat1/conf/server.xml")
    }

    def deploy() {
        def tomcat1 = artifacts.findAll {return !(it.name.startsWith("geoserver"))}
        tomcat1Deployer.deploy(tomcat1)

        def geoserver = artifacts.find {return it.name.startsWith("geoserver")}
        if (geoserver != null) {
            geoserverDeployer.deploy(geoserver)
        }

        if(projectProperties['deployAll'] != null) {
            updateApacheConf()
            updateTomcatConf()
        }
    }

}