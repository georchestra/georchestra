#
# Variables
# 
$buildpath=split-path -parent $MyInvocation.MyCommand.Definition
$webapppath="$buildpath\..\src\main\webapp"
$releasepath="$webapppath\build"
$venv="$buildpath\venv"

#
# build
#
if ( Test-Path -Path $releasepath )
{
    rm -Recurse -Force $releasepath
}

mkdir -Force $releasepath > $null
mkdir -Force $releasepath\lang > $null
$start=$(get-location)

cd $buildpath;
if (!(Test-Path $venv))
{
	echo "creating virtual env and installing jstools..."
	$cmd = "cd $buildpath ; python go-jstools.py $venv --no-site-packages"
	Start-Process "$psHome\powershell.exe" -Verb Runas -ArgumentList "-command $cmd"
	echo "done."
}
echo "running jsbuild for main app..."
cmd /C $venv\Scripts\jsbuild -o "${releasepath}" main.cfg
echo "running jsbuild for edit app..."
cmd /C $venv\Scripts\jsbuild -o "${releasepath}" edit.cfg
echo "done."
cd $start
 
 
#
# OpenLayers resources
#
$openlayerspath="${webapppath}\lib\externals\openlayers"
$openlayersreleasepath="${releasepath}\openlayers"

echo "copying OpenLayers resources..."
mkdir ${openlayersreleasepath}
cp -Force -Recurse "${openlayerspath}\img" "${openlayersreleasepath}" 
cp -Force -Recurse "${openlayerspath}\theme" "${openlayersreleasepath}"
echo "done."

# Cleanup SVN stuff
Get-ChildItem "${releasepath}" -include .svn -Recurse -Force | Remove-Item -Recurse -Force

echo "built files and resources placed in src\main\webapp\build"

exit 0
