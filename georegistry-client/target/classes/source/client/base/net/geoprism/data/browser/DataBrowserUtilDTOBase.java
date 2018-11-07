/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Runway SDK(tm).
 *
 * Runway SDK(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Runway SDK(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.data.browser;

@com.runwaysdk.business.ClassSignature(hash = 1211704731)
public abstract class DataBrowserUtilDTOBase extends com.runwaysdk.business.UtilDTO 
{
  public final static String CLASS = "net.geoprism.data.browser.DataBrowserUtil";
  private static final long serialVersionUID = 1211704731;
  
  protected DataBrowserUtilDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String OID = "oid";
  public static final void deleteData(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String type)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{type};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.data.browser.DataBrowserUtilDTO.CLASS, "deleteData", _declaredTypes);
    clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final net.geoprism.data.browser.MetadataTypeQueryDTO getDefaultTypes(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    String[] _declaredTypes = new String[]{};
    Object[] _parameters = new Object[]{};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.data.browser.DataBrowserUtilDTO.CLASS, "getDefaultTypes", _declaredTypes);
    return (net.geoprism.data.browser.MetadataTypeQueryDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final net.geoprism.data.browser.MetadataTypeQueryDTO getTypes(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String[] packages, java.lang.String[] types)
  {
    String[] _declaredTypes = new String[]{"[Ljava.lang.String;", "[Ljava.lang.String;"};
    Object[] _parameters = new Object[]{packages, types};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.data.browser.DataBrowserUtilDTO.CLASS, "getTypes", _declaredTypes);
    return (net.geoprism.data.browser.MetadataTypeQueryDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static DataBrowserUtilDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.UtilDTO dto = (com.runwaysdk.business.UtilDTO)clientRequest.get(oid);
    
    return (DataBrowserUtilDTO) dto;
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
