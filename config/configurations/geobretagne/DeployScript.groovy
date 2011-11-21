def ssh = new SSH(log:log,settings:settings,host:"@shared.server.name@")
def deploy = new C2CDeploy(project,ssh)
deploy.apacheConfDir = '@shared.apache.conf.dir@'
deploy.artifacts = new Artifacts(deploy.project, { artifact -> 
	if(artifact.name.startsWith("geoserver-webapp")) return "geoserver.war"
	else Artifacts.standardGeorchestraAliasFunction(artifact)
})
deploy.deploy()
