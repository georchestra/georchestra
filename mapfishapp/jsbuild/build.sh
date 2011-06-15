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
mapfishpath="${webapppath}/lib/externals/mapfish"
if [ -d ${releasepath} ]; then
    ${rm} -rf ${releasepath}
fi

mapfishreleasepath="${releasepath}/mapfish"
${mkdir} -p ${mapfishreleasepath}

(cd ${buildpath};
 if [ ! -d ${venv} ]; then
     echo "creating virtual env and installing jstools..."
     ${python} go-jstools.py ${venv} --no-site-packages > /dev/null
     echo "done."
 fi;
 echo "running jsbuild for main app..."
 ${venv}/bin/jsbuild -o "${mapfishreleasepath}" main.cfg
 echo "running jsbuild for edit app..."
 ${venv}/bin/jsbuild -o "${mapfishreleasepath}" edit.cfg
 echo "done.")

#
# MapFish resources
#
echo "copying MapFish resources..."
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
