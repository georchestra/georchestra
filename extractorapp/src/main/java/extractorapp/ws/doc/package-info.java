/**
* Provides RESTful services to store and load files. Besides that many treatments can be done as 
* validation, formatting, or interpretation. <br /> <br />
* New services can be added. In order to do that it must inherits {@link extractorapp.ws.doc.A_DocService} and should be named as
* {DOCTYPE}DocService. {@link extractorapp.ws.doc.A_DocService} contains common methods for all doc services. Few methods can be overridden from it
* to adapt some specific behaviors. Then the service should be registered in {@link extractorapp.ws.doc.DocController} to provides RESTful entry points.
* <br /> <br />
* @author yoann.buch@gmail.com
*/
package extractorapp.ws.doc;
