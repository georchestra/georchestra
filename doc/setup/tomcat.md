# Tomcat


based on INSTALL.md 
http://blog.tartachuc.org/2012/01/18/plusieurs-instances-de-tomcat-sur-ubuntu/
http://geo.viennagglo.fr/doc/index.html, but with 3 tomcat instances by default.

connector config: requires proxyhost, proxyport

if proxies, add to setenv.sh: ```-Dhttp.proxyHost=... -Dhttp.proxyPort=.... -Dhttps.proxyHost=... -Dhttps.proxyPort=....```



