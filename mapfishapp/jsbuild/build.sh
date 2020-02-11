#
# Variables
# 
buildpath="$(cd $(dirname $0); pwd)"
webapppath="${buildpath}/../src/main/webapp"
releasepath="${webapppath}/build"
venv="${buildpath}/env"

#
# Command path definitions
#
python="/usr/bin/env python2"
virtualenv="/usr/bin/env virtualenv --python=${python}"
mkdir="/usr/bin/env mkdir"
rm="/usr/bin/env rm"
sh="/usr/bin/env sh"
cp="/usr/bin/env cp"

#
# build
#
if [ -d ${releasepath} ]; then
    ${rm} -rf ${releasepath}
fi

${mkdir} -p ${releasepath} ${releasepath}/lang

(cd ${buildpath};
 ${python} -c "import pip"
 if [ $? != 0 ]; then
    curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py
    ${python} get-pip.py
    rm get-pip.py
 fi
 ${python} -c "import virtualenv"
 if [ $? != 0 ]; then
   ${python} -m pip install virtualenv
 fi
 ${venv}/bin/jsbuild -h > /dev/null
 if  [ ! -d ${venv} ] || [ $? -eq 0 ]; then
     echo "creating virtual env and installing jstools..."
     rm -rf ${venv}
     ${python} -m virtualenv ${venv}
     ${venv}/bin/python -m pip install jstools==0.6 -i https://pypi.python.org/simple/
     echo "done."
 fi;

 echo "running jsbuild for main app..."
 ${venv}/bin/jsbuild -o "${releasepath}" main.cfg
 echo "done."
)

if [ ! -e ${releasepath}/mapfishapp.js ]; then
    echo "\033[01;31m[NOK]\033[00m jsbuild failure"
    exit 1
fi;
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
