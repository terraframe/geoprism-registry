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
package net.geoprism.dashboard.layer;

@com.runwaysdk.business.ClassSignature(hash = -319932432)
public abstract class DashboardThematicLayerDTOBase extends net.geoprism.dashboard.layer.DashboardLayerDTO 
{
  public final static String CLASS = "net.geoprism.dashboard.layer.DashboardThematicLayer";
  private static final long serialVersionUID = -319932432;
  
  protected DashboardThematicLayerDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected DashboardThematicLayerDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String AGGREGATIONSTRATEGY = "aggregationStrategy";
  public static java.lang.String AGGREGATIONTYPE = "aggregationType";
  public static java.lang.String GEONODE = "geoNode";
  public static java.lang.String MDATTRIBUTE = "mdAttribute";
  public net.geoprism.dashboard.AggregationStrategyDTO getAggregationStrategy()
  {
    if(getValue(AGGREGATIONSTRATEGY) == null || getValue(AGGREGATIONSTRATEGY).trim().equals(""))
    {
      return null;
    }
    else
    {
      return net.geoprism.dashboard.AggregationStrategyDTO.get(getRequest(), getValue(AGGREGATIONSTRATEGY));
    }
  }
  
  public String getAggregationStrategyId()
  {
    return getValue(AGGREGATIONSTRATEGY);
  }
  
  public void setAggregationStrategy(net.geoprism.dashboard.AggregationStrategyDTO value)
  {
    if(value == null)
    {
      setValue(AGGREGATIONSTRATEGY, "");
    }
    else
    {
      setValue(AGGREGATIONSTRATEGY, value.getOid());
    }
  }
  
  public boolean isAggregationStrategyWritable()
  {
    return isWritable(AGGREGATIONSTRATEGY);
  }
  
  public boolean isAggregationStrategyReadable()
  {
    return isReadable(AGGREGATIONSTRATEGY);
  }
  
  public boolean isAggregationStrategyModified()
  {
    return isModified(AGGREGATIONSTRATEGY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getAggregationStrategyMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(AGGREGATIONSTRATEGY).getAttributeMdDTO();
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<net.geoprism.dashboard.AllAggregationTypeDTO> getAggregationType()
  {
    return (java.util.List<net.geoprism.dashboard.AllAggregationTypeDTO>) com.runwaysdk.transport.conversion.ConversionFacade.convertEnumDTOsFromEnumNames(getRequest(), net.geoprism.dashboard.AllAggregationTypeDTO.CLASS, getEnumNames(AGGREGATIONTYPE));
  }
  
  public java.util.List<String> getAggregationTypeEnumNames()
  {
    return getEnumNames(AGGREGATIONTYPE);
  }
  
  public void addAggregationType(net.geoprism.dashboard.AllAggregationTypeDTO enumDTO)
  {
    addEnumItem(AGGREGATIONTYPE, enumDTO.toString());
  }
  
  public void removeAggregationType(net.geoprism.dashboard.AllAggregationTypeDTO enumDTO)
  {
    removeEnumItem(AGGREGATIONTYPE, enumDTO.toString());
  }
  
  public void clearAggregationType()
  {
    clearEnum(AGGREGATIONTYPE);
  }
  
  public boolean isAggregationTypeWritable()
  {
    return isWritable(AGGREGATIONTYPE);
  }
  
  public boolean isAggregationTypeReadable()
  {
    return isReadable(AGGREGATIONTYPE);
  }
  
  public boolean isAggregationTypeModified()
  {
    return isModified(AGGREGATIONTYPE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeEnumerationMdDTO getAggregationTypeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeEnumerationMdDTO) getAttributeDTO(AGGREGATIONTYPE).getAttributeMdDTO();
  }
  
  public com.runwaysdk.system.gis.geo.GeoNodeDTO getGeoNode()
  {
    if(getValue(GEONODE) == null || getValue(GEONODE).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.gis.geo.GeoNodeDTO.get(getRequest(), getValue(GEONODE));
    }
  }
  
  public String getGeoNodeId()
  {
    return getValue(GEONODE);
  }
  
  public void setGeoNode(com.runwaysdk.system.gis.geo.GeoNodeDTO value)
  {
    if(value == null)
    {
      setValue(GEONODE, "");
    }
    else
    {
      setValue(GEONODE, value.getOid());
    }
  }
  
  public boolean isGeoNodeWritable()
  {
    return isWritable(GEONODE);
  }
  
  public boolean isGeoNodeReadable()
  {
    return isReadable(GEONODE);
  }
  
  public boolean isGeoNodeModified()
  {
    return isModified(GEONODE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getGeoNodeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(GEONODE).getAttributeMdDTO();
  }
  
  public com.runwaysdk.system.metadata.MdAttributeDTO getMdAttribute()
  {
    if(getValue(MDATTRIBUTE) == null || getValue(MDATTRIBUTE).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.metadata.MdAttributeDTO.get(getRequest(), getValue(MDATTRIBUTE));
    }
  }
  
  public String getMdAttributeId()
  {
    return getValue(MDATTRIBUTE);
  }
  
  public void setMdAttribute(com.runwaysdk.system.metadata.MdAttributeDTO value)
  {
    if(value == null)
    {
      setValue(MDATTRIBUTE, "");
    }
    else
    {
      setValue(MDATTRIBUTE, value.getOid());
    }
  }
  
  public boolean isMdAttributeWritable()
  {
    return isWritable(MDATTRIBUTE);
  }
  
  public boolean isMdAttributeReadable()
  {
    return isReadable(MDATTRIBUTE);
  }
  
  public boolean isMdAttributeModified()
  {
    return isModified(MDATTRIBUTE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getMdAttributeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(MDATTRIBUTE).getAttributeMdDTO();
  }
  
  public final java.lang.String applyWithStyleAndStrategy(net.geoprism.dashboard.DashboardStyleDTO style, java.lang.String mapId, net.geoprism.dashboard.AggregationStrategyDTO strategy, java.lang.String state)
  {
    String[] _declaredTypes = new String[]{"net.geoprism.dashboard.DashboardStyle", "java.lang.String", "net.geoprism.dashboard.AggregationStrategy", "java.lang.String"};
    Object[] _parameters = new Object[]{style, mapId, strategy, state};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.layer.DashboardThematicLayerDTO.CLASS, "applyWithStyleAndStrategy", _declaredTypes);
    return (java.lang.String) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final java.lang.String applyWithStyleAndStrategy(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid, net.geoprism.dashboard.DashboardStyleDTO style, java.lang.String mapId, net.geoprism.dashboard.AggregationStrategyDTO strategy, java.lang.String state)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "net.geoprism.dashboard.DashboardStyle", "java.lang.String", "net.geoprism.dashboard.AggregationStrategy", "java.lang.String"};
    Object[] _parameters = new Object[]{oid, style, mapId, strategy, state};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.layer.DashboardThematicLayerDTO.CLASS, "applyWithStyleAndStrategy", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public final java.lang.String getFeatureInformation(java.lang.String featureId)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{featureId};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.layer.DashboardThematicLayerDTO.CLASS, "getFeatureInformation", _declaredTypes);
    return (java.lang.String) getRequest().invokeMethod(_metadata, this, _parameters);
  }
  
  public static final java.lang.String getFeatureInformation(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid, java.lang.String featureId)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String"};
    Object[] _parameters = new Object[]{oid, featureId};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.layer.DashboardThematicLayerDTO.CLASS, "getFeatureInformation", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final java.lang.String getGeoNodeGeometryTypesJSON(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String geoNodeId)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{geoNodeId};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.layer.DashboardThematicLayerDTO.CLASS, "getGeoNodeGeometryTypesJSON", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static final java.lang.String getOptionsJSON(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String thematicAttributeId, java.lang.String dashboardId)
  {
    String[] _declaredTypes = new String[]{"java.lang.String", "java.lang.String"};
    Object[] _parameters = new Object[]{thematicAttributeId, dashboardId};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.layer.DashboardThematicLayerDTO.CLASS, "getOptionsJSON", _declaredTypes);
    return (java.lang.String) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public static net.geoprism.dashboard.layer.DashboardThematicLayerDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.dashboard.layer.DashboardThematicLayerDTO) dto;
  }
  
  public void apply()
  {
    if(isNewInstance())
    {
      getRequest().createBusiness(this);
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
  
  public static net.geoprism.dashboard.layer.DashboardThematicLayerQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.dashboard.layer.DashboardThematicLayerQueryDTO) clientRequest.getAllInstances(net.geoprism.dashboard.layer.DashboardThematicLayerDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.dashboard.layer.DashboardThematicLayerDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.layer.DashboardThematicLayerDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.dashboard.layer.DashboardThematicLayerDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.dashboard.layer.DashboardThematicLayerDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.layer.DashboardThematicLayerDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.dashboard.layer.DashboardThematicLayerDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
