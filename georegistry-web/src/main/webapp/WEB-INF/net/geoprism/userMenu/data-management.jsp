<%--

    Copyright (c) 2015 TerraFrame, Inc. All rights reserved.

    This file is part of Runway SDK(tm).

    Runway SDK(tm) is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    Runway SDK(tm) is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ taglib uri="/WEB-INF/tlds/geoprism.tld" prefix="gdb"%>
<%@ taglib uri="http://jawr.net/tags" prefix="jwr" %>


<html ng-app="data-management">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="cache-control" content="max-age=0" />
    <meta http-equiv="cache-control" content="no-cache" />
    <meta http-equiv="expires" content="0" />
    <meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT" />
    <meta http-equiv="pragma" content="no-cache" />

    <title><gdb:localize key="dataset.title"/></title>
    
    <!-- Tell Runway what the application context path is. -->
    <script>
      window.com = window.com || {};
      window.com.runwaysdk = window.com.runwaysdk || {};
      window.com.runwaysdk.__applicationContextPath = "<%=request.getContextPath()%>";
    </script>
    
  
    <!-- CSS imports -->
    <jwr:style src="/bundles/main.css" useRandomParam="false" />
    <jwr:style src="/bundles/administration.css" useRandomParam="false" />
    <jwr:style src="/bundles/widget.css" useRandomParam="false"/>  
<%--     <jwr:style src="/bundles/dynamic-map.css" useRandomParam="false"/>   --%>
    <jwr:style src="/bundles/webgl-map-current.css" useRandomParam="false"/>  
    
  
    <!-- Default imports -->
    <jwr:script src="/bundles/runway.js" useRandomParam="false"/> 
    <jwr:script src="/bundles/main.js" useRandomParam="false"/>  
    <jwr:script src="/bundles/widget.js" useRandomParam="false"/> 
    <jwr:script src="/bundles/localization.js" useRandomParam="false"/>
    
    <script type="text/javascript" src="${pageContext.request.contextPath}/net/geoprism/Localized.js.jsp"></script>
    
    <!-- Individual Page Javascript -->
<%--     <jwr:script src="/bundles/dynamic-map.js" useRandomParam="false"/> --%>
    <jwr:script src="/bundles/management.js" useRandomParam="false"/>

    <%-- <script src="${pageContext.request.contextPath}/net/geoprism/MapConfig.json"></script>  --%>
    <jwr:script src="/bundles/webgl-map.js" useRandomParam="false"/>
    
    <script type="text/javascript">${js}</script>
    
    <script type="text/javascript">
      com.runwaysdk.ui.Manager.setFactory("JQuery");
    </script>    
  </head>
  <body ng-controller="DataManagementController" id="innerFrameHtml">
    <!-- HEADER AND NAVBAR -->
    <header>
      <nav class="navbar navbar-default">
        <div class="container">
          <div class="navbar-header">
            <a class="navbar-brand" href="${pageContext.request.contextPath}/prism/management#/menu" title="<gdb:localize key="userMenu.menuTooltip"/>"><img src="${pageContext.request.contextPath}/logo/view?id=logo" /></a>
            <a class="navbar-brand" href="${pageContext.request.contextPath}/prism/home#/menu"><gdb:localize key="cgr.title"/></a>
<%-- 			<h3 class="navbar-brand"><gdb:localize key="data.management.title"/></h3> --%>
          </div>

          <ul class="nav navbar-nav navbar-right">
            <li class="dropdown">
            	<a class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false"><gdb:localize key="Location_Configurations"/><span class="caret"></span></a>
            	<ul class="dropdown-menu">
            		<li><a href="#locations"><i></i><gdb:localize key="Location_Management"/></a></li>
            	</ul>
            </li>

<%--             <li><a href="${pageContext.request.contextPath}/menu" title="<gdb:localize key="userMenu.menuTooltip"/>"><img src="net/geoprism/images/splash_logo_icon.png" /></a></li> --%>
<%--             <li><a href="${pageContext.request.contextPath}/session/logout"><gdb:localize key="userDashboards.logout"/></a></li>             --%>
          </ul>
        </div>
      </nav>
    </header>
        
    <!-- MAIN CONTENT AND INJECTED VIEWS -->
    <div id="main" class="new-admin-design-main">
      <!-- angular templating -->
      <!-- this is where content will be injected -->
      <ng-view></ng-view> 
    </div>
  </body>
</html>
