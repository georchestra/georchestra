Build
mvn install -Dmaven.test.skip


Execute
java -jar target/postgistojndi-0.0.1-SNAPSHOT.jar src/test/resources/workspaces  target/test/  jdbc/postgres/geoserverdb 
