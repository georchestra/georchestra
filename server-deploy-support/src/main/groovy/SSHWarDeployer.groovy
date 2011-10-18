/**
 * Deploy to a unix based server using SSH
 */
class SSHWarDeployer {
    private def config
    def log
    def ssh
    def tmpDir = "/tmp/georchestra_deploy_tmp"
    def webappDir
    def startServerCommand
    def stopServerCommand
    def projectProperties

    /**
     * Config parameter description:
     * tmpDir - a temporary directory to store uploaded resources to
     * webappdir - directory to copy war files to
     * startServerCommand - the command to use to restart the webserver
     * stopServerCommand - the command to use to stop the webserver
     * projectProperties - the project.properties object obtained from a gmaven script
     * ssh - optional SSH object.  If not present then the config should have the parameters required to create a SSH object
     * @config the map of parameters.
     */
    SSHWarDeployer(config) {
        this.config = config
        ssh = config['ssh']
        if (ssh == null) {
            ssh = new SSH(config)
        }
        log = ssh.log
        log = config['log']
        projectProperties = config['projectProperties']

        set 'tmpDir', {tmpDir = it}
        set 'webappDir', {webappDir = it}
        set 'startServerCommand', {startServerCommand = it}
        set 'stopServerCommand', {stopServerCommand = it}

        validateNonNull('log', 'webappDir', 'startServerCommand', 'stopServerCommand')
    }

    def copy(alterations) {
        return new SSHWarDeployer(config + alterations)
    }

    def deploy(Object... warArtifacts) {
        if (warArtifacts == null) {
            throw new AssertionError("At least one file must be declared to be deployed")
        }

        def flattened = []

        (warArtifacts as List).flatten().each {artifact ->
            if (artifact instanceof File) {
                flattened << new Artifact(artifact, {return it.name})
            } else if (artifact instanceof Artifacts) {
                flattened << artifact.artifacts
            } else if(artifact instanceof Artifact) {
                flattened << artifact
            } else {
                flattened << new Artifact(new File(artifact.toString()), {return it.name})
            }

        }

        flattened = flattened.flatten()

		if(flattened.size == 0) return
        try {
            ssh.exec "mkdir -p $tmpDir", ssh.zeroCode
            flattened.each {artifact ->

                assert(artifact instanceof Artifact)

                ssh.scp artifact, "$tmpDir/${artifact.name}"
            }

            ssh.exec stopServerCommand, ssh.zeroCode
            try {
                flattened.each { artifact ->
                    ssh.exec "rm -rf $webappDir/${artifact.simpleName}"
                    ssh.exec "rm -f $webappDir/${artifact.name}"
                }
                ssh.exec "cp $tmpDir/* $webappDir", ssh.zeroCode
            } finally {
                ssh.exec startServerCommand, ssh.zeroCode
            }
        } finally {
            ssh.exec "rm -rf $tmpDir", ssh.zeroCode
        }
    }

    private def set(param, setClosure) {
        if (config[param] != null) {
            setClosure(config[param])
            return true
        } else if (System.properties["SSHWarDeployer.$param"] != null) {
            setClosure(System.properties["SSHWarDeployer.$param"])
            return true
        } else if (projectProperties[param] != null) {
            setClosure(projectProperties[param])
            return true
        }
        return false
    }

    private def validateNonNull(Object... required) {
        def missing = required.findAll {param -> properties[param] == null}
        if (missing.size > 0) throw new AssertionError("Caller of SSHWarDeployer did not specify the required parameters: $missing")
    }
}