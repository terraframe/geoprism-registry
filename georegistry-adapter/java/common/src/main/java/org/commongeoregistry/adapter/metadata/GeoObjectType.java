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
package org.commongeoregistry.adapter.metadata;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * A {@link GeoObjectType} represents the definition of a location type, such as
 * state, county, province, district, village household, or health facility. A
 * {@link GeoObjectType} specifies the geometry type stored and the feature
 * attributes on the {@link GeoObject}s that are instances of the
 * {@link GeoObjectType}. {@link GeoObjectType} objects are used to define
 * integrity constraints on the user
 * 
 * @author nathan
 * @author rrowlands
 *
 */
public class GeoObjectType implements Serializable
{
  /**
   * 
   */
  private static final long          serialVersionUID           = 2857923921744440744L;

  public static final String         JSON_ATTRIBUTES            = "attributes";

  public static final String         JSON_CODE                  = "code";

  public static final String         JSON_LOCALIZED_LABEL       = "label";

  public static final String         JSON_LOCALIZED_DESCRIPTION = "description";

  public static final String         JSON_GEOMETRY_TYPE         = "geometryType";

  public static final String         JSON_IS_GEOMETRY_EDITABLE  = "isGeometryEditable";

  public static final String         JSON_IS_DEFAULT            = "isDefault";

  public static final String         JSON_ORGANIZARION_CODE     = "organizationCode";

  public static final String         JSON_IS_ABSTRACT           = "isAbstract";

  public static final String         JSON_SUPER_TYPE_CODE       = "superTypeCode";
  
  public static final String         JSON_IS_PRIVATE            = "isPrivate";

  /**
   * Unique but human readable identifier. It could be VILLAGE or HOUSEHOLD.
   */
  private String                     code;

  /**
   * Type of geometry used for instances of this {@link GeoObjectType}, such as
   * point, line, polygon, etc.
   */
  private GeometryType               geometryType;

  /**
   * The localized label of this type, such as Village or Household. Used for
   * display in the presentation tier.
   */
  private LocalizedValue             label;

  /**
   * The localized description of this type, used for display in the
   * presentation tier.
   */
  private LocalizedValue             description;

  /**
   * Indicates if geometries can be modified through the web interface.
   */
  private Boolean                    isGeometryEditable;

  /**
   * Indicates if the type is abstract.
   */
  private Boolean                    isAbstract;

  /**
   * The abstract parent type ofthis {@link GeoObjectType}. This can be null.
   */
  private String                     superTypeCode;

  /**
   * The organization responsible for this {@link GeoObjectType}. This can be
   * null.
   */
  private String                     organizationCode;
  
  /**
   * Whether or not the GeoObjectType is publicly viewable, or restricted to their organization.
   */
  private Boolean                    isPrivate = false;

  /**
   * Collection of {@link AttributeType} metadata attributes.
   * 
   * key: {@code AttributeType#getName()}
   * 
   * value: {@code AttributeType}
   * 
   */
  private Map<String, AttributeType> attributeMap;

  /**
   * 
   * Precondition: The organization code is valid.
   * 
   * @param code
   *          unique identifier that his human readable.
   * @param geometryType
   *          type of geometry for the {@link GeoObjectType} such as point,
   *          line, etc.
   * @param label
   *          localized label of the {@link GeoObjectType}.
   * @param description
   *          localized description of the {@link GeoObjectType}.
   * @param registry
   *          {@link RegistryAdapter} from which this {@link GeoObjectType} is
   *          defined.
   */
  public GeoObjectType(String code, GeometryType geometryType, LocalizedValue label, LocalizedValue description, Boolean isGeometryEditable, String organizationCode, RegistryAdapter registry)
  {
    this.init(code, geometryType, label, description, isGeometryEditable, organizationCode);

    this.attributeMap = buildDefaultAttributes(registry);
  }

  /**
   * 
   * Precondition: The organization code is valid.
   * 
   * @param code
   *          unique identifier that his human readable.
   * @param geometryType
   *          type of geometry for the {@link GeoObjectType} such as point,
   *          line, etc.
   * @param label
   *          localized label of the {@link GeoObjectType}.
   * @param description
   *          localized description of the {@link GeoObjectType}.
   * @param isGeometryEditable
   *          True if geometries can be modified through the web interface
   * @param attributeMap
   *          attribute map.
   */
  private GeoObjectType(String code, GeometryType geometryType, LocalizedValue label, LocalizedValue description, Boolean isGeometryEditable, String organizationCode, Map<String, AttributeType> attributeMap)
  {
    this.init(code, geometryType, label, description, isGeometryEditable, organizationCode);

    this.attributeMap = attributeMap;
  }

  /**
   * Initializes member variables.
   * 
   * Precondition: The organization code is valid.
   * 
   * @param code
   * @param geometryType
   * @param label
   * @param description
   * @param isGeometryEditable
   * 
   */
  private void init(String code, GeometryType geometryType, LocalizedValue label, LocalizedValue description, Boolean isGeometryEditable, String organizationCode)
  {
    this.code = code;
    this.label = label;
    this.description = description;

    this.geometryType = geometryType;

    this.isGeometryEditable = isGeometryEditable;

    this.organizationCode = organizationCode;
  }

  /**
   * Creates a new instance of the current object and copies the attributes from
   * the given {@link GeoObject} into this object.
   * 
   * @param gotSource
   *          {@link GeoObject} with attributes to copy into this attribute.
   * 
   * @return this {@link GeoObject}
   */
  public GeoObjectType copy(GeoObjectType gotSource)
  {
    GeoObjectType newGeoObjt = new GeoObjectType(this.code, this.geometryType, this.label, this.description, this.isGeometryEditable, this.organizationCode, this.attributeMap);

    newGeoObjt.code = gotSource.getCode();
    newGeoObjt.label = gotSource.getLabel();
    newGeoObjt.description = gotSource.getDescription();
    newGeoObjt.geometryType = gotSource.getGeometryType();
    newGeoObjt.isGeometryEditable = gotSource.isGeometryEditable();
    newGeoObjt.organizationCode = gotSource.getOrganizationCode();
    newGeoObjt.isAbstract = gotSource.getIsAbstract();
    newGeoObjt.superTypeCode = gotSource.getSuperTypeCode();
    newGeoObjt.isPrivate = gotSource.getIsPrivate();

    return newGeoObjt;
  }

  /**
   * Returns the code which is the human readable unique identifier.
   * 
   * @return Code value.
   */
  public String getCode()
  {
    return this.code;
  }
  
  public Boolean getIsPrivate()
  {
    return isPrivate;
  }

  public void setIsPrivate(Boolean isPrivate)
  {
    this.isPrivate = isPrivate;
  }

  /**
   * Returns the {@link GeometryType} supported for instances of this
   * {@link GeoObjectType}.
   * 
   * @return {@link GeometryType}.
   */
  public GeometryType getGeometryType()
  {
    return this.geometryType;
  }

  public void setGeometryType(GeometryType geometryType)
  {
    this.geometryType = geometryType;
  }

  /**
   * Returns the localized label of this {@link GeoObjectType} used for the
   * presentation layer.
   * 
   * @return Localized label of this {@link GeoObjectType}.
   */
  public LocalizedValue getLabel()
  {
    return this.label;
  }

  /**
   * Sets the localized display label of this {@link GeoObjectType}.
   * 
   * @param label
   */
  public void setLabel(LocalizedValue label)
  {
    this.label = label;
  }

  /**
   * Sets the localized display label of this {@link GeoObjectType}.
   * 
   * Precondition: key may not be null Precondition: key must represent a valid
   * locale that has been defined on the back-end
   * 
   * @param key
   *          string of the locale name.
   * @param value
   *          value for the given locale.
   */
  public void setLabel(String key, String value)
  {
    this.label.setValue(key, value);
  }

  /**
   * @return True if geometries can be edited through the web interface
   */
  public Boolean isGeometryEditable()
  {
    if (this.isGeometryEditable != null)
    {
      return isGeometryEditable;
    }

    return Boolean.TRUE;
  }

  /**
   * @param isGeometryEditable
   */
  public void setIsGeometryEditable(Boolean isGeometryEditable)
  {
    this.isGeometryEditable = isGeometryEditable;
  }

  /**
   * Returns the localized description of this {@link GeoObjectType} used for
   * the presentation layer.
   * 
   * @return Localized description of this {@link GeoObjectType}.
   */
  public LocalizedValue getDescription()
  {
    return this.description;
  }

  /**
   * Sets the localized display label of this {@link GeoObjectType}.
   * 
   * @param description
   */
  public void setDescription(LocalizedValue description)
  {
    this.description = description;
  }

  /**
   * Sets the localized display label of this {@link GeoObjectType}.
   * 
   * @param description
   */
  public void setDescription(String key, String value)
  {
    this.description.setValue(key, value);
  }

  /**
   * 
   * @return the code of the {@link OrganizationDTO} (Optional) that manages
   *         this {@link GeoObjectType}, or NULL if not managed by an
   *         {@link OrganizationDTO}.
   */
  public String getOrganizationCode()
  {
    return this.organizationCode;
  }

  /**
   * Sets the {@link OrganizationDTO} (Optional) that manages this
   * {@link GeoObjectType}.
   * 
   * Precondition: The organization code is valid
   * 
   * @param organizationCode
   *          code of the {@link OrganizationDTO} that manages this
   *          {@link GeoObjectType}, or NULL if none.
   */
  public void setOrganizationCode(String organizationCode)
  {
    this.organizationCode = organizationCode;
  }

  public Boolean getIsAbstract()
  {
    return ( isAbstract != null ? this.isAbstract : Boolean.FALSE );
  }

  public void setIsAbstract(Boolean isAbstract)
  {
    this.isAbstract = isAbstract;
  }

  public String getSuperTypeCode()
  {
    return superTypeCode;
  }

  public void setSuperTypeCode(String superTypeCode)
  {
    this.superTypeCode = superTypeCode;
  }

  /**
   * Returns the {@link AttributeType} defined on this {@link GeoObjectType}
   * with the given name.
   * 
   * @param name
   *          Name of the attribute {@code AttributeType#getName()}.
   * 
   *          Precondition: Attribute with the given name is defined on this
   *          {@link GeoObjectType}.
   * 
   * @return Name of the attributes.
   */
  public Optional<AttributeType> getAttribute(String name)
  {
    return Optional.of(this.attributeMap.get(name));
  }

  /**
   * Adds the given {@link AttributeType} as an attribute defined on this
   * {@link GeoObjectType}.
   * 
   * @param attributeType
   *          {@link AttributeType} to add to this {@link GeoObjectType}.
   */
  public void addAttribute(AttributeType attributeType)
  {
    this.attributeMap.put(attributeType.getName(), attributeType);
  }

  /**
   * Removes the given {@link AttributeType} as an attribute defined on this
   * {@link GeoObjectType}.
   * 
   * @param attributeName
   *          {@link AttributeType} to remove from this {@link GeoObjectType}.
   */
  public void removeAttribute(String attributeName)
  {
    this.attributeMap.remove(attributeName);
  }

  /**
   * Returns the {@link AttributeType} objects of the attributes defined on
   * this @link GeoObjectType}.
   * 
   * @return {@link AttributeType} objects of the attributes defined on
   *         this @link GeoObjectType}.
   */
  public Map<String, AttributeType> getAttributeMap()
  {
    return this.attributeMap;
  }

  /**
   * Defines the standard set of {@link AttributeType} defined on
   * all{@link GeoObjectType}s.
   * 
   * @return the standard set of {@link AttributeType} defined on
   *         all{@link GeoObjectType}s.
   */
  private static Map<String, AttributeType> buildDefaultAttributes(RegistryAdapter registry)
  {
    Map<String, AttributeType> defaultAttributeMap = new ConcurrentHashMap<String, AttributeType>();

    AttributeCharacterType uid = (AttributeCharacterType) DefaultAttribute.UID.createAttributeType();
    defaultAttributeMap.put(DefaultAttribute.UID.getName(), uid);

    AttributeCharacterType code = (AttributeCharacterType) DefaultAttribute.CODE.createAttributeType();
    defaultAttributeMap.put(DefaultAttribute.CODE.getName(), code);
    
    AttributeBooleanType invalid = (AttributeBooleanType) DefaultAttribute.INVALID.createAttributeType();
    defaultAttributeMap.put(DefaultAttribute.INVALID.getName(), invalid);

    AttributeLocalType displayLabel = (AttributeLocalType) DefaultAttribute.DISPLAY_LABEL.createAttributeType();
    defaultAttributeMap.put(DefaultAttribute.DISPLAY_LABEL.getName(), displayLabel);

    AttributeCharacterType type = (AttributeCharacterType) DefaultAttribute.TYPE.createAttributeType();
    defaultAttributeMap.put(DefaultAttribute.TYPE.getName(), type);

    AttributeIntegerType sequence = (AttributeIntegerType) DefaultAttribute.SEQUENCE.createAttributeType();
    defaultAttributeMap.put(DefaultAttribute.SEQUENCE.getName(), sequence);

    AttributeDateType createdDate = (AttributeDateType) DefaultAttribute.CREATE_DATE.createAttributeType();
    defaultAttributeMap.put(DefaultAttribute.CREATE_DATE.getName(), createdDate);

    AttributeDateType updatedDate = (AttributeDateType) DefaultAttribute.LAST_UPDATE_DATE.createAttributeType();
    defaultAttributeMap.put(DefaultAttribute.LAST_UPDATE_DATE.getName(), updatedDate);

    AttributeBooleanType exists = (AttributeBooleanType) DefaultAttribute.EXISTS.createAttributeType();
    defaultAttributeMap.put(DefaultAttribute.EXISTS.getName(), exists);

    // AttributeCharacterType organization = (AttributeCharacterType)
    // DefaultAttribute.ORGANIZATION.createAttributeType();
    // defaultAttributeMap.put(DefaultAttribute.ORGANIZATION.getName(),
    // organization);
    //
    // AttributeGeometryType geometry = (AttributeGeometryType)
    // DefaultAttribute.GEOMETRY.createAttributeType();
    // defaultAttributeMap.put(DefaultAttribute.GEOMETRY.getName(), geometry);

    return defaultAttributeMap;
  }

  public static GeoObjectType[] fromJSONArray(String saJson, RegistryAdapter adapter)
  {
    JsonParser parser = new JsonParser();

    JsonArray jaGots = parser.parse(saJson).getAsJsonArray();
    GeoObjectType[] gots = new GeoObjectType[jaGots.size()];
    for (int i = 0; i < jaGots.size(); ++i)
    {
      GeoObjectType got = GeoObjectType.fromJSON(jaGots.get(i).toString(), adapter);
      gots[i] = got;
    }

    return gots;
  }

  /**
   * Creates a {@link GeoObjectType} from the given JSON string.
   * 
   * @param sJson
   *          JSON string that defines the {@link GeoObjectType}.
   * @param registry
   *          {@link RegistryAdapter} from which this {@link GeoObjectType}
   *          object comes.
   * @return
   */
  public static GeoObjectType fromJSON(String sJson, RegistryAdapter registry)
  {
    JsonParser parser = new JsonParser();

    JsonObject oJson = parser.parse(sJson).getAsJsonObject();
    JsonArray oJsonAttrs = oJson.getAsJsonArray(JSON_ATTRIBUTES);

    String code = oJson.get(JSON_CODE).getAsString();
    LocalizedValue label = LocalizedValue.fromJSON(oJson.get(JSON_LOCALIZED_LABEL).getAsJsonObject());
    LocalizedValue description = LocalizedValue.fromJSON(oJson.get(JSON_LOCALIZED_DESCRIPTION).getAsJsonObject());
    GeometryType geometryType = GeometryType.valueOf(oJson.get(JSON_GEOMETRY_TYPE).getAsString());
    Boolean isGeometryEditable = new Boolean(oJson.get(JSON_IS_GEOMETRY_EDITABLE).getAsBoolean());

    String organizationCode = null;
    JsonElement jsonOrganization = oJson.get(JSON_ORGANIZARION_CODE);

    if (jsonOrganization != null)
    {
      organizationCode = jsonOrganization.getAsString();
    }

    Map<String, AttributeType> attributeMap = buildDefaultAttributes(registry);

    for (int i = 0; i < oJsonAttrs.size(); ++i)
    {
      JsonObject joAttr = oJsonAttrs.get(i).getAsJsonObject();
      AttributeType attrType = AttributeType.parse(joAttr);

      attributeMap.put(attrType.getName(), attrType);
    }

    // TODO Need to validate that the default attributes are still defined.
    GeoObjectType geoObjType = new GeoObjectType(code, geometryType, label, description, isGeometryEditable, organizationCode, attributeMap);

    if (oJson.has(JSON_SUPER_TYPE_CODE))
    {
      geoObjType.setSuperTypeCode(oJson.get(JSON_SUPER_TYPE_CODE).getAsString());
    }

    if (oJson.has(JSON_IS_ABSTRACT))
    {
      geoObjType.setIsAbstract(oJson.get(JSON_IS_ABSTRACT).getAsBoolean());
    }
    
    if (oJson.has(JSON_IS_PRIVATE))
    {
      geoObjType.setIsPrivate(oJson.get(JSON_IS_PRIVATE).getAsBoolean());
    }

    return geoObjType;
  }

  /**
   * Return the JSON representation of this {@link GeoObjectType}.
   * 
   * @return JSON representation of this {@link GeoObjectType}.
   */
  public final JsonObject toJSON()
  {
    return toJSON(new DefaultSerializer());
  }

  /**
   * Return the JSON representation of this {@link GeoObjectType}. Filters the
   * attributes to include in serialization.
   * 
   * @param filter
   *          Filter used to determine if an attribute is included
   * 
   * @return JSON representation of this {@link GeoObjectType}.
   */
  public JsonObject toJSON(CustomSerializer serializer)
  {
    JsonObject json = new JsonObject();

    json.addProperty(JSON_CODE, this.getCode());

    json.add(JSON_LOCALIZED_LABEL, this.getLabel().toJSON(serializer));

    json.add(JSON_LOCALIZED_DESCRIPTION, this.getDescription().toJSON(serializer));

    // TODO: PROPOSED but not yet approved. Required for fromJSON
    // reconstruction.
    json.addProperty(JSON_GEOMETRY_TYPE, this.geometryType.name());

    json.addProperty(JSON_IS_GEOMETRY_EDITABLE, this.isGeometryEditable());
    
    json.addProperty(JSON_IS_PRIVATE, this.getIsPrivate());

    String organizationString;
    if (this.organizationCode == null)
    {
      organizationString = "";
    }
    else
    {
      organizationString = this.organizationCode;
    }
    json.addProperty(JSON_ORGANIZARION_CODE, organizationString);

    json.addProperty(JSON_SUPER_TYPE_CODE, this.superTypeCode != null ? this.superTypeCode : "");

    if (this.isAbstract != null && this.isAbstract)
    {
      json.addProperty(JSON_IS_ABSTRACT, this.isAbstract);
    }

    Collection<AttributeType> attributes = serializer.attributes(this);

    JsonArray attrs = serializer.serialize(this, attributes);

    json.add(JSON_ATTRIBUTES, attrs);

    serializer.configure(this, json);

    return json;
  }

}
