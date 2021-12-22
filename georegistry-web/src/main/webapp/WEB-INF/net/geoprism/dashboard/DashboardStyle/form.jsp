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
<mjl:component item="${item}" param="dto">
  <mjl:dt attribute="basicPointSize">
    <mjl:input param="basicPointSize" type="text" />
  </mjl:dt>
  <mjl:dt attribute="enableLabel">
    <mjl:boolean param="enableLabel" />
  </mjl:dt>
  <mjl:dt attribute="enableValue">
    <mjl:boolean param="enableValue" />
  </mjl:dt>
  <mjl:dt attribute="labelColor">
    <mjl:input param="labelColor" type="text" />
  </mjl:dt>
  <mjl:dt attribute="labelFont">
    <mjl:input param="labelFont" type="text" />
  </mjl:dt>
  <mjl:dt attribute="labelHalo">
    <mjl:input param="labelHalo" type="text" />
  </mjl:dt>
  <mjl:dt attribute="labelHaloWidth">
    <mjl:input param="labelHaloWidth" type="text" />
  </mjl:dt>
  <mjl:dt attribute="labelSize">
    <mjl:input param="labelSize" type="text" />
  </mjl:dt>
  <mjl:dt attribute="lineOpacity">
    <mjl:input param="lineOpacity" type="text" />
  </mjl:dt>
  <mjl:dt attribute="lineStroke">
    <mjl:input param="lineStroke" type="text" />
  </mjl:dt>
  <mjl:dt attribute="lineStrokeCap">
    <mjl:input param="lineStrokeCap" type="text" />
  </mjl:dt>
  <mjl:dt attribute="lineStrokeWidth">
    <mjl:input param="lineStrokeWidth" type="text" />
  </mjl:dt>
  <mjl:dt attribute="name">
    <mjl:input param="name" type="text" />
  </mjl:dt>
  <mjl:dt attribute="pointFill">
    <mjl:input param="pointFill" type="text" />
  </mjl:dt>
  <mjl:dt attribute="pointOpacity">
    <mjl:input param="pointOpacity" type="text" />
  </mjl:dt>
  <mjl:dt attribute="pointRotation">
    <mjl:input param="pointRotation" type="text" />
  </mjl:dt>
  <mjl:dt attribute="pointStroke">
    <mjl:input param="pointStroke" type="text" />
  </mjl:dt>
  <mjl:dt attribute="pointStrokeOpacity">
    <mjl:input param="pointStrokeOpacity" type="text" />
  </mjl:dt>
  <mjl:dt attribute="pointStrokeWidth">
    <mjl:input param="pointStrokeWidth" type="text" />
  </mjl:dt>
  <mjl:dt attribute="pointWellKnownName">
    <mjl:input param="pointWellKnownName" type="text" />
  </mjl:dt>
  <mjl:dt attribute="polygonFill">
    <mjl:input param="polygonFill" type="text" />
  </mjl:dt>
  <mjl:dt attribute="polygonFillOpacity">
    <mjl:input param="polygonFillOpacity" type="text" />
  </mjl:dt>
  <mjl:dt attribute="polygonStroke">
    <mjl:input param="polygonStroke" type="text" />
  </mjl:dt>
  <mjl:dt attribute="polygonStrokeOpacity">
    <mjl:input param="polygonStrokeOpacity" type="text" />
  </mjl:dt>
  <mjl:dt attribute="polygonStrokeWidth">
    <mjl:input param="polygonStrokeWidth" type="text" />
  </mjl:dt>
  <mjl:dt attribute="valueColor">
    <mjl:input param="valueColor" type="text" />
  </mjl:dt>
  <mjl:dt attribute="valueFont">
    <mjl:input param="valueFont" type="text" />
  </mjl:dt>
  <mjl:dt attribute="valueHalo">
    <mjl:input param="valueHalo" type="text" />
  </mjl:dt>
  <mjl:dt attribute="valueHaloWidth">
    <mjl:input param="valueHaloWidth" type="text" />
  </mjl:dt>
  <mjl:dt attribute="valueSize">
    <mjl:input param="valueSize" type="text" />
  </mjl:dt>
</mjl:component>
