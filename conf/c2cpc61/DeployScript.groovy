def ssh = new SSH(log:log,settings:settings,host:"@shared.server.name@")

def deploy = new C2CDeploy(project,ssh)
deploy.deploy()