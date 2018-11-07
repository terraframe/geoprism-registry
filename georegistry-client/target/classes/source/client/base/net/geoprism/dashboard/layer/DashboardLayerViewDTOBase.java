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

@com.runwaysdk.business.ClassSignature(hash = -365034344)
public abstract class DashboardLayerViewDTOBase extends com.runwaysdk.business.ViewDTO 
{
  public final static String CLASS = "net.geoprism.dashboard.layer.DashboardLayerView";
  private static final long serialVersionUID = -365034344;
  
  protected DashboardLayerViewDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String ACTIVEBYDEFAULT = "activeByDefault";
  public static java.lang.String AGGREGATIONATTRIBUTE = "aggregationAttribute";
  public static java.lang.String AGGREGATIONMETHOD = "aggregationMethod";
  public static java.lang.String ATTRIBUTELABEL = "attributeLabel";
  public static java.lang.String DISPLAYINLEGEND = "displayInLegend";
  public static java.lang.String FEATURESTRATEGY = "featureStrategy";
  public static java.lang.String GROUPEDINLEGEND = "groupedInLegend";
  public static java.lang.String OID = "oid";
  public static java.lang.String LAYERID = "layerId";
  public static java.lang.String LAYERISACTIVE = "layerIsActive";
  public static java.lang.String LAYERNAME = "layerName";
  public static java.lang.String LEGENDXPOSITION = "legendXPosition";
  public static java.lang.String LEGENDYPOSITION = "legendYPosition";
  public static java.lang.String MDATTRIBUTE = "mdAttribute";
  public static java.lang.String SLDNAME = "sldName";
  public static java.lang.String VIEWNAME = "viewName";
  public Boolean getActiveByDefault()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(ACTIVEBYDEFAULT));
  }
  
  public void setActiveByDefault(Boolean value)
  {
    if(value == null)
    {
      setValue(ACTIVEBYDEFAULT, "");
    }
    else
    {
      setValue(ACTIVEBYDEFAULT, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isActiveByDefaultWritable()
  {
    return isWritable(ACTIVEBYDEFAULT);
  }
  
  public boolean isActiveByDefaultReadable()
  {
    return isReadable(ACTIVEBYDEFAULT);
  }
  
  public boolean isActiveByDefaultModified()
  {
    return isModified(ACTIVEBYDEFAULT);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getActiveByDefaultMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(ACTIVEBYDEFAULT).getAttributeMdDTO();
  }
  
  public String getAggregationAttribute()
  {
    return getValue(AGGREGATIONATTRIBUTE);
  }
  
  public void setAggregationAttribute(String value)
  {
    if(value == null)
    {
      setValue(AGGREGATIONATTRIBUTE, "");
    }
    else
    {
      setValue(AGGREGATIONATTRIBUTE, value);
    }
  }
  
  public boolean isAggregationAttributeWritable()
  {
    return isWritable(AGGREGATIONATTRIBUTE);
  }
  
  public boolean isAggregationAttributeReadable()
  {
    return isReadable(AGGREGATIONATTRIBUTE);
  }
  
  public boolean isAggregationAttributeModified()
  {
    return isModified(AGGREGATIONATTRIBUTE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getAggregationAttributeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(AGGREGATIONATTRIBUTE).getAttributeMdDTO();
  }
  
  public String getAggregationMethod()
  {
    return getValue(AGGREGATIONMETHOD);
  }
  
  public void setAggregationMethod(String value)
  {
    if(value == null)
    {
      setValue(AGGREGATIONMETHOD, "");
    }
    else
    {
      setValue(AGGREGATIONMETHOD, value);
    }
  }
  
  public boolean isAggregationMethodWritable()
  {
    return isWritable(AGGREGATIONMETHOD);
  }
  
  public boolean isAggregationMethodReadable()
  {
    return isReadable(AGGREGATIONMETHOD);
  }
  
  public boolean isAggregationMethodModified()
  {
    return isModified(AGGREGATIONMETHOD);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getAggregationMethodMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(AGGREGATIONMETHOD).getAttributeMdDTO();
  }
  
  public String getAttributeLabel()
  {
    return getValue(ATTRIBUTELABEL);
  }
  
  public void setAttributeLabel(String value)
  {
    if(value == null)
    {
      setValue(ATTRIBUTELABEL, "");
    }
    else
    {
      setValue(ATTRIBUTELABEL, value);
    }
  }
  
  public boolean isAttributeLabelWritable()
  {
    return isWritable(ATTRIBUTELABEL);
  }
  
  public boolean isAttributeLabelReadable()
  {
    return isReadable(ATTRIBUTELABEL);
  }
  
  public boolean isAttributeLabelModified()
  {
    return isModified(ATTRIBUTELABEL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getAttributeLabelMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(ATTRIBUTELABEL).getAttributeMdDTO();
  }
  
  public Boolean getDisplayInLegend()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(DISPLAYINLEGEND));
  }
  
  public void setDisplayInLegend(Boolean value)
  {
    if(value == null)
    {
      setValue(DISPLAYINLEGEND, "");
    }
    else
    {
      setValue(DISPLAYINLEGEND, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isDisplayInLegendWritable()
  {
    return isWritable(DISPLAYINLEGEND);
  }
  
  public boolean isDisplayInLegendReadable()
  {
    return isReadable(DISPLAYINLEGEND);
  }
  
  public boolean isDisplayInLegendModified()
  {
    return isModified(DISPLAYINLEGEND);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getDisplayInLegendMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(DISPLAYINLEGEND).getAttributeMdDTO();
  }
  
  public String getFeatureStrategy()
  {
    return getValue(FEATURESTRATEGY);
  }
  
  public void setFeatureStrategy(String value)
  {
    if(value == null)
    {
      setValue(FEATURESTRATEGY, "");
    }
    else
    {
      setValue(FEATURESTRATEGY, value);
    }
  }
  
  public boolean isFeatureStrategyWritable()
  {
    return isWritable(FEATURESTRATEGY);
  }
  
  public boolean isFeatureStrategyReadable()
  {
    return isReadable(FEATURESTRATEGY);
  }
  
  public boolean isFeatureStrategyModified()
  {
    return isModified(FEATURESTRATEGY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getFeatureStrategyMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(FEATURESTRATEGY).getAttributeMdDTO();
  }
  
  public Boolean getGroupedInLegend()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(GROUPEDINLEGEND));
  }
  
  public void setGroupedInLegend(Boolean value)
  {
    if(value == null)
    {
      setValue(GROUPEDINLEGEND, "");
    }
    else
    {
      setValue(GROUPEDINLEGEND, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isGroupedInLegendWritable()
  {
    return isWritable(GROUPEDINLEGEND);
  }
  
  public boolean isGroupedInLegendReadable()
  {
    return isReadable(GROUPEDINLEGEND);
  }
  
  public boolean isGroupedInLegendModified()
  {
    return isModified(GROUPEDINLEGEND);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getGroupedInLegendMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(GROUPEDINLEGEND).getAttributeMdDTO();
  }
  
  public String getLayerId()
  {
    return getValue(LAYERID);
  }
  
  public void setLayerId(String value)
  {
    if(value == null)
    {
      setValue(LAYERID, "");
    }
    else
    {
      setValue(LAYERID, value);
    }
  }
  
  public boolean isLayerIdWritable()
  {
    return isWritable(LAYERID);
  }
  
  public boolean isLayerIdReadable()
  {
    return isReadable(LAYERID);
  }
  
  public boolean isLayerIdModified()
  {
    return isModified(LAYERID);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getLayerIdMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(LAYERID).getAttributeMdDTO();
  }
  
  public Boolean getLayerIsActive()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(LAYERISACTIVE));
  }
  
  public void setLayerIsActive(Boolean value)
  {
    if(value == null)
    {
      setValue(LAYERISACTIVE, "");
    }
    else
    {
      setValue(LAYERISACTIVE, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isLayerIsActiveWritable()
  {
    return isWritable(LAYERISACTIVE);
  }
  
  public boolean isLayerIsActiveReadable()
  {
    return isReadable(LAYERISACTIVE);
  }
  
  public boolean isLayerIsActiveModified()
  {
    return isModified(LAYERISACTIVE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getLayerIsActiveMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(LAYERISACTIVE).getAttributeMdDTO();
  }
  
  public String getLayerName()
  {
    return getValue(LAYERNAME);
  }
  
  public void setLayerName(String value)
  {
    if(value == null)
    {
      setValue(LAYERNAME, "");
    }
    else
    {
      setValue(LAYERNAME, value);
    }
  }
  
  public boolean isLayerNameWritable()
  {
    return isWritable(LAYERNAME);
  }
  
  public boolean isLayerNameReadable()
  {
    return isReadable(LAYERNAME);
  }
  
  public boolean isLayerNameModified()
  {
    return isModified(LAYERNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getLayerNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(LAYERNAME).getAttributeMdDTO();
  }
  
  public Integer getLegendXPosition()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(LEGENDXPOSITION));
  }
  
  public void setLegendXPosition(Integer value)
  {
    if(value == null)
    {
      setValue(LEGENDXPOSITION, "");
    }
    else
    {
      setValue(LEGENDXPOSITION, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isLegendXPositionWritable()
  {
    return isWritable(LEGENDXPOSITION);
  }
  
  public boolean isLegendXPositionReadable()
  {
    return isReadable(LEGENDXPOSITION);
  }
  
  public boolean isLegendXPositionModified()
  {
    return isModified(LEGENDXPOSITION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getLegendXPositionMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(LEGENDXPOSITION).getAttributeMdDTO();
  }
  
  public Integer getLegendYPosition()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(LEGENDYPOSITION));
  }
  
  public void setLegendYPosition(Integer value)
  {
    if(value == null)
    {
      setValue(LEGENDYPOSITION, "");
    }
    else
    {
      setValue(LEGENDYPOSITION, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isLegendYPositionWritable()
  {
    return isWritable(LEGENDYPOSITION);
  }
  
  public boolean isLegendYPositionReadable()
  {
    return isReadable(LEGENDYPOSITION);
  }
  
  public boolean isLegendYPositionModified()
  {
    return isModified(LEGENDYPOSITION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getLegendYPositionMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(LEGENDYPOSITION).getAttributeMdDTO();
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
  
  public String getSldName()
  {
    return getValue(SLDNAME);
  }
  
  public void setSldName(String value)
  {
    if(value == null)
    {
      setValue(SLDNAME, "");
    }
    else
    {
      setValue(SLDNAME, value);
    }
  }
  
  public boolean isSldNameWritable()
  {
    return isWritable(SLDNAME);
  }
  
  public boolean isSldNameReadable()
  {
    return isReadable(SLDNAME);
  }
  
  public boolean isSldNameModified()
  {
    return isModified(SLDNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getSldNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(SLDNAME).getAttributeMdDTO();
  }
  
  public String getViewName()
  {
    return getValue(VIEWNAME);
  }
  
  public void setViewName(String value)
  {
    if(value == null)
    {
      setValue(VIEWNAME, "");
    }
    else
    {
      setValue(VIEWNAME, value);
    }
  }
  
  public boolean isViewNameWritable()
  {
    return isWritable(VIEWNAME);
  }
  
  public boolean isViewNameReadable()
  {
    return isReadable(VIEWNAME);
  }
  
  public boolean isViewNameModified()
  {
    return isModified(VIEWNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getViewNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(VIEWNAME).getAttributeMdDTO();
  }
  
  public static DashboardLayerViewDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.ViewDTO dto = (com.runwaysdk.business.ViewDTO)clientRequest.get(oid);
    
    return (DashboardLayerViewDTO) dto;
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
