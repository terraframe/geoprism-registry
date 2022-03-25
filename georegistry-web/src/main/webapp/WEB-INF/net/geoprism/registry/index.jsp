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
<%@page import="net.geoprism.registry.service.RegistryService"%>
<%@ page import="net.geoprism.registry.GeoregistryProperties"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tlds/geoprism.tld" prefix="gdb"%>
<%@ taglib uri="http://jawr.net/tags" prefix="jwr" %>

<%@page import="net.geoprism.localization.LocalizationFacadeDTO"%>
<%@page import="com.runwaysdk.constants.ClientConstants"%>
<%@page import="com.runwaysdk.constants.ClientRequestIF"%>

<%
  ClientRequestIF clientRequest = (ClientRequestIF) request.getAttribute(ClientConstants.CLIENTREQUEST);

  request.setAttribute("ilocales", RegistryService.getInstance().getLocales(clientRequest.getSessionId()));
  request.setAttribute("locale", RegistryService.getInstance().getCurrentLocale(clientRequest.getSessionId()));
%>


<!DOCTYPE html>

<head>
  <meta charset='utf-8' />
  <meta name='viewport' content='initial-scale=1,maximum-scale=1,user-scalable=no' />
  
  <title><gdb:localize key="cgr.title"/></title>
  <link rel="icon" href="${pageContext.request.contextPath}/net/geoprism/images/splash_logo_icon.png"/>  
  
  <base href="<%=request.getContextPath()%>/cgr/manage">
  
  <style>
    body { background-color:#efe9e1 }
  </style>  
  
  <script>
    window.registry = {
    		contextPath: "<%=request.getContextPath()%>",
    		locale:'<%=request.getAttribute("locale")%>', 
    		locales:<%=request.getAttribute("ilocales")%>,
    		searchEnabled:<%=GeoregistryProperties.isSearchEnabled().toString()%>,
    		graphVisualizerEnabled:<%=GeoregistryProperties.isGraphVisualizerEnabled().toString()%>,    		
        enableBusinessData:<%=GeoregistryProperties.isBusinessDataEnabled()%>,
    		defaultMapBounds: JSON.parse("<%=GeoregistryProperties.getDefaultMapBounds()%>")
    }; 
    window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '');   
    window.googleanalyticstoken = "<%=GeoregistryProperties.getGoogleAnalyticsToken()%>";
    
  </script>
  
   <link href="https://fonts.googleapis.com/css?family=Roboto" rel="stylesheet">
  
  <c:if test="${not empty requestScope['customFont'] and requestScope['customFont'] == 'Phetsarath OT'}">
	  <style>
	    @font-face {
	      font-family: 'Phetsarath OT';
	      font-style: normal;
	      font-weight: 400;
	      src: url(https://s3.us-west-2.amazonaws.com/geoprism.net/cdn/Phetsarath+OT.ttf) format('ttf');
	    }
	  </style>
  </c:if>
  
  <!-- CSS imports -->
  <jwr:style src="/bundles/datatable.css" useRandomParam="false"/>
  <jwr:style src="/bundles/main.css" useRandomParam="false" />
  <jwr:style src="/bundles/administration.css" useRandomParam="false" />
  
  <!-- Default imports -->  
  <jwr:script src="/bundles/runway.js" useRandomParam="false"/> 
  <jwr:script src="/bundles/main.js" useRandomParam="false"/>  
  <jwr:script src="/bundles/localization.js" useRandomParam="false"/>
    
  <script type="text/javascript" src="${pageContext.request.contextPath}/net/geoprism/Localized.js.jsp"></script>
  
  <!-- Global site tag (gtag.js) - Google Analytics -->
  <script async src="https://www.googletagmanager.com/gtag/js?id=G-BZ81VQJG2N"></script>
  <script>
  	if(googleanalyticstoken && googleanalyticstoken.length > 0){
      window.dataLayer = window.dataLayer || [];
      function gtag(){dataLayer.push(arguments);}
      gtag('js', new Date());
    
      gtag('config', googleanalyticstoken);
  	}
  </script>
  
</head>

<style>
:root {
  --cgr-font: ${not empty requestScope['customFont'] ? requestScope['customFont'] : "Roboto"};
}
</style>

<body>
    <cgr-app>
    <style type="text/css">
      cgr-app {
        display: flex;
        justify-content: center;
        align-items: center;
        height: 100vh;

        color: #7C868D;
        font-family:
          ${not empty requestScope['customFont'] ? requestScope['customFont'] : ""}
          -apple-system,
          BlinkMacSystemFont,
          "Segoe UI",
          Roboto,
          Oxygen-Sans,
          Ubuntu,
          Cantarell,
          Helvetica,
          sans-serif;
        font-size: 5em;
        text-shadow: 2px 2px 10px rgba(0,0,0,0.2);
      }
      
      body {
        background: white;
        margin: 0;
        padding: 0;
      }

      @keyframes dots {
        50% {
          transform: translateY(-.4rem);
        }
        100% {
          transform: translateY(0);
        }
      }

      .d {
       animation: dots 1.5s ease-out infinite;
      }
      .d-2 {
        animation-delay: .5s;
      }
      .d-3 {
        animation-delay: 1s;
      }
    </style>
    <span class="d">.</span><span class="d d-2">.</span><span class="d d-3">.</span>
    
    </cgr-app>


  <!-- IE required polyfills, in this exact order -->    
<%--   <script type="text/javascript" src="${pageContext.request.contextPath}/dist/cgr-polyfills.js"></script> --%>
<%--   <script type="text/javascript" src="${pageContext.request.contextPath}/dist/vendor.chunk.js"></script> --%>
<%--   <script type="text/javascript" src="${pageContext.request.contextPath}/dist/cgr-vendor.js"></script> --%>
<%--   <script type="text/javascript" src="${pageContext.request.contextPath}/dist/cgr-app.js"></script> --%>
  

  <script type="text/javascript" src="$local.host$/dist/cgr-polyfills.js"></script>  
  <script type="text/javascript" src="$local.host$/dist/vendor.chunk.js"></script>  
  <script type="text/javascript" src="$local.host$/dist/cgr-vendor.js"></script>    
  <script type="text/javascript" src="$local.host$/dist/cgr-app.js"></script>

</body>
  