<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<table border=0 width=100%>
	
	<html:form action="/config/wms/renderingSubmit" focus="svgRenderer">
	
	<tr><td align="right">
		<span class="help" title="<bean:message key="help.wms.svgRenderer"/>">
			<bean:message key="label.wms.svgRenderer"/>:
		</span>
		</td>
		<td colspan=2>
			<html:radio name="wmsRenderingForm" property="svgRenderer" value="Simple"><bean:message key="label.wms.svgSimple"/></html:radio>
		</td></tr>
	<tr><td align="right">
		</td>
		<td colspan=2>
			<html:radio name="wmsRenderingForm" property="svgRenderer" value="Batik"><bean:message key="label.wms.svgBatik"/></html:radio> 	
		</td></tr>
	<tr><td align="right">
		</td>
		<td colspan=2>
			<html:checkbox name="wmsRenderingForm" property="svgAntiAlias"><bean:message key="label.wms.svgAntiAlias"/></html:checkbox> 	
		</td></tr>
	<tr><td align="right">
		<span class="help" title="<bean:message key="help.wms.allowInterpolation"/>">
			<bean:message key="label.wms.allowInterpolation"/>:
		</span>
		</td>
		<td colspan=2>
			<html:radio name="wmsRenderingForm" property="allowInterpolation" value="Nearest"><bean:message key="label.wms.allowInterpolation.nearest"/></html:radio>
		</td></tr>
	<tr><td align="right">
		</td>
		<td colspan=2>
			<html:radio name="wmsRenderingForm" property="allowInterpolation" value="Bilinear"><bean:message key="label.wms.allowInterpolation.bilinear"/></html:radio>
		</td></tr>
	<tr><td align="right">
		</td>
		<td colspan=2>
			<html:radio name="wmsRenderingForm" property="allowInterpolation" value="Bicubic"><bean:message key="label.wms.allowInterpolation.bicubic"/></html:radio>
		</td></tr>
    <!-- Watermarking Options - START -->
	<tr><td align="right">
		<span class="help" title="<bean:message key="help.wms.allowWatermarking"/>">
			<bean:message key="label.wms.allowWatermarking"/>:
		</span>
		</td>
		<td colspan=2>
			<html:checkbox name="wmsRenderingForm" property="globalWatermarking"><bean:message key="label.wms.globalWatermarking"/></html:checkbox> 
		</td>
		</tr>
	<tr><td align="right">
		<span class="help" title="<bean:message key="help.wms.globalWatermarkingURL"/>">
			<bean:message key="label.wms.globalWatermarkingURL"/>:
		</span>
		</td><td>
			<html:text property="globalWatermarkingURL" size="60"/>
		</td></tr>
    <tr><td align="right">
		<span class="help" title="<bean:message key="help.wms.watermarkTransparency"/>">
			<bean:message key="label.wms.watermarkTransparency"/>:
		</span>
		</td><td>
			<html:text property="watermarkTransparency" size="3"/>
		</td></tr>
   <tr>
    <td align="right">
		<span class="help" title="<bean:message key="help.wms.watermarkPosition"/>">
          <bean:message key="label.wms.watermarkPosition"/>:
        </span>
	  </td>
	  <td class="datum">
	  	<html:radio property="watermarkPosition" value="0"/> -- <html:radio property="watermarkPosition" value="1"/> -- <html:radio property="watermarkPosition" value="2"/><br/>
     	<br/>
     	<html:radio property="watermarkPosition" value="3"/> -- <html:radio property="watermarkPosition" value="4"/> -- <html:radio property="watermarkPosition" value="5"/><br/>
     	<br/>
     	<html:radio property="watermarkPosition" value="6"/> -- <html:radio property="watermarkPosition" value="7"/> -- <html:radio property="watermarkPosition" value="8"/><br/>
	  </td>
	</tr>
    <!-- Watermarking Options - END -->

	<tr><td align="right">&nbsp;</td><td>
		<html:submit>
			<bean:message key="label.submit"/>
		</html:submit>
		
		<html:reset>
			<bean:message key="label.reset"/>
		</html:reset>
	</td></tr>
	</html:form>
	
</table>