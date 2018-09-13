<%@ taglib uri="/WEB-INF/tlds/runwayLib.tld" prefix="mjl"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="page_title" scope="request" value="Create a new Dashboard"/>
<dl>
  <mjl:form method="POST" name="net.geoprism.dashboard.Dashboard.form.name" id="net.geoprism.dashboard.Dashboard.form.id">
    <%@include file="form.jsp" %>
    <mjl:command name="net.geoprism.dashboard.Dashboard.form.create.button" action="net.geoprism.dashboard.DashboardController.create.mojo" value="Create" />
  </mjl:form>
</dl>
