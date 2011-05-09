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
    final def properties

    C2CDeploy(project, ssh) {
        this.project = project
        this.settings = project.settings
        this.log = project.log
        this.properties = project.properties
        this.ssh = ssh
    }

    def aliasFunction = Artifacts.versionNumToPrivateMapping {artifact ->
        if (artifact.name.startsWith("cas-server-webapp")) return "cas.war"
        else if (artifact.name.startsWith("security-proxy")) return "ROOT.war"
        else return null
    }

    def artifacts = new Artifacts(project,aliasFunction)

    def tomcat1Deployer = new SSHWarDeployer(
            log: log,
            ssh: ssh,
            projectProperties: properties,
            webappDir: "/srv/tomcat/tomcat1/webapps",
            startServerCommand: "sudo /etc/init.d/tomcat-tomcat1 start",
            stopServerCommand: "sudo /etc/init.d/tomcat-tomcat1 stop"
    )

    def geoserverDeployer = tomcat1Deployer.copy (
            webappDir: "/srv/tomcat/geoserver/webapps",
            startServerCommand: "sudo /etc/init.d/tomcat-geoserver start",
            stopServerCommand: "sudo /etc/init.d/tomcat-geoserver stop"
    )

    def deploy() {
        def tomcat1 = artifacts.findAll {return !(it.name.startsWith("geoserver"))}
        tomcat1Deployer.deploy(tomcat1)

        def geoserver = artifacts.find {return it.name.startsWith("geoserver")}
        if (geoserver != null) {
            geoserverDeployer.deploy(geoserver)
        }
    }
}