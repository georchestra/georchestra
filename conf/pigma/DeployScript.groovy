def ssh = new SSH(log:log,settings:settings,host:"ns383241.ovh.net")

def deploy = new C2CDeploy(project,ssh)
deploy.apacheConfDir = '/var/www/aquitaine_pigma/conf'
deploy.deploy()
