<%--

    Copyright (c) 2019 TerraFrame, Inc. All rights reserved.

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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>
<%@ taglib uri="/WEB-INF/tlds/geoprism.tld" prefix="gdb"%>
<%@ taglib uri="/WEB-INF/tlds/runwayLib.tld" prefix="mjl"%>
<%@ taglib uri="http://jawr.net/tags" prefix="jwr" %>

<head>
  <gdb:localize var="page_title" key="login.header"/>
  
  <c:choose>
    <c:when test="${not empty miniLogoFilePath}" >
      <link rel="icon" href="${miniLogoFilePath}">
    </c:when>
    <c:otherwise>
      <link rel="icon" href="${pageContext.request.contextPath}/net/geoprism/images/splash_logo_icon.png">
    </c:otherwise>
  </c:choose>

    <!-- User account CSS -->
  <jwr:style src="/bundles/datatable.css" useRandomParam="false"/>  
  <jwr:style src="/net/geoprism/userstable/UsersTable.css" useRandomParam="false"/>  
  <jwr:style src="/bundles/user-menu.css" useRandomParam="false"/>  
  
    <!-- User account Javascript -->
  <jwr:script src="/bundles/datatablejquery.js" useRandomParam="false"/>
  <jwr:script src="/bundles/datatable.js" useRandomParam="false"/>
  <jwr:script src="/bundles/account.js" useRandomParam="false"/>  
  
  <script type="text/javascript">${js}</script>
</head>
<body>
georegistry-userMenu-jsp-1
  </body>
</html>

<script type="text/javascript">     
  $(document).ready(function(){
    com.runwaysdk.ui.Manager.setFactory("JQuery");
          
    $("#account-btn").on("click", function(){

    var dialog = com.runwaysdk.ui.Manager.getFactory().newDialog(com.runwaysdk.Localize.get("accountSettings", "Account Settings"), {modal: true, width: 600});
    dialog.addButton(com.runwaysdk.Localize.get("rOk", "Ok"), function(){
      dialog.close();
    }, null, {primary: true});
    dialog.setStyle("z-index", 2001);
    dialog.render();    
                
    var ut = new com.runwaysdk.ui.userstable.UserForm();  
      ut.render("#"+dialog.getContentEl().getId());
    });
  });
</script>
  
  
