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

  <c:if test="${not empty param.errorMessage}">
    <div class="error-message">
      <p>${param.errorMessage}</p>
    </div>
  </c:if>
  
  <div id="container">
    <div id="geodash-landing-top-div">
      <header id="header">
        <div id="header-link-container" class="text-right">
          <a href="${pageContext.request.contextPath}/menu" title="<gdb:localize key="userMenu.menuTooltip"/>">
            <img id="logo-icon" class="img-responsive" src="net/geoprism/images/splash_logo_icon.png" alt="logo"/>
          </a>
          <p id="user-link-container" class="text-right">                
            <c:if test="${isAdmin}">
              <a class="user-command-link" href="${pageContext.request.contextPath}/app"><gdb:localize key="geoprismLanding.administration"/></a>
              <i class="user-command-link"> | </i>          
            </c:if>
            <a id="account-btn" class="user-command-link" href="#" class="link-active"><gdb:localize key="userDashboards.account"/></a>
            <i class="user-command-link"> | </i>
             <a class="user-command-link" href="${pageContext.request.contextPath}/session/logout"><gdb:localize key="userDashboards.logout"/></a>
           </p>
         </div>
        <div class="row-fluid header-logo-row">
          <div class="hidden-xs col-md-1"></div>
          <div class="col-md-8">
              <img id="logo" class="pull-left img-responsive" src="${banner}" alt="logo"/>
          </div>
          <div class="hidden-xs col-md-3"></div>
         </div>
         
       </header>
    </div>    
    <div id="geodash-landing-bottom-div">
      <div id="mask"></div>
      <div class="nav-icon-container container-fluid">
          <div class="row-fluid vertical-center-row">
              <div class="col-sm-3 hidden-xs col-md-2"></div>
              <div class="col-xs-12 col-sm-6 col-md-8">
              
                <div class="row-fluid">
                   <c:forEach var="application" items="${applications}" varStatus="loop">
                     <div class="col-xs-12 col-sm-6 col-md-3 text-center">
                     <div id="${application.id}" class="nav-option">
                        <img class="nav-icon-img img-responsive" src="${application.src}" alt="Navigation" />
                        <h3 class="nav-icon-img-label">${application.label}</h3>
                      </div>                
                     </div>
                   
                     <script type="text/javascript">    
                       $(document).ready(function(){
                         $("#${application.id}").click(function(){
                           window.open(window.location.origin +"${pageContext.request.contextPath}/${application.url}", "_self");
                         });
                       });
                     </script>                  
                   </c:forEach>              
                  </div>
                    
              <!-- TO ADD MORE NAV OPTIONS SIMPLY ADD ANOTHER ROW-FLUID WITH CONTENTS LIKE ABOVE -->
              </div>
              <div class="col-sm-3 hidden-xs col-md-8"></div>
            </div>
         </div>
            
      <div id="geodash-landing-footer">
        <h4><gdb:localize key="geoprismLanding.footerMessage"/></h4>
      </div>
    </div>
  
  </div>

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
  
  
