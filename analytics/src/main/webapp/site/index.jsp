<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html"%>
<%@ page pageEncoding="UTF-8"%>
<%@ page isELIgnored="false" %>
<%
Boolean anonymous = true;
Boolean admin = false;
Boolean editor = false;
String sec_roles = request.getHeader("sec-roles");
if(sec_roles != null) {
    String[] roles = sec_roles.split(",");
    for (int i = 0; i < roles.length; i++) {
        // ROLE_ANONYMOUS is added by the security proxy:
        if (roles[i].equals("ROLE_ANONYMOUS")) {
            break;
        }
        if (roles[i].equals("ROLE_SV_ADMIN")) {
            admin = true;
        }
        if (roles[i].equals("ROLE_SV_EDITOR") || roles[i].equals("ROLE_SV_REVIEWER") || roles[i].equals("ROLE_SV_ADMIN")) {
            editor = true;
            anonymous = false;
        }
        if (roles[i].equals("ROLE_SV_USER")) {
            anonymous = false;
        }
    }
}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>


<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>geOrchestra Analytics</title>
    <link rel="stylesheet" type="text/css" href="resources/site/js/lib/external/ext/resources/css/ext-all-gray.css" />
    <link rel="stylesheet" type="text/css" href="resources/site/css/app.css" />
    <style type="text/css">
        body {
            background: #ffffff;
        }
        #loading {
            position: absolute;
            left: 45%;
            top: 40%;
            padding: 2px;
            z-index: 20001;
            height: auto;
        }
        .admin_only {
        	padding-left: 40px;
        	color:red;
        	font-family : 'Yanone Kaffeesatz', arial,verdana,helvetica;
        	font-size: 13px;
        	
        }
        #loading-msg {
            font: normal 12px arial,tahoma,sans-serif;
        }
        #go_head ul {
            float: left;
            list-style: none;
            margin: 20px 0 0 10px;
            padding: 0;
            font-size: 18px;
            display: inline;
        }
        #go_head li {
            margin: 0;
            padding: 0;
            display: inline-block;
        }
        #go_head .logged {
            margin        : 20px 15px 0 0;
            border        : 1px dotted #ddd;
            border-radius : 0.3em;
            padding       : 0 0.6em;
            width         : auto;
            float         : right;
            height        : 52px;
            line-height   : 52px;
        }
 
    </style>

    <title lang="fr" dir="ltr">Analytics - geOrchestra</title>
</head>
<body>

    <%@ include file="header.jsp" %>

    
    
    <c:choose>
    	<c:when test='<%= admin == true %>'>
			<div id="waiter" style="display:none;">
		        <span>Chargement ...</span>
		    </div>
		    <div id="loading">
		        <img src="resources/site/images/loading.gif" alt="chargement" width="32" height="32" style="margin-right:8px;float:left;vertical-align:top;"/>
		        <span id="loading-msg">Chargement...</span>
		    </div>
		    
			<script type="text/javascript" src="resources/site/js/lib/external/ext/ext.js"></script>
   			<script type="text/javascript" src="resources/site/js/lib/external/ext/locale/ext-lang-fr.js"></script>
		    <script type="text/javascript" src="resources/site/js/app/Application.js"></script>

		    <script type="text/javascript">
			    Ext.onReady(function() {
			        Ext.get("loading").remove();
			    });
		    </script>
    	</c:when>
    	<c:otherwise>
    		<div class="admin_only"><b>Module uniquement accessible aux administrateurs</b></div>
    	</c:otherwise>
	</c:choose>
    
    
    <noscript><p>Cette application nécessite le support de JavaScript par votre navigateur. Merci de l'activer.</p></noscript>
</body>
</html>
