/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commongeoregistry.adapter.dataaccess;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.RequiredParameterException;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.metadata.AttributeGeometryType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.CustomSerializer;
import org.commongeoregistry.adapter.metadata.DefaultSerializer;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vividsolutions.jts.geom.Geometry;

public class GeoObjectOverTime implements Serializable
{

  private static final long serialVersionUID = 6218261169426542019L;
  
  GeoObjectType          geoObjectType;
  
  /**
   * For attributes that do change over time, they will be stored here.
   */
  Map<String, ValueOverTimeCollectionDTO> votAttributeMap;
  
  /**
   * Not all attributes are stored with change-over-time properties. You can check the AttributeType
   * to see if the attribute changes over time.
   */
  Map<String, Attribute> attributeMap;
  
  private AttributeGeometryType geometryAttributeType;
  
  public GeoObjectOverTime(GeoObjectType geoObjectType, Map<String, ValueOverTimeCollectionDTO> votAttributeMap, Map<String, Attribute> attributeMap)
  {
    this.geoObjectType = geoObjectType;
    this.votAttributeMap = votAttributeMap;
    this.attributeMap = attributeMap;
    geometryAttributeType = (AttributeGeometryType) DefaultAttribute.GEOMETRY.createAttributeType();
    
    this.setValue(DefaultAttribute.TYPE.getName(), this.geoObjectType.getCode());
  }
  
  public static Map<String, ValueOverTimeCollectionDTO> buildVotAttributeMap(GeoObjectType geoObjectType)
  {
    Map<String, AttributeType> attributeTypeMap = geoObjectType.getAttributeMap();

    Map<String, ValueOverTimeCollectionDTO> attributeMap = new ConcurrentHashMap<String, ValueOverTimeCollectionDTO>();

    for (AttributeType attributeType : attributeTypeMap.values())
    {
      if (attributeType.isChangeOverTime())
      {
        ValueOverTimeCollectionDTO votc = new ValueOverTimeCollectionDTO(attributeType);
  
        attributeMap.put(attributeType.getName(), votc);
      }
    }
    
    AttributeGeometryType geometry = (AttributeGeometryType) DefaultAttribute.GEOMETRY.createAttributeType();
    ValueOverTimeCollectionDTO votc = new ValueOverTimeCollectionDTO(geometry);
    attributeMap.put(geometry.getName(), votc);

    return attributeMap;
  }
  
  public static Map<String, Attribute> buildAttributeMap(GeoObjectType geoObjectType)
  {
    Map<String, AttributeType> attributeTypeMap = geoObjectType.getAttributeMap();

    Map<String, Attribute> attributeMap = new ConcurrentHashMap<String, Attribute>();

    for (AttributeType attributeType : attributeTypeMap.values())
    {
      if (!attributeType.isChangeOverTime())
      {
        Attribute attribute = Attribute.attributeFactory(attributeType);

        attributeMap.put(attribute.getName(), attribute);
      }
    }

    return attributeMap;
  }
  
  public GeoObjectType getType()
  {
    return this.geoObjectType;
  }
  
  
  /**
   * Returns the Attribute at the exact start date. If date is null,
   * it is assumed to be the latest date at which data is available (infinity).
   * If no values exist, one will be created. If no values exist and the date is null,
   * then a value will be created with the current date.
   * 
   * @param date
   * @return
   */
  public Attribute getOrCreateAttribute(String key, Date startDate)
  {
    return this.votAttributeMap.get(key).getOrCreateAttribute(startDate);
  }
  
  /**
   * Returns the attribute which represents the given day. If no start or end date exactly
   * matches this day, then the attribute which spans the date range which this date falls
   * within will be returned. If the provided date is null, the date is assumed to be infinity,
   * in which case the latest value will be returned. This method may return null if the provided
   * date occurs before all recorded data.
   * 
   * @param key
   * @param date
   * @return
   */
  public Attribute getAttributeOnDate(String key, Date date)
  {
    return this.votAttributeMap.get(key).getAttributeOnDate(date);
  }
  
  /**
   * Sets the WKT geometry at the exact start date. If date is null, it will be set to today's date.
   * If no value exists at the exact start date, one will be created. The end date will automatically
   * span the range to the next available value in the system, or infinity if one does not exist.
   * 
   * @param date
   * @return
   */
  public void setWKTGeometry(String wkt, Date startDate)
  {
    ((AttributeGeometry) this.votAttributeMap.get(DefaultAttribute.GEOMETRY.getName()).getOrCreateAttribute(startDate)).setWKTGeometry(wkt);
  }
  
  public ValueOverTimeCollectionDTO getAllValues(String attributeName)
  {
    return this.votAttributeMap.get(attributeName);
  }
  
  /**
   * Returns the value of the non-change-over-time attribute with the given name.
   * 
   * @pre attribute with the given name is defined on the {@link GeoObjectType}
   *      that defines this {@link GeoObject}.
   * 
   * @param attributeName
   * 
   * @return value of the attribute with the given name.
   */
  public Object getValue(String attributeName)
  {
    if (this.attributeMap.containsKey(attributeName))
    {
      return this.attributeMap.get(attributeName).getValue();
    }
    else if (this.votAttributeMap.containsKey(attributeName))
    {
      return this.votAttributeMap.get(attributeName).getValueOnDate(null);
    }
    else
    {
      throw new RuntimeException("Attribute not found [" + attributeName + "]."); // TODO : Better error handling
    }
  }
  
  /**
   * Returns the value which represents the given day. If no start or end date exactly
   * matches this day, then the value which spans the date range which this date falls
   * within will be returned. If the provided date is null, the date is assumed to be infinity,
   * in which case the latest value will be returned. This method may return null if the provided
   * date occurs before all recorded data.
   * 
   * @pre attribute with the given name is defined on the {@link GeoObjectType}
   *      that defines this {@link GeoObject}.
   * 
   * @param attributeName
   * 
   * @return value of the attribute with the given name.
   */
  public Object getValue(String attributeName, Date date)
  {
    return this.votAttributeMap.get(attributeName).getValueOnDate(date);
  }
  
  /**
   * Sets the value of the non-change-over-time attribute.
   * 
   * @param attributeName
   * @param _value
   */
  public void setValue(String attributeName, Object _value)
  {
    Optional<AttributeType> optional = this.getType().getAttribute(attributeName);
    
    if (optional.isPresent())
    {
      optional.get().validate(_value);
    }
    
    if (this.attributeMap.containsKey(attributeName))
    {
      this.attributeMap.get(attributeName).setValue(_value);
    }
    else if (this.votAttributeMap.containsKey(attributeName))
    {
      this.votAttributeMap.get(attributeName).setValue(_value, null);
    }
    else
    {
      throw new RuntimeException("Attribute not found [" + attributeName + "]."); // TODO : Better error handling
    }
  }
  
  /**
   * Sets the value of the change-over-time attribute. If endDate is null then it is assumed to
   * expand as far as possible into the future. If startDate is null then it will grab the latest
   * available value and set it. If one does not exist one will be created with today's date.
   * 
   * @throws {@link RequiredParamterException} if startDate is missing
   * @param attributeName
   * @param _value
   */
  public void setValue(String attributeName, Object _value, Date startDate, Date endDate)
  {
    ValueOverTimeCollectionDTO votc = this.votAttributeMap.get(attributeName);
    
    if (attributeName.equals(DefaultAttribute.GEOMETRY.getName()))
    {
      geometryAttributeType.validate(_value);
    }
    else
    {
      Optional<AttributeType> optional = this.getType().getAttribute(attributeName);
      
      if (optional.isPresent())
      {
        optional.get().validate(_value);
      }
    }
    
    ValueOverTimeDTO vot = votc.getOrCreate(startDate);
    vot.setEndDate(endDate);
    vot.setValue(_value);
  }
  
  /**
   * Allows for bulk setting of all values for a given attribute. Be careful when
   * using this method as it is quite powerful and not for everyday usecases.
   * 
   * @param attributeName
   */
  public void setValueCollection(String attributeName, ValueOverTimeCollectionDTO collection)
  {
    this.votAttributeMap.put(attributeName, collection);
  }

  /**
   * Returns the geometry of this {@link GeoObject}
   * 
   * @return the geometry of this {@link GeoObject}
   */
  public Geometry getGeometry(Date date)
  {
    return (Geometry) this.getValue(DefaultAttribute.GEOMETRY.getName(), date);
  }
  
  /**
   * Set the {@link Geometry} on this {@link GeoObject}
   * 
   * @param geometry
   */
  public void setGeometry(Geometry geometry, Date startDate, Date endDate)
  {
    this.setValue(DefaultAttribute.GEOMETRY.getName(), geometry, startDate, endDate);
  }
  
  public AttributeGeometryType getGeometryAttributeType()
  {
    return this.geometryAttributeType;
  }
  
  /**
   * Sets the code of this {@link GeoObject}.
   * 
   * @param code
   */
  public void setCode(String code)
  {
    this.setValue(DefaultAttribute.CODE.getName(), code);
  }

  /**
   * Returns the code id of this {@link GeoObject}
   * 
   * @return the code id of this {@link GeoObject}
   */
  public String getCode()
  {
    return (String) this.getValue(DefaultAttribute.CODE.getName());
  }
  
  /**
   * Sets the invalid of this {@link GeoObject}.
   * 
   * @param code
   */
  public void setInvalid(Boolean invalid)
  {
    this.attributeMap.get(DefaultAttribute.INVALID.getName()).setValue(invalid);
  }

  /**
   * Returns the invalid of this {@link GeoObject}
   * 
   * @return the invalid of this {@link GeoObject}
   */
  public Boolean getInvalid()
  {
    return (Boolean) this.attributeMap.get(DefaultAttribute.INVALID.getName()).getValue();
  }
  
  /**
   * Sets the display label of this {@link GeoObject}. If endDate is null then it is assumed to
   * expand as far as possible into the future. If startDate is null then it will grab the latest
   * available value and set it. If one does not exist one will be created.
   * 
   * @param label
   * @param startDate
   * @param endDate
   */
  public void setDisplayLabel(LocalizedValue label, Date startDate, Date endDate)
  {
    this.setValue(DefaultAttribute.DISPLAY_LABEL.getName(), label, startDate, endDate);
  }

  /**
   * Returns the display label of this {@link GeoObjectOverTime}. If date is null
   * it is assumed to be the latest date at which data is available (infinity).
   * 
   * @return the display label of this {@link GeoObjectOverTime}
   */
  public LocalizedValue getDisplayLabel(Date startDate)
  {
    return (LocalizedValue) this.getValue(DefaultAttribute.DISPLAY_LABEL.getName(), startDate);
  }

  /**
   * Sets the UID of this {@link GeoObject}.
   * 
   * @param uid
   */
  public void setUid(String uid)
  {
    this.setValue(DefaultAttribute.UID.getName(), uid);
  }

  /**
   * Returns the UID of this {@link GeoObject}.
   * 
   * @return
   */
  public String getUid()
  {
    return (String) this.getValue(DefaultAttribute.UID.getName());
  }
  
  /**
   * Returns the status code
   * 
   * @return
   */
  public Boolean getExists(Date date)
  {
    return (Boolean) this.getValue(DefaultAttribute.EXISTS.getName(), date);
  }

  public void setExists(Boolean exists, Date startDate, Date endDate)
  {
    this.setValue(DefaultAttribute.EXISTS.getName(), exists, startDate, endDate);
  }
  
  /**
   * Creates a {@link GeoObjectOverTime} from the given JSON.
   * 
   * @pre assumes the attributes on the JSON are valid attributes defined by the
   *      {@link GeoObjectType}
   * 
   * @param _registry
   * @param _sJson
   * 
   * @return {@link GeoObjectOverTime} from the given JSON.
   */
  public static GeoObjectOverTime fromJSON(RegistryAdapter registry, String sJson)
  {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(GeoObjectOverTime.class, new GeoObjectOverTimeJsonAdapters.GeoObjectDeserializer(registry));

    return builder.create().fromJson(sJson, GeoObjectOverTime.class);
  }

  public JsonObject toJSON()
  {
    return toJSON(new DefaultSerializer());
  }

  public JsonObject toJSON(CustomSerializer serializer)
  {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(GeoObjectOverTime.class, new GeoObjectOverTimeJsonAdapters.GeoObjectSerializer());

    return (JsonObject) builder.create().toJsonTree(this);
  }
  
}
