<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<logic:notPresent name="org.apache.struts.action.MESSAGE" scope="application">
  <span class="error">
    ERROR:  Application resources not loaded -- check servlet container
    logs for error messages.
  </span>
</logic:notPresent>

</span>
<div class="info-text">
<p>
<bean:message key="text.welcome1"/>
</p>

<!--p>
<bean:message key="text.welcome2"/>
</p-->

<!--p>
<bean:message key="text.welcome3"/>
</p-->

<p>
<bean:message key="text.welcome5"/>
</p>

<ul>
  <li>
    <a href="http://geoserver.org/display/GEOSDOC/Documentation">
      Documentation
    </a>
  </li>
  <li>
    <a href="http://geoserver.org/">
      Wiki
    </a>
  </li>
  <li>
    <a href="http://jira.codehaus.org/secure/BrowseProject.jspa?id=10311">
      Task Tracker
    </a>
   </li>
   <li>
    <a href="http://sigma.openplans.org/users/">
      User Map
    </a>
  </li>
</ul>

<p>
	<bean:message key="text.visitDemoPage"/>
</p>
</div>
	<a href="./ows?service=WCS&request=GetCapabilities">WCS Capabilities</a>
	<br>
	<a href="./ows?service=WFS&request=GetCapabilities">WFS Capabilities</a>
	<br>
	<a href="./ows?service=WMS&request=GetCapabilities">WMS Capabilities</a>
	<br><br>
	<a href="./srsHelp.do"><bean:message key="label.SRSList"/></a>
<br>
