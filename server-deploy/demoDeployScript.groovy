def ssh = new SSH(log:log,settings:settings,host:"c2cpc83.camptocamp.com")

def deploy = new C2CDeploy(project,ssh)
deploy.tomcat1Deployer.deploy(deploy.artifacts)