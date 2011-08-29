How to quickly run mapfishapp:
------------------------------

Note: this is for dev/evaluation purposes only. More instructions to come
when the application profile system will be commited.

You need maven 3 or greater. Make sure that no server is running on localhost:8080.

Just type :
 mvn jetty:run

...from the application root directory (the one that contains pom.xml)


At the end of the process, open http://localhost:8080/mapfishapp/?debug=true or
http://localhost:8080/mapfishapp/ in your browser

?debug=true appended at the end of the URL makes you use uncompressed javascript,
which means you can live-test your modification and observe the wargnins and errors
in the browser javascript console or in the featured firebug debugger.

For authentication, you can fool the authentication mechanism by installing the firefox extension
"modify headers" (https://addons.mozilla.org/en-US/firefox/addon/modify-headers/)

In the extension config, activate those rules :
 add / sec-roles / ROLE_SV_ADMIN (or any others at your convenience, separated by commas)
 add / sec-username / yourname
 add / sec-email / youremail

And in options tab, check "Always On".
