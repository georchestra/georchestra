# About

This directory contains a maven project to build a WAR with the frontend part
for the datafeeder.

Note: the docker image makes use of an Nginx, which makes more sense to serve
static resources as a web ui, but since this leads to make several assumptions
on the targeted infrastructurei (Do we have a webserver ? Which port is it
supposed to listen on ? Where is the document root ? Which webserver to begin
with ? Which sample configuration to provide ?), is has been preferred to
provide this UI as another webapp, as we know that at least one servlet
container will be available.

# Compilation

```
mvn clean package
```

This will result in a `import.war` in the `target/` subdirectory.
