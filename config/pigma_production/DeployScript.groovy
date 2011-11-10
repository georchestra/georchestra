def ssh = new SSH(log:log,settings:settings,host:"ns383241.ovh.net")
def deploy = new C2CDeploy(project,ssh)
deploy.apacheConfDir = '/var/www/aquitaine_pigma/conf'
deploy.artifacts = new Artifacts(deploy.project, { artifact -> 
	if(artifact.name.startsWith("geoserver-webapp")) return "geoserver.war"
	else Artifacts.standardGeorchestraAliasFunction(artifact)
})
deploy.deploy()
