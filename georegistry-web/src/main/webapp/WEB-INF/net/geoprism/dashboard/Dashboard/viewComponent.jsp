<%@ taglib uri="/WEB-INF/tlds/runwayLib.tld" prefix="mjl"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="page_title" scope="request" value="View a Dashboard"/>
<dl>
  <mjl:form method="POST" name="net.geoprism.dashboard.Dashboard.form.name" id="net.geoprism.dashboard.Dashboard.form.id">
    <mjl:input param="id" type="hidden" value="${item.id}" />
    <mjl:component item="${item}" param="dto">
      <mjl:dt attribute="dashboardRole">
        ${item.dashboardRole.keyName}
      </mjl:dt>
      <mjl:dt attribute="description">
        ${item.description}
      </mjl:dt>
      <mjl:dt attribute="displayLabel">
        ${item.displayLabel}
      </mjl:dt>
      <mjl:dt attribute="filterDate">
        ${item.filterDate.keyName}
      </mjl:dt>
      <mjl:dt attribute="fromDate">
        ${item.fromDate}
      </mjl:dt>
      <mjl:dt attribute="name">
        ${item.name}
      </mjl:dt>
      <mjl:dt attribute="removable">
        ${item.removable ? item.removableMd.positiveDisplayLabel : item.removableMd.negativeDisplayLabel}
      </mjl:dt>
      <mjl:dt attribute="toDate">
        ${item.toDate}
      </mjl:dt>
    </mjl:component>
    <mjl:command name="net.geoprism.dashboard.Dashboard.form.edit.button" action="net.geoprism.dashboard.DashboardController.edit.mojo" value="Edit" />
  </mjl:form>
</dl>
<dl>
</dl>
<mjl:commandLink name="net.geoprism.dashboard.Dashboard.viewAll.link" action="net.geoprism.dashboard.DashboardController.viewAll.mojo">
  View All
</mjl:commandLink>
