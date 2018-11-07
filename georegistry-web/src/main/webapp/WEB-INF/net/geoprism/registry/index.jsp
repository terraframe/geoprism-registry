<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/tlds/geoprism.tld" prefix="gdb"%>
<%@ taglib uri="http://jawr.net/tags" prefix="jwr" %>

<!DOCTYPE html>

<head>
  <meta charset='utf-8' />
  <meta name='viewport' content='initial-scale=1,maximum-scale=1,user-scalable=no' />
  
  <title><gdb:localize key="project.management.title"/></title>
  <link rel="icon" href="${pageContext.request.contextPath}/net/geoprism/images/splash_logo_icon.png"/>  
  
  <base href="<%=request.getContextPath()%>/hierarchies">
  
  <style>
    body { background-color:#efe9e1 }
  </style>  
  
  <script>
    window.acp = "<%=request.getContextPath()%>"; 
    window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '');   
  </script>
  
  <!-- CSS imports -->
  <jwr:style src="/bundles/datatable.css" useRandomParam="false"/>
  <jwr:style src="/bundles/main.css" useRandomParam="false" />
  
  <!-- Default imports -->  
  <jwr:script src="/bundles/runway.js" useRandomParam="false"/> 
  <jwr:script src="/bundles/main.js" useRandomParam="false"/>  
  <jwr:script src="/bundles/localization.js" useRandomParam="false"/>
    
  <script type="text/javascript" src="${pageContext.request.contextPath}/net/geoprism/Localized.js.jsp"></script>
  
  <!-- IE required polyfills, in this exact order -->

<!--   <script type="text/javascript" src="https://localhost:8080/dist/cgr-polyfills.js"></script> -->
<!--   <script type="text/javascript" src="https://localhost:8080/dist/cgr-vendor.js"></script> -->
  <script type="text/javascript" src="${pageContext.request.contextPath}/dist/cgr-polyfills.js"></script>  
  <script type="text/javascript" src="${pageContext.request.contextPath}/dist/cgr-vendor.js"></script>  
</head>

<body>
<!--   <div> -->
    <uasdm-app>
    </uasdm-app>

<!--     <script type="text/javascript" src="https://localhost:8080/dist/uasdm-app.js"></script>       -->
    <script type="text/javascript" src="${pageContext.request.contextPath}/dist/uasdm-app.js"></script>      
<!--   </div> -->
</body>
  