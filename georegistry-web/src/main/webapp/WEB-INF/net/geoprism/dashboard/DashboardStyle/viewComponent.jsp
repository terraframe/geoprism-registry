<%@ taglib uri="/WEB-INF/tlds/runwayLib.tld" prefix="mjl"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="page_title" scope="request" value="View a Style"/>
<dl>
  <mjl:form method="POST" name="net.geoprism.dashboard.DashboardStyle.form.name" id="net.geoprism.dashboard.DashboardStyle.form.id">
    <mjl:input param="id" type="hidden" value="${item.id}" />
    <mjl:component item="${item}" param="dto">
      <mjl:dt attribute="basicPointSize">
        ${item.basicPointSize}
      </mjl:dt>
      <mjl:dt attribute="enableLabel">
        ${item.enableLabel ? item.enableLabelMd.positiveDisplayLabel : item.enableLabelMd.negativeDisplayLabel}
      </mjl:dt>
      <mjl:dt attribute="enableValue">
        ${item.enableValue ? item.enableValueMd.positiveDisplayLabel : item.enableValueMd.negativeDisplayLabel}
      </mjl:dt>
      <mjl:dt attribute="labelColor">
        ${item.labelColor}
      </mjl:dt>
      <mjl:dt attribute="labelFont">
        ${item.labelFont}
      </mjl:dt>
      <mjl:dt attribute="labelHalo">
        ${item.labelHalo}
      </mjl:dt>
      <mjl:dt attribute="labelHaloWidth">
        ${item.labelHaloWidth}
      </mjl:dt>
      <mjl:dt attribute="labelSize">
        ${item.labelSize}
      </mjl:dt>
      <mjl:dt attribute="lineOpacity">
        ${item.lineOpacity}
      </mjl:dt>
      <mjl:dt attribute="lineStroke">
        ${item.lineStroke}
      </mjl:dt>
      <mjl:dt attribute="lineStrokeCap">
        ${item.lineStrokeCap}
      </mjl:dt>
      <mjl:dt attribute="lineStrokeWidth">
        ${item.lineStrokeWidth}
      </mjl:dt>
      <mjl:dt attribute="name">
        ${item.name}
      </mjl:dt>
      <mjl:dt attribute="pointFill">
        ${item.pointFill}
      </mjl:dt>
      <mjl:dt attribute="pointOpacity">
        ${item.pointOpacity}
      </mjl:dt>
      <mjl:dt attribute="pointRotation">
        ${item.pointRotation}
      </mjl:dt>
      <mjl:dt attribute="pointStroke">
        ${item.pointStroke}
      </mjl:dt>
      <mjl:dt attribute="pointStrokeOpacity">
        ${item.pointStrokeOpacity}
      </mjl:dt>
      <mjl:dt attribute="pointStrokeWidth">
        ${item.pointStrokeWidth}
      </mjl:dt>
      <mjl:dt attribute="pointWellKnownName">
        ${item.pointWellKnownName}
      </mjl:dt>
      <mjl:dt attribute="polygonFill">
        ${item.polygonFill}
      </mjl:dt>
      <mjl:dt attribute="polygonFillOpacity">
        ${item.polygonFillOpacity}
      </mjl:dt>
      <mjl:dt attribute="polygonStroke">
        ${item.polygonStroke}
      </mjl:dt>
      <mjl:dt attribute="polygonStrokeOpacity">
        ${item.polygonStrokeOpacity}
      </mjl:dt>
      <mjl:dt attribute="polygonStrokeWidth">
        ${item.polygonStrokeWidth}
      </mjl:dt>
      <mjl:dt attribute="valueColor">
        ${item.valueColor}
      </mjl:dt>
      <mjl:dt attribute="valueFont">
        ${item.valueFont}
      </mjl:dt>
      <mjl:dt attribute="valueHalo">
        ${item.valueHalo}
      </mjl:dt>
      <mjl:dt attribute="valueHaloWidth">
        ${item.valueHaloWidth}
      </mjl:dt>
      <mjl:dt attribute="valueSize">
        ${item.valueSize}
      </mjl:dt>
    </mjl:component>
    <mjl:command name="net.geoprism.dashboard.DashboardStyle.form.edit.button" action="net.geoprism.dashboard.DashboardStyleController.edit.mojo" value="Edit" />
  </mjl:form>
</dl>
<dl>
</dl>
<mjl:commandLink name="net.geoprism.dashboard.DashboardStyle.viewAll.link" action="net.geoprism.dashboard.DashboardStyleController.viewAll.mojo">
  View All
</mjl:commandLink>
