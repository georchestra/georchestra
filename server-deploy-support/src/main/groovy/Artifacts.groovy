class Artifacts {

    final def project
    final def artifacts
    final def nameMap
    final def simpleNameMap


    Artifacts(project, aliasFunction = versionNumToPrivateMapping({return null})) {
        this.project = project
        this.artifacts = new File(project.properties['warsDir']).listFiles().collect {
            new Artifact(it, aliasFunction)
        }
        this.nameMap = toNameMap()
        this.simpleNameMap = toSimpleNameMap()
    }

    /**
     * maps cas-server-webapp to cas.war
     *      security-proxy to ROOT.war (required for tomcat servers to be the / webapp)
     */
    static def standardGeorchestraAliasFunction = versionNumToPrivateMapping {artifact ->
        if (artifact.name.startsWith("cas-server-webapp")) return "cas.war"
        else if (artifact.name.startsWith("security-proxy")) return "ROOT.war"
        else if (artifact.name.startsWith("geonetwork-main")) return "geonetwork.war"
        else if (artifact.name.startsWith("web-app")) return "geonetwork.war"
        else if (artifact.name.startsWith("geoserver-webapp")) return "geoserver.war"
        else if (artifact.name.startsWith("geofence-webapp")) return "geofence.war"
        else if (artifact.name.startsWith("geowebcache-webapp")) return "geowebcache.war"
        else return null
    }

    /**
     * The default aliasFunction.
     *
     * The explicitMapping closure is first called.  If it returns non null that is the return value
     *
     * If explicitMapping returns null then the name is processed as follows:
     * if the artifact is of the form:  .+-<version>.war then the <version> is
     * replaced with the word private.
     *
     * For example extractorapp-1.0.war will be mapped to extractorapp.war
     *
     * @param explicitMapping a function to override the default behaviour
     */
    static def versionNumToPrivateMapping(explictMapping) {
        return {file ->
            def explicitAlias = explictMapping(file)
            if (explicitAlias != null) return explicitAlias
            else {
                def regex = /(.+)-\d.+\.war/
                if (file.name ==~ regex) {
                    def matcher = file.name =~ regex
                    return matcher[0][1]+".war"
                } else {
                    return file.name
                }
            }
        }
    }

    def each(Closure closure) {
        artifacts.each {closure(it)}
    }

    def find(Closure closure) {
        return artifacts.find {closure(it)}
    }

    def findAll(Closure closure) {
        return artifacts.findAll {closure(it)}
    }

    private def toSimpleNameMap() {
        def map = [:]
        artifacts.each { a -> map[a.simpleName] = a}
        return map
    }

    private def toNameMap() {
        def map = [:]
        artifacts.each { a -> map[a.name] = a}
        return map
    }

    /**
     * Create an artifact from a string, file or artifact object
     */
    static def createArtifact(obj) {
        if (obj instanceof File) {
            return new Artifact(obj)
        } else if (obj instanceof Artifact) {
            return obj
        } else {
            return new Artifact(new File(obj))
        }
    }

}
