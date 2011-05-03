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
PRGDIR=`dirname "$PRG"`


OUT=$PRGDIR/../git_gn
URL=ssh://jeichar.int.lsn/home/jeichar/repos/geoorchestra
BRANCH=geoorchestra

if [ ! -d  "$OUT" ]; then
    mkdir -p $OUT
    echo "checking out $URL"
    git clone $URL $OUT
    cd $OUT
    git checkout -q $BRANCH
else
    cd $OUT
    git pull
fi
