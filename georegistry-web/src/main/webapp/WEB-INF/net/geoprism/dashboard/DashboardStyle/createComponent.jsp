<%@ taglib uri="/WEB-INF/tlds/runwayLib.tld" prefix="mjl"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="page_title" scope="request" value="Create a new Style"/>
<dl>
  <mjl:form method="POST" name="net.geoprism.dashboard.DashboardStyle.form.name" id="net.geoprism.dashboard.DashboardStyle.form.id">
    <%@include file="form.jsp" %>
    <mjl:command name="net.geoprism.dashboard.DashboardStyle.form.create.button" action="net.geoprism.dashboard.DashboardStyleController.create.mojo" value="Create" />
  </mjl:form>
</dl>
