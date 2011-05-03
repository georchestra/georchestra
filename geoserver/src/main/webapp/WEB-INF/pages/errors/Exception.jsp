<%@ page session="false" isErrorPage="true" %>


<%@page import="java.io.ByteArrayOutputStream"%>
<%@page import="java.io.PrintStream"%><html>
<head><title>GeoServer - Exception</title>
  <meta content="text/css" http-equiv="content-style-type">
  <style type="text/css">
    <!-- @import url("/geoserver/style.css"); -->
  </style>
  <link type="image/gif" href="gs.gif" rel="icon"><!-- mozilla --> 
  <link href="gs.ico" rel="SHORTCUT ICON"><!-- ie -->
</head>
<body>
<h1>GeoServer - Exception</h1>

The following exception was thrown:
<br>
<ul>
<%Throwable t = (Throwable) request.getAttribute("javax.servlet.error.exception");
  while(t != null) {
      out.write("<li>" + t.getClass().toString() + ": "); out.write(t.getMessage()); out.write("</li>");
      t = t.getCause();
  }
%>
</ul>

<p>Details:
<pre>
<%Exception causeException = (Exception) request.getAttribute("javax.servlet.error.exception");
  ByteArrayOutputStream bos = new ByteArrayOutputStream();
  PrintStream ps = new PrintStream(bos);
  causeException.printStackTrace(ps);
  ps.flush();
  out.write(bos.toString());
%>
</pre>
</body>
</html>