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
package net.geoprism.dashboard;

@com.runwaysdk.business.ClassSignature(hash = 2115883788)
public abstract class DashboardThematicStyleDTOBase extends net.geoprism.dashboard.DashboardStyleDTO 
{
  public final static String CLASS = "net.geoprism.dashboard.DashboardThematicStyle";
  private static final long serialVersionUID = 2115883788;
  
  protected DashboardThematicStyleDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(clientRequest);
  }
  
  /**
  * Copy Constructor: Duplicates the values and attributes of the given BusinessDTO into a new DTO.
  * 
  * @param businessDTO The BusinessDTO to duplicate
  * @param clientRequest The clientRequest this DTO should use to communicate with the server.
  */
  protected DashboardThematicStyleDTOBase(com.runwaysdk.business.BusinessDTO businessDTO, com.runwaysdk.constants.ClientRequestIF clientRequest)
  {
    super(businessDTO, clientRequest);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String BUBBLECONTINUOUSSIZE = "bubbleContinuousSize";
  public static java.lang.String BUBBLEFILL = "bubbleFill";
  public static java.lang.String BUBBLEMAXSIZE = "bubbleMaxSize";
  public static java.lang.String BUBBLEMINSIZE = "bubbleMinSize";
  public static java.lang.String BUBBLEOPACITY = "bubbleOpacity";
  public static java.lang.String BUBBLEROTATION = "bubbleRotation";
  public static java.lang.String BUBBLESIZE = "bubbleSize";
  public static java.lang.String BUBBLESTROKE = "bubbleStroke";
  public static java.lang.String BUBBLESTROKEOPACITY = "bubbleStrokeOpacity";
  public static java.lang.String BUBBLESTROKEWIDTH = "bubbleStrokeWidth";
  public static java.lang.String BUBBLEWELLKNOWNNAME = "bubbleWellKnownName";
  public static java.lang.String CATEGORYPOINTFILLOPACITY = "categoryPointFillOpacity";
  public static java.lang.String CATEGORYPOINTSIZE = "categoryPointSize";
  public static java.lang.String CATEGORYPOINTSTROKE = "categoryPointStroke";
  public static java.lang.String CATEGORYPOINTSTROKEOPACITY = "categoryPointStrokeOpacity";
  public static java.lang.String CATEGORYPOINTSTROKEWIDTH = "categoryPointStrokeWidth";
  public static java.lang.String CATEGORYPOINTSTYLES = "categoryPointStyles";
  public static java.lang.String CATEGORYPOINTWELLKNOWNNAME = "categoryPointWellKnownName";
  public static java.lang.String CATEGORYPOLYGONFILLOPACITY = "categoryPolygonFillOpacity";
  public static java.lang.String CATEGORYPOLYGONSTROKE = "categoryPolygonStroke";
  public static java.lang.String CATEGORYPOLYGONSTROKEOPACITY = "categoryPolygonStrokeOpacity";
  public static java.lang.String CATEGORYPOLYGONSTROKEWIDTH = "categoryPolygonStrokeWidth";
  public static java.lang.String CATEGORYPOLYGONSTYLES = "categoryPolygonStyles";
  public static java.lang.String GRADIENTPOINTFILLOPACITY = "gradientPointFillOpacity";
  public static java.lang.String GRADIENTPOINTMAXFILL = "gradientPointMaxFill";
  public static java.lang.String GRADIENTPOINTMINFILL = "gradientPointMinFill";
  public static java.lang.String GRADIENTPOINTSIZE = "gradientPointSize";
  public static java.lang.String GRADIENTPOINTSTROKE = "gradientPointStroke";
  public static java.lang.String GRADIENTPOINTSTROKEOPACITY = "gradientPointStrokeOpacity";
  public static java.lang.String GRADIENTPOINTSTROKEWIDTH = "gradientPointStrokeWidth";
  public static java.lang.String GRADIENTPOINTWELLKNOWNNAME = "gradientPointWellKnownName";
  public static java.lang.String GRADIENTPOLYGONFILLOPACITY = "gradientPolygonFillOpacity";
  public static java.lang.String GRADIENTPOLYGONMAXFILL = "gradientPolygonMaxFill";
  public static java.lang.String GRADIENTPOLYGONMINFILL = "gradientPolygonMinFill";
  public static java.lang.String GRADIENTPOLYGONSTROKE = "gradientPolygonStroke";
  public static java.lang.String GRADIENTPOLYGONSTROKEOPACITY = "gradientPolygonStrokeOpacity";
  public static java.lang.String GRADIENTPOLYGONSTROKEWIDTH = "gradientPolygonStrokeWidth";
  public static java.lang.String NUMBUBBLESIZECATEGORIES = "numBubbleSizeCategories";
  public static java.lang.String NUMGRADIENTPOINTCATEGORIES = "numGradientPointCategories";
  public static java.lang.String NUMGRADIENTPOLYGONCATEGORIES = "numGradientPolygonCategories";
  public static java.lang.String SECONDARYAGGREGATIONTYPE = "secondaryAggregationType";
  public static java.lang.String SECONDARYATTRIBUTE = "secondaryAttribute";
  public static java.lang.String SECONDARYCATEGORIES = "secondaryCategories";
  public Boolean getBubbleContinuousSize()
  {
    return com.runwaysdk.constants.MdAttributeBooleanUtil.getTypeSafeValue(getValue(BUBBLECONTINUOUSSIZE));
  }
  
  public void setBubbleContinuousSize(Boolean value)
  {
    if(value == null)
    {
      setValue(BUBBLECONTINUOUSSIZE, "");
    }
    else
    {
      setValue(BUBBLECONTINUOUSSIZE, java.lang.Boolean.toString(value));
    }
  }
  
  public boolean isBubbleContinuousSizeWritable()
  {
    return isWritable(BUBBLECONTINUOUSSIZE);
  }
  
  public boolean isBubbleContinuousSizeReadable()
  {
    return isReadable(BUBBLECONTINUOUSSIZE);
  }
  
  public boolean isBubbleContinuousSizeModified()
  {
    return isModified(BUBBLECONTINUOUSSIZE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeBooleanMdDTO getBubbleContinuousSizeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeBooleanMdDTO) getAttributeDTO(BUBBLECONTINUOUSSIZE).getAttributeMdDTO();
  }
  
  public String getBubbleFill()
  {
    return getValue(BUBBLEFILL);
  }
  
  public void setBubbleFill(String value)
  {
    if(value == null)
    {
      setValue(BUBBLEFILL, "");
    }
    else
    {
      setValue(BUBBLEFILL, value);
    }
  }
  
  public boolean isBubbleFillWritable()
  {
    return isWritable(BUBBLEFILL);
  }
  
  public boolean isBubbleFillReadable()
  {
    return isReadable(BUBBLEFILL);
  }
  
  public boolean isBubbleFillModified()
  {
    return isModified(BUBBLEFILL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getBubbleFillMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(BUBBLEFILL).getAttributeMdDTO();
  }
  
  public Integer getBubbleMaxSize()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(BUBBLEMAXSIZE));
  }
  
  public void setBubbleMaxSize(Integer value)
  {
    if(value == null)
    {
      setValue(BUBBLEMAXSIZE, "");
    }
    else
    {
      setValue(BUBBLEMAXSIZE, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isBubbleMaxSizeWritable()
  {
    return isWritable(BUBBLEMAXSIZE);
  }
  
  public boolean isBubbleMaxSizeReadable()
  {
    return isReadable(BUBBLEMAXSIZE);
  }
  
  public boolean isBubbleMaxSizeModified()
  {
    return isModified(BUBBLEMAXSIZE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getBubbleMaxSizeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(BUBBLEMAXSIZE).getAttributeMdDTO();
  }
  
  public Integer getBubbleMinSize()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(BUBBLEMINSIZE));
  }
  
  public void setBubbleMinSize(Integer value)
  {
    if(value == null)
    {
      setValue(BUBBLEMINSIZE, "");
    }
    else
    {
      setValue(BUBBLEMINSIZE, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isBubbleMinSizeWritable()
  {
    return isWritable(BUBBLEMINSIZE);
  }
  
  public boolean isBubbleMinSizeReadable()
  {
    return isReadable(BUBBLEMINSIZE);
  }
  
  public boolean isBubbleMinSizeModified()
  {
    return isModified(BUBBLEMINSIZE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getBubbleMinSizeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(BUBBLEMINSIZE).getAttributeMdDTO();
  }
  
  public Double getBubbleOpacity()
  {
    return com.runwaysdk.constants.MdAttributeDoubleUtil.getTypeSafeValue(getValue(BUBBLEOPACITY));
  }
  
  public void setBubbleOpacity(Double value)
  {
    if(value == null)
    {
      setValue(BUBBLEOPACITY, "");
    }
    else
    {
      setValue(BUBBLEOPACITY, java.lang.Double.toString(value));
    }
  }
  
  public boolean isBubbleOpacityWritable()
  {
    return isWritable(BUBBLEOPACITY);
  }
  
  public boolean isBubbleOpacityReadable()
  {
    return isReadable(BUBBLEOPACITY);
  }
  
  public boolean isBubbleOpacityModified()
  {
    return isModified(BUBBLEOPACITY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDecMdDTO getBubbleOpacityMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDecMdDTO) getAttributeDTO(BUBBLEOPACITY).getAttributeMdDTO();
  }
  
  public Integer getBubbleRotation()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(BUBBLEROTATION));
  }
  
  public void setBubbleRotation(Integer value)
  {
    if(value == null)
    {
      setValue(BUBBLEROTATION, "");
    }
    else
    {
      setValue(BUBBLEROTATION, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isBubbleRotationWritable()
  {
    return isWritable(BUBBLEROTATION);
  }
  
  public boolean isBubbleRotationReadable()
  {
    return isReadable(BUBBLEROTATION);
  }
  
  public boolean isBubbleRotationModified()
  {
    return isModified(BUBBLEROTATION);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getBubbleRotationMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(BUBBLEROTATION).getAttributeMdDTO();
  }
  
  public Integer getBubbleSize()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(BUBBLESIZE));
  }
  
  public void setBubbleSize(Integer value)
  {
    if(value == null)
    {
      setValue(BUBBLESIZE, "");
    }
    else
    {
      setValue(BUBBLESIZE, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isBubbleSizeWritable()
  {
    return isWritable(BUBBLESIZE);
  }
  
  public boolean isBubbleSizeReadable()
  {
    return isReadable(BUBBLESIZE);
  }
  
  public boolean isBubbleSizeModified()
  {
    return isModified(BUBBLESIZE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getBubbleSizeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(BUBBLESIZE).getAttributeMdDTO();
  }
  
  public String getBubbleStroke()
  {
    return getValue(BUBBLESTROKE);
  }
  
  public void setBubbleStroke(String value)
  {
    if(value == null)
    {
      setValue(BUBBLESTROKE, "");
    }
    else
    {
      setValue(BUBBLESTROKE, value);
    }
  }
  
  public boolean isBubbleStrokeWritable()
  {
    return isWritable(BUBBLESTROKE);
  }
  
  public boolean isBubbleStrokeReadable()
  {
    return isReadable(BUBBLESTROKE);
  }
  
  public boolean isBubbleStrokeModified()
  {
    return isModified(BUBBLESTROKE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getBubbleStrokeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(BUBBLESTROKE).getAttributeMdDTO();
  }
  
  public Double getBubbleStrokeOpacity()
  {
    return com.runwaysdk.constants.MdAttributeDoubleUtil.getTypeSafeValue(getValue(BUBBLESTROKEOPACITY));
  }
  
  public void setBubbleStrokeOpacity(Double value)
  {
    if(value == null)
    {
      setValue(BUBBLESTROKEOPACITY, "");
    }
    else
    {
      setValue(BUBBLESTROKEOPACITY, java.lang.Double.toString(value));
    }
  }
  
  public boolean isBubbleStrokeOpacityWritable()
  {
    return isWritable(BUBBLESTROKEOPACITY);
  }
  
  public boolean isBubbleStrokeOpacityReadable()
  {
    return isReadable(BUBBLESTROKEOPACITY);
  }
  
  public boolean isBubbleStrokeOpacityModified()
  {
    return isModified(BUBBLESTROKEOPACITY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDecMdDTO getBubbleStrokeOpacityMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDecMdDTO) getAttributeDTO(BUBBLESTROKEOPACITY).getAttributeMdDTO();
  }
  
  public Integer getBubbleStrokeWidth()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(BUBBLESTROKEWIDTH));
  }
  
  public void setBubbleStrokeWidth(Integer value)
  {
    if(value == null)
    {
      setValue(BUBBLESTROKEWIDTH, "");
    }
    else
    {
      setValue(BUBBLESTROKEWIDTH, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isBubbleStrokeWidthWritable()
  {
    return isWritable(BUBBLESTROKEWIDTH);
  }
  
  public boolean isBubbleStrokeWidthReadable()
  {
    return isReadable(BUBBLESTROKEWIDTH);
  }
  
  public boolean isBubbleStrokeWidthModified()
  {
    return isModified(BUBBLESTROKEWIDTH);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getBubbleStrokeWidthMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(BUBBLESTROKEWIDTH).getAttributeMdDTO();
  }
  
  public String getBubbleWellKnownName()
  {
    return getValue(BUBBLEWELLKNOWNNAME);
  }
  
  public void setBubbleWellKnownName(String value)
  {
    if(value == null)
    {
      setValue(BUBBLEWELLKNOWNNAME, "");
    }
    else
    {
      setValue(BUBBLEWELLKNOWNNAME, value);
    }
  }
  
  public boolean isBubbleWellKnownNameWritable()
  {
    return isWritable(BUBBLEWELLKNOWNNAME);
  }
  
  public boolean isBubbleWellKnownNameReadable()
  {
    return isReadable(BUBBLEWELLKNOWNNAME);
  }
  
  public boolean isBubbleWellKnownNameModified()
  {
    return isModified(BUBBLEWELLKNOWNNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getBubbleWellKnownNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(BUBBLEWELLKNOWNNAME).getAttributeMdDTO();
  }
  
  public Double getCategoryPointFillOpacity()
  {
    return com.runwaysdk.constants.MdAttributeDoubleUtil.getTypeSafeValue(getValue(CATEGORYPOINTFILLOPACITY));
  }
  
  public void setCategoryPointFillOpacity(Double value)
  {
    if(value == null)
    {
      setValue(CATEGORYPOINTFILLOPACITY, "");
    }
    else
    {
      setValue(CATEGORYPOINTFILLOPACITY, java.lang.Double.toString(value));
    }
  }
  
  public boolean isCategoryPointFillOpacityWritable()
  {
    return isWritable(CATEGORYPOINTFILLOPACITY);
  }
  
  public boolean isCategoryPointFillOpacityReadable()
  {
    return isReadable(CATEGORYPOINTFILLOPACITY);
  }
  
  public boolean isCategoryPointFillOpacityModified()
  {
    return isModified(CATEGORYPOINTFILLOPACITY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDecMdDTO getCategoryPointFillOpacityMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDecMdDTO) getAttributeDTO(CATEGORYPOINTFILLOPACITY).getAttributeMdDTO();
  }
  
  public Integer getCategoryPointSize()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(CATEGORYPOINTSIZE));
  }
  
  public void setCategoryPointSize(Integer value)
  {
    if(value == null)
    {
      setValue(CATEGORYPOINTSIZE, "");
    }
    else
    {
      setValue(CATEGORYPOINTSIZE, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isCategoryPointSizeWritable()
  {
    return isWritable(CATEGORYPOINTSIZE);
  }
  
  public boolean isCategoryPointSizeReadable()
  {
    return isReadable(CATEGORYPOINTSIZE);
  }
  
  public boolean isCategoryPointSizeModified()
  {
    return isModified(CATEGORYPOINTSIZE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getCategoryPointSizeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(CATEGORYPOINTSIZE).getAttributeMdDTO();
  }
  
  public String getCategoryPointStroke()
  {
    return getValue(CATEGORYPOINTSTROKE);
  }
  
  public void setCategoryPointStroke(String value)
  {
    if(value == null)
    {
      setValue(CATEGORYPOINTSTROKE, "");
    }
    else
    {
      setValue(CATEGORYPOINTSTROKE, value);
    }
  }
  
  public boolean isCategoryPointStrokeWritable()
  {
    return isWritable(CATEGORYPOINTSTROKE);
  }
  
  public boolean isCategoryPointStrokeReadable()
  {
    return isReadable(CATEGORYPOINTSTROKE);
  }
  
  public boolean isCategoryPointStrokeModified()
  {
    return isModified(CATEGORYPOINTSTROKE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getCategoryPointStrokeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(CATEGORYPOINTSTROKE).getAttributeMdDTO();
  }
  
  public Double getCategoryPointStrokeOpacity()
  {
    return com.runwaysdk.constants.MdAttributeDoubleUtil.getTypeSafeValue(getValue(CATEGORYPOINTSTROKEOPACITY));
  }
  
  public void setCategoryPointStrokeOpacity(Double value)
  {
    if(value == null)
    {
      setValue(CATEGORYPOINTSTROKEOPACITY, "");
    }
    else
    {
      setValue(CATEGORYPOINTSTROKEOPACITY, java.lang.Double.toString(value));
    }
  }
  
  public boolean isCategoryPointStrokeOpacityWritable()
  {
    return isWritable(CATEGORYPOINTSTROKEOPACITY);
  }
  
  public boolean isCategoryPointStrokeOpacityReadable()
  {
    return isReadable(CATEGORYPOINTSTROKEOPACITY);
  }
  
  public boolean isCategoryPointStrokeOpacityModified()
  {
    return isModified(CATEGORYPOINTSTROKEOPACITY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDecMdDTO getCategoryPointStrokeOpacityMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDecMdDTO) getAttributeDTO(CATEGORYPOINTSTROKEOPACITY).getAttributeMdDTO();
  }
  
  public Integer getCategoryPointStrokeWidth()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(CATEGORYPOINTSTROKEWIDTH));
  }
  
  public void setCategoryPointStrokeWidth(Integer value)
  {
    if(value == null)
    {
      setValue(CATEGORYPOINTSTROKEWIDTH, "");
    }
    else
    {
      setValue(CATEGORYPOINTSTROKEWIDTH, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isCategoryPointStrokeWidthWritable()
  {
    return isWritable(CATEGORYPOINTSTROKEWIDTH);
  }
  
  public boolean isCategoryPointStrokeWidthReadable()
  {
    return isReadable(CATEGORYPOINTSTROKEWIDTH);
  }
  
  public boolean isCategoryPointStrokeWidthModified()
  {
    return isModified(CATEGORYPOINTSTROKEWIDTH);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getCategoryPointStrokeWidthMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(CATEGORYPOINTSTROKEWIDTH).getAttributeMdDTO();
  }
  
  public String getCategoryPointStyles()
  {
    return getValue(CATEGORYPOINTSTYLES);
  }
  
  public void setCategoryPointStyles(String value)
  {
    if(value == null)
    {
      setValue(CATEGORYPOINTSTYLES, "");
    }
    else
    {
      setValue(CATEGORYPOINTSTYLES, value);
    }
  }
  
  public boolean isCategoryPointStylesWritable()
  {
    return isWritable(CATEGORYPOINTSTYLES);
  }
  
  public boolean isCategoryPointStylesReadable()
  {
    return isReadable(CATEGORYPOINTSTYLES);
  }
  
  public boolean isCategoryPointStylesModified()
  {
    return isModified(CATEGORYPOINTSTYLES);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getCategoryPointStylesMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(CATEGORYPOINTSTYLES).getAttributeMdDTO();
  }
  
  public String getCategoryPointWellKnownName()
  {
    return getValue(CATEGORYPOINTWELLKNOWNNAME);
  }
  
  public void setCategoryPointWellKnownName(String value)
  {
    if(value == null)
    {
      setValue(CATEGORYPOINTWELLKNOWNNAME, "");
    }
    else
    {
      setValue(CATEGORYPOINTWELLKNOWNNAME, value);
    }
  }
  
  public boolean isCategoryPointWellKnownNameWritable()
  {
    return isWritable(CATEGORYPOINTWELLKNOWNNAME);
  }
  
  public boolean isCategoryPointWellKnownNameReadable()
  {
    return isReadable(CATEGORYPOINTWELLKNOWNNAME);
  }
  
  public boolean isCategoryPointWellKnownNameModified()
  {
    return isModified(CATEGORYPOINTWELLKNOWNNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getCategoryPointWellKnownNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(CATEGORYPOINTWELLKNOWNNAME).getAttributeMdDTO();
  }
  
  public Double getCategoryPolygonFillOpacity()
  {
    return com.runwaysdk.constants.MdAttributeDoubleUtil.getTypeSafeValue(getValue(CATEGORYPOLYGONFILLOPACITY));
  }
  
  public void setCategoryPolygonFillOpacity(Double value)
  {
    if(value == null)
    {
      setValue(CATEGORYPOLYGONFILLOPACITY, "");
    }
    else
    {
      setValue(CATEGORYPOLYGONFILLOPACITY, java.lang.Double.toString(value));
    }
  }
  
  public boolean isCategoryPolygonFillOpacityWritable()
  {
    return isWritable(CATEGORYPOLYGONFILLOPACITY);
  }
  
  public boolean isCategoryPolygonFillOpacityReadable()
  {
    return isReadable(CATEGORYPOLYGONFILLOPACITY);
  }
  
  public boolean isCategoryPolygonFillOpacityModified()
  {
    return isModified(CATEGORYPOLYGONFILLOPACITY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDecMdDTO getCategoryPolygonFillOpacityMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDecMdDTO) getAttributeDTO(CATEGORYPOLYGONFILLOPACITY).getAttributeMdDTO();
  }
  
  public String getCategoryPolygonStroke()
  {
    return getValue(CATEGORYPOLYGONSTROKE);
  }
  
  public void setCategoryPolygonStroke(String value)
  {
    if(value == null)
    {
      setValue(CATEGORYPOLYGONSTROKE, "");
    }
    else
    {
      setValue(CATEGORYPOLYGONSTROKE, value);
    }
  }
  
  public boolean isCategoryPolygonStrokeWritable()
  {
    return isWritable(CATEGORYPOLYGONSTROKE);
  }
  
  public boolean isCategoryPolygonStrokeReadable()
  {
    return isReadable(CATEGORYPOLYGONSTROKE);
  }
  
  public boolean isCategoryPolygonStrokeModified()
  {
    return isModified(CATEGORYPOLYGONSTROKE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getCategoryPolygonStrokeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(CATEGORYPOLYGONSTROKE).getAttributeMdDTO();
  }
  
  public Double getCategoryPolygonStrokeOpacity()
  {
    return com.runwaysdk.constants.MdAttributeDoubleUtil.getTypeSafeValue(getValue(CATEGORYPOLYGONSTROKEOPACITY));
  }
  
  public void setCategoryPolygonStrokeOpacity(Double value)
  {
    if(value == null)
    {
      setValue(CATEGORYPOLYGONSTROKEOPACITY, "");
    }
    else
    {
      setValue(CATEGORYPOLYGONSTROKEOPACITY, java.lang.Double.toString(value));
    }
  }
  
  public boolean isCategoryPolygonStrokeOpacityWritable()
  {
    return isWritable(CATEGORYPOLYGONSTROKEOPACITY);
  }
  
  public boolean isCategoryPolygonStrokeOpacityReadable()
  {
    return isReadable(CATEGORYPOLYGONSTROKEOPACITY);
  }
  
  public boolean isCategoryPolygonStrokeOpacityModified()
  {
    return isModified(CATEGORYPOLYGONSTROKEOPACITY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDecMdDTO getCategoryPolygonStrokeOpacityMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDecMdDTO) getAttributeDTO(CATEGORYPOLYGONSTROKEOPACITY).getAttributeMdDTO();
  }
  
  public Integer getCategoryPolygonStrokeWidth()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(CATEGORYPOLYGONSTROKEWIDTH));
  }
  
  public void setCategoryPolygonStrokeWidth(Integer value)
  {
    if(value == null)
    {
      setValue(CATEGORYPOLYGONSTROKEWIDTH, "");
    }
    else
    {
      setValue(CATEGORYPOLYGONSTROKEWIDTH, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isCategoryPolygonStrokeWidthWritable()
  {
    return isWritable(CATEGORYPOLYGONSTROKEWIDTH);
  }
  
  public boolean isCategoryPolygonStrokeWidthReadable()
  {
    return isReadable(CATEGORYPOLYGONSTROKEWIDTH);
  }
  
  public boolean isCategoryPolygonStrokeWidthModified()
  {
    return isModified(CATEGORYPOLYGONSTROKEWIDTH);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getCategoryPolygonStrokeWidthMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(CATEGORYPOLYGONSTROKEWIDTH).getAttributeMdDTO();
  }
  
  public String getCategoryPolygonStyles()
  {
    return getValue(CATEGORYPOLYGONSTYLES);
  }
  
  public void setCategoryPolygonStyles(String value)
  {
    if(value == null)
    {
      setValue(CATEGORYPOLYGONSTYLES, "");
    }
    else
    {
      setValue(CATEGORYPOLYGONSTYLES, value);
    }
  }
  
  public boolean isCategoryPolygonStylesWritable()
  {
    return isWritable(CATEGORYPOLYGONSTYLES);
  }
  
  public boolean isCategoryPolygonStylesReadable()
  {
    return isReadable(CATEGORYPOLYGONSTYLES);
  }
  
  public boolean isCategoryPolygonStylesModified()
  {
    return isModified(CATEGORYPOLYGONSTYLES);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getCategoryPolygonStylesMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(CATEGORYPOLYGONSTYLES).getAttributeMdDTO();
  }
  
  public Double getGradientPointFillOpacity()
  {
    return com.runwaysdk.constants.MdAttributeDoubleUtil.getTypeSafeValue(getValue(GRADIENTPOINTFILLOPACITY));
  }
  
  public void setGradientPointFillOpacity(Double value)
  {
    if(value == null)
    {
      setValue(GRADIENTPOINTFILLOPACITY, "");
    }
    else
    {
      setValue(GRADIENTPOINTFILLOPACITY, java.lang.Double.toString(value));
    }
  }
  
  public boolean isGradientPointFillOpacityWritable()
  {
    return isWritable(GRADIENTPOINTFILLOPACITY);
  }
  
  public boolean isGradientPointFillOpacityReadable()
  {
    return isReadable(GRADIENTPOINTFILLOPACITY);
  }
  
  public boolean isGradientPointFillOpacityModified()
  {
    return isModified(GRADIENTPOINTFILLOPACITY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDecMdDTO getGradientPointFillOpacityMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDecMdDTO) getAttributeDTO(GRADIENTPOINTFILLOPACITY).getAttributeMdDTO();
  }
  
  public String getGradientPointMaxFill()
  {
    return getValue(GRADIENTPOINTMAXFILL);
  }
  
  public void setGradientPointMaxFill(String value)
  {
    if(value == null)
    {
      setValue(GRADIENTPOINTMAXFILL, "");
    }
    else
    {
      setValue(GRADIENTPOINTMAXFILL, value);
    }
  }
  
  public boolean isGradientPointMaxFillWritable()
  {
    return isWritable(GRADIENTPOINTMAXFILL);
  }
  
  public boolean isGradientPointMaxFillReadable()
  {
    return isReadable(GRADIENTPOINTMAXFILL);
  }
  
  public boolean isGradientPointMaxFillModified()
  {
    return isModified(GRADIENTPOINTMAXFILL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getGradientPointMaxFillMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(GRADIENTPOINTMAXFILL).getAttributeMdDTO();
  }
  
  public String getGradientPointMinFill()
  {
    return getValue(GRADIENTPOINTMINFILL);
  }
  
  public void setGradientPointMinFill(String value)
  {
    if(value == null)
    {
      setValue(GRADIENTPOINTMINFILL, "");
    }
    else
    {
      setValue(GRADIENTPOINTMINFILL, value);
    }
  }
  
  public boolean isGradientPointMinFillWritable()
  {
    return isWritable(GRADIENTPOINTMINFILL);
  }
  
  public boolean isGradientPointMinFillReadable()
  {
    return isReadable(GRADIENTPOINTMINFILL);
  }
  
  public boolean isGradientPointMinFillModified()
  {
    return isModified(GRADIENTPOINTMINFILL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getGradientPointMinFillMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(GRADIENTPOINTMINFILL).getAttributeMdDTO();
  }
  
  public Integer getGradientPointSize()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(GRADIENTPOINTSIZE));
  }
  
  public void setGradientPointSize(Integer value)
  {
    if(value == null)
    {
      setValue(GRADIENTPOINTSIZE, "");
    }
    else
    {
      setValue(GRADIENTPOINTSIZE, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isGradientPointSizeWritable()
  {
    return isWritable(GRADIENTPOINTSIZE);
  }
  
  public boolean isGradientPointSizeReadable()
  {
    return isReadable(GRADIENTPOINTSIZE);
  }
  
  public boolean isGradientPointSizeModified()
  {
    return isModified(GRADIENTPOINTSIZE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getGradientPointSizeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(GRADIENTPOINTSIZE).getAttributeMdDTO();
  }
  
  public String getGradientPointStroke()
  {
    return getValue(GRADIENTPOINTSTROKE);
  }
  
  public void setGradientPointStroke(String value)
  {
    if(value == null)
    {
      setValue(GRADIENTPOINTSTROKE, "");
    }
    else
    {
      setValue(GRADIENTPOINTSTROKE, value);
    }
  }
  
  public boolean isGradientPointStrokeWritable()
  {
    return isWritable(GRADIENTPOINTSTROKE);
  }
  
  public boolean isGradientPointStrokeReadable()
  {
    return isReadable(GRADIENTPOINTSTROKE);
  }
  
  public boolean isGradientPointStrokeModified()
  {
    return isModified(GRADIENTPOINTSTROKE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getGradientPointStrokeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(GRADIENTPOINTSTROKE).getAttributeMdDTO();
  }
  
  public Double getGradientPointStrokeOpacity()
  {
    return com.runwaysdk.constants.MdAttributeDoubleUtil.getTypeSafeValue(getValue(GRADIENTPOINTSTROKEOPACITY));
  }
  
  public void setGradientPointStrokeOpacity(Double value)
  {
    if(value == null)
    {
      setValue(GRADIENTPOINTSTROKEOPACITY, "");
    }
    else
    {
      setValue(GRADIENTPOINTSTROKEOPACITY, java.lang.Double.toString(value));
    }
  }
  
  public boolean isGradientPointStrokeOpacityWritable()
  {
    return isWritable(GRADIENTPOINTSTROKEOPACITY);
  }
  
  public boolean isGradientPointStrokeOpacityReadable()
  {
    return isReadable(GRADIENTPOINTSTROKEOPACITY);
  }
  
  public boolean isGradientPointStrokeOpacityModified()
  {
    return isModified(GRADIENTPOINTSTROKEOPACITY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDecMdDTO getGradientPointStrokeOpacityMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDecMdDTO) getAttributeDTO(GRADIENTPOINTSTROKEOPACITY).getAttributeMdDTO();
  }
  
  public Integer getGradientPointStrokeWidth()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(GRADIENTPOINTSTROKEWIDTH));
  }
  
  public void setGradientPointStrokeWidth(Integer value)
  {
    if(value == null)
    {
      setValue(GRADIENTPOINTSTROKEWIDTH, "");
    }
    else
    {
      setValue(GRADIENTPOINTSTROKEWIDTH, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isGradientPointStrokeWidthWritable()
  {
    return isWritable(GRADIENTPOINTSTROKEWIDTH);
  }
  
  public boolean isGradientPointStrokeWidthReadable()
  {
    return isReadable(GRADIENTPOINTSTROKEWIDTH);
  }
  
  public boolean isGradientPointStrokeWidthModified()
  {
    return isModified(GRADIENTPOINTSTROKEWIDTH);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getGradientPointStrokeWidthMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(GRADIENTPOINTSTROKEWIDTH).getAttributeMdDTO();
  }
  
  public String getGradientPointWellKnownName()
  {
    return getValue(GRADIENTPOINTWELLKNOWNNAME);
  }
  
  public void setGradientPointWellKnownName(String value)
  {
    if(value == null)
    {
      setValue(GRADIENTPOINTWELLKNOWNNAME, "");
    }
    else
    {
      setValue(GRADIENTPOINTWELLKNOWNNAME, value);
    }
  }
  
  public boolean isGradientPointWellKnownNameWritable()
  {
    return isWritable(GRADIENTPOINTWELLKNOWNNAME);
  }
  
  public boolean isGradientPointWellKnownNameReadable()
  {
    return isReadable(GRADIENTPOINTWELLKNOWNNAME);
  }
  
  public boolean isGradientPointWellKnownNameModified()
  {
    return isModified(GRADIENTPOINTWELLKNOWNNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getGradientPointWellKnownNameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(GRADIENTPOINTWELLKNOWNNAME).getAttributeMdDTO();
  }
  
  public Double getGradientPolygonFillOpacity()
  {
    return com.runwaysdk.constants.MdAttributeDoubleUtil.getTypeSafeValue(getValue(GRADIENTPOLYGONFILLOPACITY));
  }
  
  public void setGradientPolygonFillOpacity(Double value)
  {
    if(value == null)
    {
      setValue(GRADIENTPOLYGONFILLOPACITY, "");
    }
    else
    {
      setValue(GRADIENTPOLYGONFILLOPACITY, java.lang.Double.toString(value));
    }
  }
  
  public boolean isGradientPolygonFillOpacityWritable()
  {
    return isWritable(GRADIENTPOLYGONFILLOPACITY);
  }
  
  public boolean isGradientPolygonFillOpacityReadable()
  {
    return isReadable(GRADIENTPOLYGONFILLOPACITY);
  }
  
  public boolean isGradientPolygonFillOpacityModified()
  {
    return isModified(GRADIENTPOLYGONFILLOPACITY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDecMdDTO getGradientPolygonFillOpacityMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDecMdDTO) getAttributeDTO(GRADIENTPOLYGONFILLOPACITY).getAttributeMdDTO();
  }
  
  public String getGradientPolygonMaxFill()
  {
    return getValue(GRADIENTPOLYGONMAXFILL);
  }
  
  public void setGradientPolygonMaxFill(String value)
  {
    if(value == null)
    {
      setValue(GRADIENTPOLYGONMAXFILL, "");
    }
    else
    {
      setValue(GRADIENTPOLYGONMAXFILL, value);
    }
  }
  
  public boolean isGradientPolygonMaxFillWritable()
  {
    return isWritable(GRADIENTPOLYGONMAXFILL);
  }
  
  public boolean isGradientPolygonMaxFillReadable()
  {
    return isReadable(GRADIENTPOLYGONMAXFILL);
  }
  
  public boolean isGradientPolygonMaxFillModified()
  {
    return isModified(GRADIENTPOLYGONMAXFILL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getGradientPolygonMaxFillMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(GRADIENTPOLYGONMAXFILL).getAttributeMdDTO();
  }
  
  public String getGradientPolygonMinFill()
  {
    return getValue(GRADIENTPOLYGONMINFILL);
  }
  
  public void setGradientPolygonMinFill(String value)
  {
    if(value == null)
    {
      setValue(GRADIENTPOLYGONMINFILL, "");
    }
    else
    {
      setValue(GRADIENTPOLYGONMINFILL, value);
    }
  }
  
  public boolean isGradientPolygonMinFillWritable()
  {
    return isWritable(GRADIENTPOLYGONMINFILL);
  }
  
  public boolean isGradientPolygonMinFillReadable()
  {
    return isReadable(GRADIENTPOLYGONMINFILL);
  }
  
  public boolean isGradientPolygonMinFillModified()
  {
    return isModified(GRADIENTPOLYGONMINFILL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getGradientPolygonMinFillMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(GRADIENTPOLYGONMINFILL).getAttributeMdDTO();
  }
  
  public String getGradientPolygonStroke()
  {
    return getValue(GRADIENTPOLYGONSTROKE);
  }
  
  public void setGradientPolygonStroke(String value)
  {
    if(value == null)
    {
      setValue(GRADIENTPOLYGONSTROKE, "");
    }
    else
    {
      setValue(GRADIENTPOLYGONSTROKE, value);
    }
  }
  
  public boolean isGradientPolygonStrokeWritable()
  {
    return isWritable(GRADIENTPOLYGONSTROKE);
  }
  
  public boolean isGradientPolygonStrokeReadable()
  {
    return isReadable(GRADIENTPOLYGONSTROKE);
  }
  
  public boolean isGradientPolygonStrokeModified()
  {
    return isModified(GRADIENTPOLYGONSTROKE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeCharacterMdDTO getGradientPolygonStrokeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeCharacterMdDTO) getAttributeDTO(GRADIENTPOLYGONSTROKE).getAttributeMdDTO();
  }
  
  public Double getGradientPolygonStrokeOpacity()
  {
    return com.runwaysdk.constants.MdAttributeDoubleUtil.getTypeSafeValue(getValue(GRADIENTPOLYGONSTROKEOPACITY));
  }
  
  public void setGradientPolygonStrokeOpacity(Double value)
  {
    if(value == null)
    {
      setValue(GRADIENTPOLYGONSTROKEOPACITY, "");
    }
    else
    {
      setValue(GRADIENTPOLYGONSTROKEOPACITY, java.lang.Double.toString(value));
    }
  }
  
  public boolean isGradientPolygonStrokeOpacityWritable()
  {
    return isWritable(GRADIENTPOLYGONSTROKEOPACITY);
  }
  
  public boolean isGradientPolygonStrokeOpacityReadable()
  {
    return isReadable(GRADIENTPOLYGONSTROKEOPACITY);
  }
  
  public boolean isGradientPolygonStrokeOpacityModified()
  {
    return isModified(GRADIENTPOLYGONSTROKEOPACITY);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeDecMdDTO getGradientPolygonStrokeOpacityMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeDecMdDTO) getAttributeDTO(GRADIENTPOLYGONSTROKEOPACITY).getAttributeMdDTO();
  }
  
  public Integer getGradientPolygonStrokeWidth()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(GRADIENTPOLYGONSTROKEWIDTH));
  }
  
  public void setGradientPolygonStrokeWidth(Integer value)
  {
    if(value == null)
    {
      setValue(GRADIENTPOLYGONSTROKEWIDTH, "");
    }
    else
    {
      setValue(GRADIENTPOLYGONSTROKEWIDTH, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isGradientPolygonStrokeWidthWritable()
  {
    return isWritable(GRADIENTPOLYGONSTROKEWIDTH);
  }
  
  public boolean isGradientPolygonStrokeWidthReadable()
  {
    return isReadable(GRADIENTPOLYGONSTROKEWIDTH);
  }
  
  public boolean isGradientPolygonStrokeWidthModified()
  {
    return isModified(GRADIENTPOLYGONSTROKEWIDTH);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getGradientPolygonStrokeWidthMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(GRADIENTPOLYGONSTROKEWIDTH).getAttributeMdDTO();
  }
  
  public Integer getNumBubbleSizeCategories()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(NUMBUBBLESIZECATEGORIES));
  }
  
  public void setNumBubbleSizeCategories(Integer value)
  {
    if(value == null)
    {
      setValue(NUMBUBBLESIZECATEGORIES, "");
    }
    else
    {
      setValue(NUMBUBBLESIZECATEGORIES, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isNumBubbleSizeCategoriesWritable()
  {
    return isWritable(NUMBUBBLESIZECATEGORIES);
  }
  
  public boolean isNumBubbleSizeCategoriesReadable()
  {
    return isReadable(NUMBUBBLESIZECATEGORIES);
  }
  
  public boolean isNumBubbleSizeCategoriesModified()
  {
    return isModified(NUMBUBBLESIZECATEGORIES);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getNumBubbleSizeCategoriesMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(NUMBUBBLESIZECATEGORIES).getAttributeMdDTO();
  }
  
  public Integer getNumGradientPointCategories()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(NUMGRADIENTPOINTCATEGORIES));
  }
  
  public void setNumGradientPointCategories(Integer value)
  {
    if(value == null)
    {
      setValue(NUMGRADIENTPOINTCATEGORIES, "");
    }
    else
    {
      setValue(NUMGRADIENTPOINTCATEGORIES, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isNumGradientPointCategoriesWritable()
  {
    return isWritable(NUMGRADIENTPOINTCATEGORIES);
  }
  
  public boolean isNumGradientPointCategoriesReadable()
  {
    return isReadable(NUMGRADIENTPOINTCATEGORIES);
  }
  
  public boolean isNumGradientPointCategoriesModified()
  {
    return isModified(NUMGRADIENTPOINTCATEGORIES);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getNumGradientPointCategoriesMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(NUMGRADIENTPOINTCATEGORIES).getAttributeMdDTO();
  }
  
  public Integer getNumGradientPolygonCategories()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(NUMGRADIENTPOLYGONCATEGORIES));
  }
  
  public void setNumGradientPolygonCategories(Integer value)
  {
    if(value == null)
    {
      setValue(NUMGRADIENTPOLYGONCATEGORIES, "");
    }
    else
    {
      setValue(NUMGRADIENTPOLYGONCATEGORIES, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isNumGradientPolygonCategoriesWritable()
  {
    return isWritable(NUMGRADIENTPOLYGONCATEGORIES);
  }
  
  public boolean isNumGradientPolygonCategoriesReadable()
  {
    return isReadable(NUMGRADIENTPOLYGONCATEGORIES);
  }
  
  public boolean isNumGradientPolygonCategoriesModified()
  {
    return isModified(NUMGRADIENTPOLYGONCATEGORIES);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getNumGradientPolygonCategoriesMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(NUMGRADIENTPOLYGONCATEGORIES).getAttributeMdDTO();
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<net.geoprism.dashboard.AllAggregationTypeDTO> getSecondaryAggregationType()
  {
    return (java.util.List<net.geoprism.dashboard.AllAggregationTypeDTO>) com.runwaysdk.transport.conversion.ConversionFacade.convertEnumDTOsFromEnumNames(getRequest(), net.geoprism.dashboard.AllAggregationTypeDTO.CLASS, getEnumNames(SECONDARYAGGREGATIONTYPE));
  }
  
  public java.util.List<String> getSecondaryAggregationTypeEnumNames()
  {
    return getEnumNames(SECONDARYAGGREGATIONTYPE);
  }
  
  public void addSecondaryAggregationType(net.geoprism.dashboard.AllAggregationTypeDTO enumDTO)
  {
    addEnumItem(SECONDARYAGGREGATIONTYPE, enumDTO.toString());
  }
  
  public void removeSecondaryAggregationType(net.geoprism.dashboard.AllAggregationTypeDTO enumDTO)
  {
    removeEnumItem(SECONDARYAGGREGATIONTYPE, enumDTO.toString());
  }
  
  public void clearSecondaryAggregationType()
  {
    clearEnum(SECONDARYAGGREGATIONTYPE);
  }
  
  public boolean isSecondaryAggregationTypeWritable()
  {
    return isWritable(SECONDARYAGGREGATIONTYPE);
  }
  
  public boolean isSecondaryAggregationTypeReadable()
  {
    return isReadable(SECONDARYAGGREGATIONTYPE);
  }
  
  public boolean isSecondaryAggregationTypeModified()
  {
    return isModified(SECONDARYAGGREGATIONTYPE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeEnumerationMdDTO getSecondaryAggregationTypeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeEnumerationMdDTO) getAttributeDTO(SECONDARYAGGREGATIONTYPE).getAttributeMdDTO();
  }
  
  public com.runwaysdk.system.metadata.MdAttributeDTO getSecondaryAttribute()
  {
    if(getValue(SECONDARYATTRIBUTE) == null || getValue(SECONDARYATTRIBUTE).trim().equals(""))
    {
      return null;
    }
    else
    {
      return com.runwaysdk.system.metadata.MdAttributeDTO.get(getRequest(), getValue(SECONDARYATTRIBUTE));
    }
  }
  
  public String getSecondaryAttributeId()
  {
    return getValue(SECONDARYATTRIBUTE);
  }
  
  public void setSecondaryAttribute(com.runwaysdk.system.metadata.MdAttributeDTO value)
  {
    if(value == null)
    {
      setValue(SECONDARYATTRIBUTE, "");
    }
    else
    {
      setValue(SECONDARYATTRIBUTE, value.getOid());
    }
  }
  
  public boolean isSecondaryAttributeWritable()
  {
    return isWritable(SECONDARYATTRIBUTE);
  }
  
  public boolean isSecondaryAttributeReadable()
  {
    return isReadable(SECONDARYATTRIBUTE);
  }
  
  public boolean isSecondaryAttributeModified()
  {
    return isModified(SECONDARYATTRIBUTE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeReferenceMdDTO getSecondaryAttributeMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeReferenceMdDTO) getAttributeDTO(SECONDARYATTRIBUTE).getAttributeMdDTO();
  }
  
  public String getSecondaryCategories()
  {
    return getValue(SECONDARYCATEGORIES);
  }
  
  public void setSecondaryCategories(String value)
  {
    if(value == null)
    {
      setValue(SECONDARYCATEGORIES, "");
    }
    else
    {
      setValue(SECONDARYCATEGORIES, value);
    }
  }
  
  public boolean isSecondaryCategoriesWritable()
  {
    return isWritable(SECONDARYCATEGORIES);
  }
  
  public boolean isSecondaryCategoriesReadable()
  {
    return isReadable(SECONDARYCATEGORIES);
  }
  
  public boolean isSecondaryCategoriesModified()
  {
    return isModified(SECONDARYCATEGORIES);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getSecondaryCategoriesMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(SECONDARYCATEGORIES).getAttributeMdDTO();
  }
  
  public static net.geoprism.dashboard.DashboardThematicStyleDTO get(com.runwaysdk.constants.ClientRequestIF clientRequest, String oid)
  {
    com.runwaysdk.business.EntityDTO dto = (com.runwaysdk.business.EntityDTO)clientRequest.get(oid);
    
    return (net.geoprism.dashboard.DashboardThematicStyleDTO) dto;
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
  
  public static net.geoprism.dashboard.DashboardThematicStyleQueryDTO getAllInstances(com.runwaysdk.constants.ClientRequestIF clientRequest, String sortAttribute, Boolean ascending, Integer pageSize, Integer pageNumber)
  {
    return (net.geoprism.dashboard.DashboardThematicStyleQueryDTO) clientRequest.getAllInstances(net.geoprism.dashboard.DashboardThematicStyleDTO.CLASS, sortAttribute, ascending, pageSize, pageNumber);
  }
  
  public void lock()
  {
    getRequest().lock(this);
  }
  
  public static net.geoprism.dashboard.DashboardThematicStyleDTO lock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardThematicStyleDTO.CLASS, "lock", _declaredTypes);
    return (net.geoprism.dashboard.DashboardThematicStyleDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
  public void unlock()
  {
    getRequest().unlock(this);
  }
  
  public static net.geoprism.dashboard.DashboardThematicStyleDTO unlock(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String oid)
  {
    String[] _declaredTypes = new String[]{"java.lang.String"};
    Object[] _parameters = new Object[]{oid};
    com.runwaysdk.business.MethodMetaData _metadata = new com.runwaysdk.business.MethodMetaData(net.geoprism.dashboard.DashboardThematicStyleDTO.CLASS, "unlock", _declaredTypes);
    return (net.geoprism.dashboard.DashboardThematicStyleDTO) clientRequest.invokeMethod(_metadata, null, _parameters);
  }
  
}
