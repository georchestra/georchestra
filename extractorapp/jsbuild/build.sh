#
# Variables
# 
buildpath="$(cd $(dirname $0); pwd)"
webapppath="${buildpath}/../src/main/webapp"
releasepath="${webapppath}/build"
venv="${buildpath}/venv"

#
# Command path definitions
#
python="/usr/bin/python"
mkdir="/bin/mkdir"
rm="/bin/rm"
sh="/bin/sh"
cp="/bin/cp"

${rm} -rf "${releasepath}"

#
# MapFish.js build
#
if [ -d ${releasepath} ]; then
    ${rm} -rf ${releasepath}
fi
${mkdir} -p ${releasepath}

(cd ${buildpath};
 if [ ! -d ${venv} ]; then
     echo "creating virtual env and installing jstools..."
     ${python} go-jstools.py ${venv} > /dev/null
     echo "done."
 fi;
 echo "running jsbuild for main app..."
 ${venv}/bin/jsbuild -o "${releasepath}" main.cfg
 echo "done.")

#
# MapFish resources
#
mapfishpath="${webapppath}/lib/externals/mapfish"
mapfishreleasepath="${releasepath}/mapfish"
echo "copying MapFish resources..."
${mkdir} -p ${mapfishreleasepath}
${cp} -r "${mapfishpath}/img" "${mapfishreleasepath}"
echo "done."

#
# OpenLayers resources
#
openlayerspath="${webapppath}/lib/externals/openlayers"
openlayersreleasepath="${releasepath}/openlayers"

echo "copying OpenLayers resources..."
${mkdir} ${openlayersreleasepath}
${cp} -r "${openlayerspath}/img" "${openlayersreleasepath}"
${cp} -r "${openlayerspath}/theme" "${openlayersreleasepath}"
echo "done."

# Cleanup SVN stuff
${rm} -rf `find "${releasepath}" -name .svn -type d`

echo "built files and resources placed in src/main/webapp/build"

exit 0
