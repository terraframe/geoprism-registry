<%--

    Copyright (c) 2022 TerraFrame, Inc. All rights reserved.

    This file is part of Geoprism Registry(tm).

    Geoprism Registry(tm) is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    Geoprism Registry(tm) is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.

--%>
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
