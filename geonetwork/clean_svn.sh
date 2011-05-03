if [ -f "svn_gn/pom.xml" ] ; then cd svn_gn && mvn install -Dserver=$1; fi
