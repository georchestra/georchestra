#!/bin/sh
isOk() {
  if [ ! $1 -eq 0 ]; then
    failures=`expr $failures + 1`
    if [ "$FORCE_DEPLOY" != "true" ]; then
      exit $1;
    fi
  fi
}


cd svn_gn
../../mvn install -Dserver=$1
isOk $?
mkdir ../target
cp web/target/*.war ../target
