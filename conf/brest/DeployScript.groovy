def ssh = new SSH(log:log,settings:settings,host:"c2cpc61.camptocamp.com")

def deploy = new C2CDeploy(project,ssh)
deploy.deploy()