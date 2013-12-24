PRG="$0"

while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
        else
        PRG=`dirname "$PRG"`/"$link"
        fi
done

PRGDIR=`dirname "$PRG"`
PWD=`pwd`

isOk () {
  if [ ! $2 -eq 0 ]; then
      echo "[FAILURE] [deploy] Failed to execute '$1' correctly"
      echo "[FAILURE] [deploy] Failed to execute '$1' correctly" > /tmp/gc_deploy_failure
      exit $2;
 fi
}
function USAGE {
    echo "deploy.to.*.sh [-P <project_to_deploy>] [-m <deploy_mode>] [-D <property=value>] [-X] target"
    echo "  -P <project_to_deploy> "
    echo "      the modules (like mapfishapp) to deploy. There can be multiple -p parameters"
    echo "      'full' indicates all should be deployed"
    echo "  -m <deploy_mode> either upgrade or full.  All indicates setup databases, geoserver, etc..."
    echo "  -D <property=value> the java system property to set"
    echo "  -X maven debug mode"
    echo "  -o maven offline mode (do not check remote dependencies)"
}

MODE="upgrade"
PROFILES=""
SYSTEM_PROPS=""

while getopts ":D:P:m:Xoh?" opt; do
    case $opt in
        P ) PROFILES="$PROFILES -P$OPTARG" ;;
        m ) MODE=$OPTARG ;;
        D ) SYSTEM_PROPS="$SYSTEM_PROPS -D$OPTARG" ;;
        X ) SYSTEM_PROPS="$SYSTEM_PROPS -X" ;;
        o ) SYSTEM_PROPS="$SYSTEM_PROPS -o" ;;
        \?) USAGE; exit 1 ;;
    esac
done

if [ -z $PROFILES ] ; then
    PROFILES="-Pupgrade"
fi

if [[ $PROFILES =~ .*Pall.* ]] || [ $PROFILES == "-Pupgrade" ] ; then
    # do nothing.  we want to build all projects
    echo "building and deploying all projects"
else
    PROFILES="-P-all $PROFILES"
fi

shift $(($OPTIND -1))
TARGET=$1

if [ -z $TARGET ] ; then
    echo "a target parameter is required"
    USAGE
    exit 1;
fi

PROFILES="$PROFILES -Dserver=your_profile -Dsub.target=$TARGET"
#geoserver extensions
PROFILES="$PROFILES -Pgdal -Pjp2k -Pmonitor -Pinspire -Pwps -Pcss -Ppyramid"

echo "[deploy] Deploy mode is $MODE"
echo "[deploy] Deploy profiles are $PROFILES"
echo "[deploy] Deploy target is $TARGET"

DEPLOY_CACHE=/var/cache/deploy

CHECKOUT_DIR=$DEPLOY_CACHE/checkout
if [ ! -d $CHECKOUT_DIR ] ; then
    echo "[deploy] cloning georchestra"
    mkdir -p $CHECKOUT_DIR
    sudo -u deploy git clone -b 13.12 --recursive git://github.com/georchestra/georchestra.git $CHECKOUT_DIR
fi

if [ ! -d $CHECKOUT_DIR/config/configurations/your_profile ] ; then
    echo "[deploy] cloning config"
    sudo -u deploy git clone -b 13.12 --recursive https://github.com/your_org/your_profile.git $CHECKOUT_DIR/config/configurations/your_profile
fi

echo "[deploy] updating configuration"
cd $CHECKOUT_DIR/config/configurations/your_profile
sudo -u deploy git reset --hard 
sudo -u deploy git clean -xfd
sudo -u deploy git fetch origin
sudo -u deploy git checkout 13.12
sudo -u deploy git merge origin/13.12

echo "[deploy] updating georchestra"
cd $CHECKOUT_DIR
sudo -u deploy git clean -xf
sudo -u deploy git reset --hard
sudo -u deploy git fetch origin
sudo -u deploy git checkout 13.12
sudo -u deploy git merge origin/13.12

echo "[deploy] cleaning geonetwork"
cd $CHECKOUT_DIR/geonetwork
sudo -u deploy git clean -xdf
sudo -u deploy git reset --hard

echo "[deploy] updating submodules"
cd $CHECKOUT_DIR
sudo -u deploy git submodule sync
sudo -u deploy git submodule update --init


ARCHIVE=$DEPLOY_CACHE/archive
#need to execute as deploy

set -x
MVN="/var/cache/deploy/checkout/build-tools/maven/bin/mvn"
#sudo -u deploy $MVN clean $SYSTEM_PROPS
#isOk "clean georchestra" $?
sudo -u deploy $MVN install $PROFILES $SYSTEM_PROPS
isOk "build georchestra" $?
cd server-deploy
sudo -u deploy $MVN $PROFILES -Dnon-interactive=true $SYSTEM_PROPS
isOk "deploy" $?
