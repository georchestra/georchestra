#!/bin/bash
RELEASE=${1}

# Automatically compute next release by incrementing patch version
IFS='.' read -r -a version_parts <<< "$RELEASE"
NEXT_RELEASE="${version_parts[0]}.${version_parts[1]}.$((version_parts[2] + 1))"

GEOR_BRANCH=${2:-26.0.x}
echo 'STARTING MINOR 26 RELEASE' $RELEASE
echo 'COMPUTED NEXT RELEASE' $NEXT_RELEASE

git checkout $GEOR_BRANCH
git pull --recurse-submodules


declare -a files=("commons/pom.xml" "console/pom.xml" 
    "ldap-account-management/pom.xml" "pom.xml" "security-proxy-spring-integration/pom.xml"  "testcontainers/pom.xml")

IFS='.' read -r -a major_version <<< $RELEASE
echo 'UPDATING MAJOR VERSION' ${major_version[0]}
for i in "${files[@]}"
do
    echo 'UPDATING' $i 
    sed -i -E  "s/(<version>)${major_version[0]}.*(<\/version>)/\1$RELEASE\2/" $i
done

echo 'COMMITTING AND TAGGING GEORCHESTRA' $RELEASE
git commit -am "$RELEASE release"
git tag $RELEASE

echo 'SET BACK TO SNAPSHOT'


for i in "${files[@]}"
do
    echo 'UPDATING' $i 
    sed -i -E  "s/(<version>)$RELEASE(<\/version>)/\1$NEXT_RELEASE-SNAPSHOT\2/" $i
done

echo 'COMMITTING GEORCHESTRA' $NEXT_RELEASE
git commit -am "back to snapshot"

echo '---------------------------------------------'
echo '[MANUAL ACTION] PUSHING TO REMOTE (if ok)'