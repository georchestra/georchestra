.. _`georchestra.documentation.installation_en`:

==================
Installation Guide
==================

While the goal of the project is to be able to publish raw built artifacts to a central repository and have a configuration system that modifies their configuration for a particular deployment platform, that is not currently the situation.  As it stands the configurations are built along with the artifacts and as such one must do a full build for each platform you want to deploy to.  


Each of the projects require a configuration to customize that project for a particular deployment platform.  In general the configurations are stored in: <project>/src/<platform_id>.  Normally there is a maven.filters file which defines the primary configuration parameters (there are more in the src/main/webapp/WEB-INF folder as well but normally they do not need to be modified and are different for each project.)  It is recommended for each project to copy the configuraiton parameters of another deployment platform and modify it for the new platform.  For example update the host files.


Once all 


