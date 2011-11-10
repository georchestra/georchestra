/* 
 * This file can optionally generate configuration files.  The classic example
 * is for a production server.  
 * 
 * The full configuration may be in project_integration and this project is project_production
 * the only difference is a hostname (for example) so this script copies all the files
 * in configurationBase/project_integration to outputDir and the shared.maven.filters in this
 * directory has the new server name.  Done.  
 * 
 */
def copyResource(java.io.File configurationBase, java.io.File outputDir) {
	// Not needed for this project so this is here just so build doesn't blow up
}