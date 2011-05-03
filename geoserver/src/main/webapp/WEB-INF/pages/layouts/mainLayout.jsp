<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/tags/struts-tiles" prefix="tiles" %>
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>
<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core' %>

<tiles:importAttribute scope="request"/>

<bean:define id="key"><tiles:getAsString name='key'/></bean:define>
<bean:define id="keyLabel"><tiles:getAsString name='key'/>.label</bean:define>
<bean:define id="keyTitle"><tiles:getAsString name='key'/>.title</bean:define>
<bean:define id="keyShort"><tiles:getAsString name='key'/>.short</bean:define>
<bean:define id="keyWords"><tiles:getAsString name='key'/>.words</bean:define>
<bean:define id="layer"><tiles:getAsString name='layer'/></bean:define>

<%@page import="org.vfny.geoserver.global.GeoServer"%>
<%@page import="org.vfny.geoserver.util.Requests"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html:html locale="true" xhtml="true">
  <head>
    <title>
      <bean:message key="geoserver.logo"/>
      <bean:message key="<%= keyTitle %>"/>
    </title>
    <meta content="text/html; charset=iso-8859-1" http-equiv="content-type"/>
    <meta content="text/css" http-equiv="content-style-type"/>  
    <meta name="description"
          content="<bean:message key="<%= keyShort %>"/>">
    <meta name="keywords"
          content="(GeoServer) (GIS) (Geographic Information Systems) <bean:message key="<%= keyWords %>"/>"/>
    <meta name="author" content="Dave Blasby, Chris Holmes, Brent Owens, Justin Deoliveira, Jody Garnett, Richard Gould, David Zwiers"/>
    
    <tiles:insert attribute="javascript"/>
  	
  	<script language="JavaScript">
		<!--
		// This is used for URL parsing to check for any spaces that will cause invalid XML
		// Currently it is used in DataConfigDataStoresEditor.jsp
		function checkspaces(form)
		{
			for(var i=0; i<form.elements.length; i++)
			{
				if(form.elements[i].value.match("file:"))
				{
					var badchar = " ";	// look for the space character
					if (form.elements[i].value.indexOf(badchar) > -1) 
					{
						alert("Spaces are not allowed in the filename or path.");
						form.elements[i].focus();
						form.elements[i].select();
						return false;
					}
				}
			}// end for
			return true;
		}
	function onClean() {
	   var iFrameBody = document.getElementById("demoResponse").contentWindow.document.body;
	   var url = document.getElementById("url").value;
	   var body = document.getElementById("body").value;
	   
	   // we need to escape & and other simbols that the browser parsed for us, and make
	   // them &amp; again...
	   var div = document.createElement('div');
	   var text = document.createTextNode(body);
	   div.appendChild(text);
	   body = div.innerHTML;
	   
	   var username = document.getElementById("username").value;
	   var password = document.getElementById("password").value;
	   iFrameBody.innerHTML = "<form action='http://<%=request.getServerName()%>:<%=request.getServerPort()%><%=request.getContextPath()%>/TestWfsPost' method='POST'>\n" + 
	                          "<input type='hidden' name='url' value='" + url + "'/>\n" +
	                          "<textarea style='visibility:hidden' name='body' />" + body + "</textarea>\n" + 
	                          "<input type='hidden' name='username' value='" + username + "'/>\n" + 
	                          "<input type='hidden' name='password' value='" + password + "'/>\n" +
	                          "<input type='hidden' value='submit'/>\n" +
	                          "</form>";
	   var form = iFrameBody.firstChild;
	   form.submit();
	}
	function loadResults() {
	   document.getElementById("demoResponse").src = "about:blank";
	   setTimeout('onClean()', 10);
	};
	function resize_iframe()
	{
	    if(!document.getElementById("demoResponse"))
	      return;
	
		var height=window.innerWidth;//Firefox
		if (document.body.clientHeight)
		{
			height=document.body.clientHeight;//IE
		}
		//resize the iframe according to the size of the
		//window (all these should be on the same line)
		document.getElementById("demoResponse").style.height=parseInt(height-
	 	findPos(document.getElementById("demoResponse"))[1] - 8)+"px";
	}
	
	function findPos(obj) {
		var curleft = curtop = 0;
		if (obj.offsetParent) {
			curleft = obj.offsetLeft
			curtop = obj.offsetTop
			while (obj = obj.offsetParent) {
				curleft += obj.offsetLeft
				curtop += obj.offsetTop
			}
		}
		return [curleft,curtop];
	}

	// this will resize the iframe every
	// time you change the size of the window.
	window.onresize=resize_iframe; 

		-->
	</script>  	
    <style type="text/css">
      <!-- @import url("<html:rewrite forward='style'/>"); -->
    </style>
  
    <link type="image/gif" href="<html:rewrite forward='icon'/>" rel="icon"/>
    <link href="<html:rewrite forward='favicon'/>" rel="SHORTCUT ICON"/>
  </head>
  <body>
  
<table class="page" height="100%">
  <tbody>
	<tr class="header" height="1%">
        <td class="gutter">
          <span class="project">
            <a href="<bean:message key="link.geoserver"/>">
              <bean:message key="geoserver.logo"/>
            </a>
          </span>
		</td>
        <td style="width: 1em">
        </td>
		<td style="vertical-align: bottom; white-space: nowrap;">
          <div class="site-head">
	         <div class="selfclear">
<span class="site"><a href="<%=request.getContextPath()%>/welcome.do"><logic:notEmpty name="GeoServer" property="title">
              <bean:write name="GeoServer" property="title"/>
</logic:notEmpty>
<logic:empty name="GeoServer" property="title">
              <bean:message key="message.noTitle"/>
</logic:empty></a></span>
			
			 <span class="contact">
			   <a href="<bean:message key="label.credits.url"/>"><bean:message key="label.credits"/></a>
<logic:notEmpty name="GeoServer" property="contactParty">		
              <bean:message key="label.contact"/>: 	
              <html:link forward="contact">
                <bean:write name="GeoServer" property="contactParty"/>
              </html:link>         
</logic:notEmpty>
				</span>     
      </div>       
      </div>          
        </td>
	</tr>
	
    <tr>
      <td class="sidebar">
        <tiles:insert attribute="status"/>	
        <tiles:insert attribute="configActions"/>
        <tiles:insert attribute="actionator"/>
        <tiles:insert attribute="messages"/>
      </td>
      <td style="width: 1em">
      </td>      
      <td style="vertical-align: top;">
            
        <table class="main">
          <tbody>
            <tr class="bar">
              <td class="locator">
                <tiles:insert attribute="locator"/>
              </td>
              <td class="loginStatus">
                <span class="loginStatus">                  
<%if(Requests.isLoggedIn(request)) {%>
                    <a href="<%=request.getContextPath()%>/j_acegi_logout"><bean:message key="label.logout"/></a>
<%} else {%>                  
                    <html:link forward="login">
                      <bean:message key="label.login"/>
                    </html:link>
<%}%>                  
                </span>
              </td>
            </tr>
          	<tr>
              <td class="<tiles:getAsString name='layer'/>"
                  rowspan="1" colspan="2">
                <table class="title">
                  <tbody>
                    <tr>
                      <td class="menu">
                        <tiles:insert attribute="menuator"/>
                      </td>
                      <td class="title">
                        <h1 class="title">
                          <bean:message key="<%= keyTitle %>"/>
                        </h1>
                        <p class="abstract">
                          <bean:message key="<%= keyShort %>"/>
                        </p>
                      </td>
                      <td class="icon"></td>
                    </tr>    
                  </tbody>
                </table>
                <tiles:insert attribute="body"/>
              </td>
	        </tr>
          </tbody>
	    </table>
      </td>
	</tr>
  </tbody>
</table>

</body>
</html:html>
