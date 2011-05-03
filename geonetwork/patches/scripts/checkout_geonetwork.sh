set -x

if [ -z "$1" ]; then
    echo "ERROR: target directory must be specified as first parameter"
    exit -1
fi

if [ -z "$2" ]; then
    echo "ERROR: patch directory must be specified as second parameter"
    exit -1
fi

BASE=`pwd`
CONFIG=$BASE/src/$3
PATCH_DIR=$2
OUT=$1
URL=https://geonetwork.svn.sourceforge.net/svnroot/geonetwork/tags/2.6.3

if [ ! -d "$OUT" ]; then
	svn co $URL $OUT
fi

UP_TO_DATE=0
if [ -f "$patches_applied" ]; then
    UP_TO_DATE=1
    for i in `ls $PATCH_DIR/*.diff` ; do 
        if [ "$i" -nt "$patches_applied" ]; then
            UP_TO_DATE=0
        fi
    done
fi

if [ $UP_TO_DATE -eq 0 ]; then
    cd $OUT
    svn revert -R .
    svn st | awk '{print $2}' | xargs rm -rf
    svn up


    for i in `ls $PATCH_DIR/*.diff` ; do 
        patch -p1 --verbose < $i
        if [ ! $? -eq 0 ]; then
            exit "Unable to apply patch $i"
        fi
    done

    touch $OUT/patches_applied
fi

cp -R $CONFIG $OUT/src 

