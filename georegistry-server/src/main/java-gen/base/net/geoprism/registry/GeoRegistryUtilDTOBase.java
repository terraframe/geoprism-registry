/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = 81747895)
public abstract class GeoRegistryUtilDTOBase extends com.runwaysdk.business.UtilDTO
{
  public final static String CLASS = "net.geoprism.registry.GeoRegistryUtil";
  private static final long serialVersionUID = 81747895;
  
  protected GeoRegistryUtilDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String OID = "oid";
  public static final java.lang.String applyClassificationType(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String json)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{json};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.GeoRegistryUtilDTO.CLASS, "applyClassificationType", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final java.lang.String createHierarchyType(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String htJSON)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{htJSON};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.GeoRegistryUtilDTO.CLASS, "createHierarchyType", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final java.io.InputStream exportShapefile(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String code, java.lang.String hierarchyCode)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String"};
    Object[] _parameters = new Object[]{code, hierarchyCode};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.GeoRegistryUtilDTO.CLASS, "exportShapefile", _declaredTypes);
    return (java.io.InputStream) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final java.io.InputStream exportSpreadsheet(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String code, java.lang.String hierarchyCode)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String"};
    Object[] _parameters = new Object[]{code, hierarchyCode};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.GeoRegistryUtilDTO.CLASS, "exportSpreadsheet", _declaredTypes);
    return (java.io.InputStream) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final void importTypes(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String orgCode, java.io.InputStream istream)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.io.InputStream"};
    Object[] _parameters = new Object[]{orgCode, istream};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.GeoRegistryUtilDTO.CLASS, "importTypes", _declaredTypes);
    clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final void submitChangeRequest(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String sJson)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{sJson};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.registry.GeoRegistryUtilDTO.CLASS, "submitChangeRequest", _declaredTypes);
    clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static GeoRegistryUtilDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.UtilDTO dto = (com.runwaysdk.business.UtilDTO)clientRequest.get(oid);
    
    return (GeoRegistryUtilDTO) dto;
  }
  
  public void apply()
  {
    if(isNewInstance())
    {
      getRequest().createSessionComponent(this);
    }
    else
    {
      getRequest().update(this);
    }
  }
  public void delete()
  {
    getRequest().delete(this.getOid());
  }
  
}
