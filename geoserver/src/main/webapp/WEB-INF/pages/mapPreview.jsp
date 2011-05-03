<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html:html locale="true" xhtml="true">
  <head>
    <title>
      <bean:message key="geoserver.logo"/>
      Geoserver
    </title>

    <meta content="text/html; charset=iso-8859-1" http-equiv="content-type"/>
    <meta content="text/css" http-equiv="content-style-type"/>  
    <meta name="keywords"
          content="(GeoServer) (GIS) (Geographic Information Systems)"/>
    <meta name="author" content="Brent Owens"/>
  
    <style type="text/css">
      <!-- @import url("<html:rewrite forward='style'/>"); -->
    </style>
  
    <link type="image/gif" href="<html:rewrite forward='icon'/>" rel="icon"/>
    <link href="<html:rewrite forward='favicon'/>" rel="SHORTCUT ICON"/>
		 <!-- -   
		          This JSP expect to have a:

		          <form-bean 
			    name="mapPreviewForm" 
			    type="org.apache.struts.action.DynaActionForm">
			    <form-property 
			        name="DSNameList"
			        type="java.lang.String[]" 
			    />
			    <form-property 
			        name="FTNameList" 
			        type="java.lang.String[]" 
			    />
			    <form-property 
			        name="BBoxList" 
			        type="java.lang.String[]" 
			    />
			    </form-bean>

			   given to it.  
			   The DSNameList list is a list of strings - these are the 
			   names of the data stores for the FeatureType.
			   The FTNameList is the list of FeatureTypes represented
			   as strings.
			   The BBoxList contains the bounding box coordinates of 
			   the feature type represented as a string.
		  - -->

		<!-- ALL THIS STUFF TAKEN FROM MAINLAYOUT.JSP -->
		 <!-- ======================================== -->
  </head>
  <body>
 <table class="page">
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
	</table>
 <!-- ===================================================================== -->

<h1 align="center"> <bean:message key="mapPreview.title"/> </h1>

 <!-- ===================================================================== -->
<!-- DISPLAY THE LIST OF FEATURE TYPES AND THEIR INFORMATION             -->
 <!-- ===================================================================== -->


<table border=1 cellpadding=4 align="center">

  <tr><th><B><U>Layer</U> (NameSpace:FeatureType)</B></th><!-- <th><B><U>DataStore</U></B> --><!--bean:message key="mapPreview.tableTitle"/--></th><th><B><U>Preview Map</U></B></th></tr>
  
  
<!-- This iterator take idx from 0 to however many items there are in the list.
     I use the index to grab the data from the 3 input lists (see above).
     The it_value is ignored.
  -->
 <logic:iterate id="it_value" indexId="idx" name="mapPreviewForm" property="DSNameList">
 
	<tr >
	     <td>
				<b>
				<a href="wms?bbox=<bean:write property='<%= "BBoxList[" + idx + "]" %>' name="mapPreviewForm"/>&styles=&Format=application/openlayers&request=GetMap&version=1.1.1&layers=<bean:write property='<%= "EscapedFTNamespaceList[" + idx + "]" %>' name="mapPreviewForm"/>&width=<bean:write property='<%= "WidthList[" + idx + "]" %>' name="mapPreviewForm"/>&height=<bean:write property='<%= "HeightList[" + idx + "]" %>' name="mapPreviewForm"/>&srs=<bean:write property='<%= "SRSList[" + idx + "]" %>' name="mapPreviewForm"/>" target="_blank"><bean:write property='<%= "FTNamespaceList[" + idx + "]" %>' name="mapPreviewForm"/></a>
				</b>
	     </td>
	     <!-- 
	     <td class="greyedout2">
				 <bean:write property='<%= "DSNameList[" + idx + "]" %>' name="mapPreviewForm"/>
	     </td>
	      -->
		 <td>
			<!-- add link to FTNameList.html -->
			<center><b><font size="-1">
			<a href="<bean:write property="BaseUrl" name="mapPreviewForm"/>wms?bbox=<bean:write property='<%= "BBoxList[" + idx + "]" %>' name="mapPreviewForm"/>&styles=&Format=application/openlayers&request=GetMap&version=1.1.1&layers=<bean:write property='<%= "EscapedFTNamespaceList[" + idx + "]" %>' name="mapPreviewForm"/>&width=<bean:write property='<%= "WidthList[" + idx + "]" %>' name="mapPreviewForm"/>&height=<bean:write property='<%= "HeightList[" + idx + "]" %>' name="mapPreviewForm"/>&srs=<bean:write property='<%= "SRSList[" + idx + "]" %>' name="mapPreviewForm"/>" target="_blank">OpenLayers</a>&nbsp;&nbsp;
			<a href="<bean:write property="BaseUrl" name="mapPreviewForm"/>wms/kml?layers=<bean:write property='<%= "EscapedFTNamespaceList[" + idx + "]" %>' name="mapPreviewForm"/>" target="_blank">KML</a>&nbsp;&nbsp;
			<logic:greaterEqual property='<%= "CoverageStatus[" + idx + "]" %>' name="mapPreviewForm" value="1">
				<a href="<bean:write property="BaseUrl" name="mapPreviewForm"/>wms?bbox=<bean:write property='<%= "BBoxList[" + idx + "]" %>' name="mapPreviewForm"/>&styles=&Format=application/rss%2Bxml&request=GetMap&version=1.1.1&layers=<bean:write property='<%= "EscapedFTNamespaceList[" + idx + "]" %>' name="mapPreviewForm"/>&width=<bean:write property='<%= "WidthList[" + idx + "]" %>' name="mapPreviewForm"/>&height=<bean:write property='<%= "HeightList[" + idx + "]" %>' name="mapPreviewForm"/>&srs=<bean:write property='<%= "SRSList[" + idx + "]" %>' name="mapPreviewForm"/>" target="_blank">GeoRSS</a>&nbsp;&nbsp;
			</logic:greaterEqual>
			<logic:equal property='<%= "CoverageStatus[" + idx + "]" %>' name="mapPreviewForm" value="0">
			    <font color="gray">GeoRSS&nbsp;&nbsp;</font>
			</logic:equal>
			<a href="<bean:write property="BaseUrl" name="mapPreviewForm"/>wms?bbox=<bean:write property='<%= "BBoxList[" + idx + "]" %>' name="mapPreviewForm"/>&styles=&Format=application/pdf&request=GetMap&version=1.1.1&layers=<bean:write property='<%= "EscapedFTNamespaceList[" + idx + "]" %>' name="mapPreviewForm"/>&width=<bean:write property='<%= "WidthList[" + idx + "]" %>' name="mapPreviewForm"/>&height=<bean:write property='<%= "HeightList[" + idx + "]" %>' name="mapPreviewForm"/>&srs=<bean:write property='<%= "SRSList[" + idx + "]" %>' name="mapPreviewForm"/>" target="_blank">PDF</a>&nbsp;&nbsp;
			<logic:equal property='<%= "CoverageStatus[" + idx + "]" %>' name="mapPreviewForm" value="2">
				<a href="<bean:write property="BaseUrl" name="mapPreviewForm"/>wms?bbox=<bean:write property='<%= "BBoxList[" + idx + "]" %>' name="mapPreviewForm"/>&styles=&Format=image/svg%2Bxml&request=GetMap&version=1.1.1&layers=<bean:write property='<%= "EscapedFTNamespaceList[" + idx + "]" %>' name="mapPreviewForm"/>&width=<bean:write property='<%= "WidthList[" + idx + "]" %>' name="mapPreviewForm"/>&height=<bean:write property='<%= "HeightList[" + idx + "]" %>' name="mapPreviewForm"/>&srs=<bean:write property='<%= "SRSList[" + idx + "]" %>' name="mapPreviewForm"/>" target="_blank">SVG</a>
			</logic:equal>
			<logic:notEqual property='<%= "CoverageStatus[" + idx + "]" %>' name="mapPreviewForm" value="2">
			    <font color="gray">SVG</font>
			</logic:notEqual>
			
			</font>
			</b></center>
		 </td>
	</tr>
</logic:iterate>
</table>

</body>
</html:html>
