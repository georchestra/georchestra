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

# Get standard environment variables
PRGDIR=`pwd`/`dirname "$PRG"`

OUT=$PRGDIR/../git_gn
URL=ssh://jeichar.int.lsn/home/jeichar/repos/geoorchestra
BRANCH=geoorchestra

if [ ! -d  "$OUT" ]; then
    git clone git@github.com:camptocamp/geonetwork.git $OUT
fi
cd $OUT
git checkout -q $BRANCH
git diff 2.6.3 HEAD  > $PRGDIR/../patches/massivediff.diff