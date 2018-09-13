<%@ taglib uri="/WEB-INF/tlds/runwayLib.tld" prefix="mjl"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="page_title" scope="request" value="Edit an existing Style"/>
<dl>
  <mjl:form method="POST" name="net.geoprism.dashboard.DashboardStyle.form.name" id="net.geoprism.dashboard.DashboardStyle.form.id">
    <%@include file="form.jsp" %>
    <mjl:command name="net.geoprism.dashboard.DashboardStyle.form.update.button" action="net.geoprism.dashboard.DashboardStyleController.update.mojo" value="Update" />
    <mjl:command name="net.geoprism.dashboard.DashboardStyle.form.delete.button" action="net.geoprism.dashboard.DashboardStyleController.delete.mojo" value="Delete" />
    <mjl:command name="net.geoprism.dashboard.DashboardStyle.form.cancel.button" action="net.geoprism.dashboard.DashboardStyleController.cancel.mojo" value="Cancel" />
  </mjl:form>
</dl>
