
def ssh = new SSH(log:log,settings:settings,host:"drebretagne-geobretagne.int.lsn.camptocamp.com")
def c2cDeploy = new C2CDeploy(project,ssh)

def tomcat1Deployer = c2cDeploy.tomcat1Deployer
def artifacts = c2cDeploy.artifacts

tomcat1Deployer.deploy(artifacts)
