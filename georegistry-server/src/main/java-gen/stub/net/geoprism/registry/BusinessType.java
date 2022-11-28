/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.IndexTypes;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeCharacterInfo;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.graph.MdEdgeInfo;
import com.runwaysdk.constants.graph.MdVertexInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeMultiTermDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.metadata.MdAttributeCharacterDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeConcreteDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.constants.MdGeoVertexInfo;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.localization.SupportedLocaleIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Roles;
import com.runwaysdk.system.metadata.MdAttributeConcrete;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.conversion.AttributeTypeConverter;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.localization.DefaultLocaleView;
import net.geoprism.registry.model.AttributedType;
import net.geoprism.registry.model.ServerElement;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.query.graph.BusinessObjectPageQuery;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.Page;

public class BusinessType extends BusinessTypeBase implements JsonSerializable, ServerElement, AttributedType
{
  private static final long  serialVersionUID = 88826735;

  public static final String JSON_ATTRIBUTES  = "attributes";

  public static final String JSON_CODE        = "code";

  public BusinessType()
  {
    super();
  }

  @Override
  @Transaction
  public void delete()
  {
    // Delete the term root
    Classifier classRootTerm = TermConverter.buildIfNotExistdMdBusinessClassifier(this.getMdVertex());
    classRootTerm.delete();

    MdVertex mdVertex = this.getMdVertex();
    MdEdge mdEdge = this.getMdEdge();

    super.delete();

    mdEdge.delete();
    mdVertex.delete();
  }

  public MdVertexDAOIF getMdVertexDAO()
  {
    return MdVertexDAO.get(this.getMdVertexOid());
  }

  public MdEdgeDAOIF getMdEdgeDAO()
  {
    return MdEdgeDAO.get(this.getMdEdgeOid());
  }

  public AttributeType createAttributeType(AttributeType attributeType)
  {
    MdAttributeConcrete mdAttribute = ServerGeoObjectType.createMdAttributeFromAttributeType(this.getMdVertex(), attributeType);

    // Refresh the users session
    if (Session.getCurrentSession() != null)
    {
      // Refresh the users session
      ( (Session) Session.getCurrentSession() ).reloadPermissions();
    }

    return new AttributeTypeConverter().build(MdAttributeConcreteDAO.get(mdAttribute.getOid()));
  }

  public AttributeType createAttributeType(String attributeTypeJSON)
  {
    JsonObject attrObj = JsonParser.parseString(attributeTypeJSON).getAsJsonObject();

    AttributeType attrType = AttributeType.parse(attrObj);

    return createAttributeType(attrType);
  }

  public AttributeType updateAttributeType(String attributeTypeJSON)
  {
    JsonObject attrObj = JsonParser.parseString(attributeTypeJSON).getAsJsonObject();
    AttributeType attrType = AttributeType.parse(attrObj);

    return updateAttributeType(attrType);
  }

  public void setLabelAttribute(String attributeName)
  {
    MdVertexDAOIF mdVertex = this.getMdVertexDAO();
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(attributeName);

    this.setLabelAttributeId(mdAttribute.getOid());
  }

  public AttributeType updateAttributeType(AttributeType attrType)
  {
    MdAttributeConcrete mdAttribute = ServerGeoObjectType.updateMdAttributeFromAttributeType(this.getMdVertex(), attrType);
    return new AttributeTypeConverter().build(MdAttributeConcreteDAO.get(mdAttribute.getOid()));
  }

  public void removeAttribute(String attributeName)
  {
    this.deleteMdAttributeFromAttributeType(attributeName);

    // Refresh the users session
    if (Session.getCurrentSession() != null)
    {
      // Refresh the users session
      ( (Session) Session.getCurrentSession() ).reloadPermissions();
    }
  }

  /**
   * Delete the {@link MdAttributeConcreteDAOIF} from the given {
   * 
   * @param type
   *          TODO
   * @param mdBusiness
   * @param attributeName
   */
  @Transaction
  public void deleteMdAttributeFromAttributeType(String attributeName)
  {
    MdAttributeConcreteDAOIF mdAttributeConcreteDAOIF = ServerGeoObjectType.getMdAttribute(this.getMdVertex(), attributeName);

    if (mdAttributeConcreteDAOIF != null)
    {
      if (mdAttributeConcreteDAOIF instanceof MdAttributeTermDAOIF || mdAttributeConcreteDAOIF instanceof MdAttributeMultiTermDAOIF)
      {
        String attributeTermKey = TermConverter.buildtAtttributeKey(this.getMdVertex().getTypeName(), mdAttributeConcreteDAOIF.definesAttribute());

        try
        {
          Classifier attributeTerm = Classifier.getByKey(attributeTermKey);
          attributeTerm.delete();
        }
        catch (DataNotFoundException e)
        {
        }
      }

      mdAttributeConcreteDAOIF.getBusinessDAO().delete();
    }
  }

  public Map<String, AttributeType> getAttributeMap()
  {
    AttributeTypeConverter converter = new AttributeTypeConverter();

    MdVertexDAOIF mdVertex = this.getMdVertexDAO();

    return mdVertex.definesAttributes().stream().filter(attr -> {
      return !attr.isSystem() && !attr.definesAttribute().equals(BusinessType.SEQ);
    }).map(attr -> converter.build(attr)).collect(Collectors.toMap(AttributeType::getName, attr -> attr));
  }

  public AttributeType getAttribute(String name)
  {
    AttributeTypeConverter converter = new AttributeTypeConverter();

    MdVertexDAOIF mdVertex = this.getMdVertexDAO();
    MdAttributeConcreteDAOIF mdAttribute = (MdAttributeConcreteDAOIF) mdVertex.definesAttribute(name);

    return converter.build(mdAttribute);
  }

  public LocalizedValue getLabel()
  {
    return LocalizedValueConverter.convertNoAutoCoalesce(this.getDisplayLabel());
  }

  @Override
  public JsonObject toJSON()
  {
    return toJSON(false, false);
  }

  public JsonObject toJSON(boolean includeAttribute, boolean flattenLocalAttributes)
  {
    Organization organization = this.getOrganization();

    JsonObject object = new JsonObject();
    object.addProperty(BusinessType.CODE, this.getCode());
    object.addProperty(BusinessType.ORGANIZATION, organization.getCode());
    object.addProperty("organizationLabel", organization.getDisplayLabel().getValue());
    object.add(BusinessType.DISPLAYLABEL, LocalizedValueConverter.convertNoAutoCoalesce(this.getDisplayLabel()).toJSON());

    if (this.getLabelAttributeOid() != null && this.getLabelAttributeOid().length() > 0)
    {
      object.addProperty(BusinessType.LABELATTRIBUTE, this.getLabelAttribute().getAttributeName());
    }
    else
    {
      object.addProperty(BusinessType.LABELATTRIBUTE, BusinessType.CODE);
    }

    if (this.isAppliedToDB())
    {
      object.addProperty(BusinessType.OID, this.getOid());
    }

    if (includeAttribute)
    {
      Collector<Object, JsonArray, JsonArray> collector = Collector.of(() -> new JsonArray(), (r, t) -> r.add((JsonObject) t), (x1, x2) -> {
        x1.addAll(x2);
        return x1;
      });

      JsonArray attributes = this.getAttributeMap().values().stream().sorted((a, b) -> {
        return a.getName().compareTo(b.getName());
      }).flatMap(attr -> {
        if (flattenLocalAttributes && attr instanceof AttributeLocalType)
        {
          List<JsonObject> list = new LinkedList<>();
          list.add(this.serializeLocale(attr, LocalizedValue.DEFAULT_LOCALE, LocalizationFacade.localize(DefaultLocaleView.LABEL)));

          for (SupportedLocaleIF locale : LocalizationFacade.getSupportedLocales())
          {
            list.add(this.serializeLocale(attr, locale.getLocale().toString(), locale.getDisplayLabel().getValue()));
          }

          return list.stream();
        }

        return Stream.of(attr.toJSON());
      }).collect(collector);

      object.add(JSON_ATTRIBUTES, attributes);
    }

    return object;
  }

  private JsonObject serializeLocale(AttributeType attributeType, String key, String label)
  {
    JsonObject object = attributeType.toJSON();
    object.addProperty("locale", key);
    object.addProperty(AttributeType.JSON_CODE, attributeType.getName());

    JsonObject jaLabel = object.get(AttributeType.JSON_LOCALIZED_LABEL).getAsJsonObject();
    String value = jaLabel.get(LocalizedValue.LOCALIZED_VALUE).getAsString();
    value += " (" + label + ")";
    jaLabel.addProperty(LocalizedValue.LOCALIZED_VALUE, value);

    return object;
  }

  public Page<JsonSerializable> data(JsonObject criteria)
  {
    return new BusinessObjectPageQuery(this, criteria).getPage();
  }

  @Transaction
  public static BusinessType apply(JsonObject object)
  {
    String code = object.get(BusinessType.CODE).getAsString();
    String organizationCode = object.get(BusinessType.ORGANIZATION).getAsString();
    Organization organization = Organization.getByCode(organizationCode);

    ServiceFactory.getGeoObjectTypePermissionService().enforceCanCreate(organization.getCode(), false);

    if (!MasterList.isValidName(code))
    {
      throw new InvalidMasterListCodeException("The geo object type code has an invalid character");
    }

    if (code.length() > 64)
    {
      // Setting the typename on the MdBusiness creates this limitation.
      CodeLengthException ex = new CodeLengthException();
      ex.setLength(64);
      throw ex;
    }

    // assignSRAPermissions(mdVertex, mdBusiness);
    // assignAll_RA_Permissions(mdVertex, mdBusiness, organizationCode);

    LocalizedValue localizedValue = LocalizedValue.fromJSON(object.get(DISPLAYLABEL).getAsJsonObject());

    BusinessType businessType = ( object.has(OID) && !object.get(OID).isJsonNull() ) ? BusinessType.get(object.get(OID).getAsString()) : new BusinessType();
    businessType.setCode(code);
    businessType.setOrganization(organization);
    LocalizedValueConverter.populate(businessType.getDisplayLabel(), localizedValue);

    boolean isNew = businessType.isNew();

    if (isNew)
    {
      MdVertexDAO mdVertex = MdVertexDAO.newInstance();
      mdVertex.setValue(MdGeoVertexInfo.PACKAGE, RegistryConstants.BUSINESS_PACKAGE);
      mdVertex.setValue(MdGeoVertexInfo.NAME, code);
      mdVertex.setValue(MdGeoVertexInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
      mdVertex.setValue(MdGeoVertexInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
      LocalizedValueConverter.populate(mdVertex, MdVertexInfo.DISPLAY_LABEL, localizedValue);
      mdVertex.apply();

      // TODO CREATE the edge between this class and GeoVertex??
      MdVertexDAOIF mdGeoVertexDAO = MdVertexDAO.getMdVertexDAO(GeoVertex.CLASS);

      MdEdgeDAO mdEdge = MdEdgeDAO.newInstance();
      mdEdge.setValue(MdEdgeInfo.PACKAGE, RegistryConstants.BUSINESS_GRAPH_PACKAGE);
      mdEdge.setValue(MdEdgeInfo.NAME, code + "Edge");
      mdEdge.setValue(MdEdgeInfo.PARENT_MD_VERTEX, mdVertex.getOid());
      mdEdge.setValue(MdEdgeInfo.CHILD_MD_VERTEX, mdGeoVertexDAO.getOid());
      LocalizedValueConverter.populate(mdEdge, MdEdgeInfo.DISPLAY_LABEL, localizedValue);
      mdEdge.setValue(MdEdgeInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
      mdEdge.apply();

      // MdAttributeGraphReferenceDAO mdGeoObject =
      // MdAttributeGraphReferenceDAO.newInstance();
      // mdGeoObject.setValue(MdAttributeGraphReferenceInfo.REFERENCE_MD_VERTEX,
      // mdGeoVertexDAO.getOid());
      // mdGeoObject.setValue(MdAttributeGraphReferenceInfo.DEFINING_MD_CLASS,
      // mdVertex.getOid());
      // mdGeoObject.setValue(MdAttributeGraphReferenceInfo.NAME, GEO_OBJECT);
      // mdGeoObject.setStructValue(MdAttributeGraphReferenceInfo.DESCRIPTION,
      // MdAttributeLocalInfo.DEFAULT_LOCALE, "Geo Object");
      // mdGeoObject.apply();

      // DefaultAttribute.CODE
      MdAttributeCharacterDAO vertexCodeMdAttr = MdAttributeCharacterDAO.newInstance();
      vertexCodeMdAttr.setValue(MdAttributeConcreteInfo.NAME, DefaultAttribute.CODE.getName());
      vertexCodeMdAttr.setStructValue(MdAttributeConcreteInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.CODE.getDefaultLocalizedName());
      vertexCodeMdAttr.setStructValue(MdAttributeConcreteInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.CODE.getDefaultDescription());
      vertexCodeMdAttr.setValue(MdAttributeCharacterInfo.SIZE, MdAttributeCharacterInfo.MAX_CHARACTER_SIZE);
      vertexCodeMdAttr.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdVertex.getOid());
      vertexCodeMdAttr.setValue(MdAttributeConcreteInfo.REQUIRED, MdAttributeBooleanInfo.TRUE);
      vertexCodeMdAttr.addItem(MdAttributeConcreteInfo.INDEX_TYPE, IndexTypes.UNIQUE_INDEX.getOid());
      vertexCodeMdAttr.apply();

      businessType.setMdVertexId(mdVertex.getOid());
      businessType.setMdEdgeId(mdEdge.getOid());

      // Assign permissions
      Roles role = Roles.findRoleByName(RegistryConstants.REGISTRY_SUPER_ADMIN_ROLE);

      RoleDAO roleDAO = (RoleDAO) BusinessFacade.getEntityDAO(role);
      roleDAO.grantPermission(Operation.CREATE, mdVertex.getOid());
      roleDAO.grantPermission(Operation.DELETE, mdVertex.getOid());
      roleDAO.grantPermission(Operation.WRITE, mdVertex.getOid());
      roleDAO.grantPermission(Operation.WRITE_ALL, mdVertex.getOid());
    }

    if (object.has(BusinessType.LABELATTRIBUTE) && !object.get(BusinessType.LABELATTRIBUTE).isJsonNull())
    {
      String attributeName = object.get(BusinessType.LABELATTRIBUTE).getAsString();

      if (!StringUtils.isEmpty(attributeName))
      {
        businessType.setLabelAttribute(attributeName);
      }
    }

    businessType.apply();

    return businessType;
  }

  public List<BusinessEdgeType> getParentEdgeTypes()
  {
    BusinessEdgeTypeQuery query = new BusinessEdgeTypeQuery(new QueryFactory());
    query.WHERE(query.getParentType().EQ(this.getMdVertex()));
    query.ORDER_BY_DESC(query.getDisplayLabel().localize());

    try (OIterator<? extends BusinessEdgeType> iterator = query.getIterator())
    {
      List<? extends BusinessEdgeType> results = iterator.getAll();

      return new LinkedList<BusinessEdgeType>(results);
    }
  }

  public List<BusinessEdgeType> getChildEdgeTypes()
  {
    BusinessEdgeTypeQuery query = new BusinessEdgeTypeQuery(new QueryFactory());
    query.WHERE(query.getChildType().EQ(this.getMdVertex()));
    query.ORDER_BY_DESC(query.getDisplayLabel().localize());

    try (OIterator<? extends BusinessEdgeType> iterator = query.getIterator())
    {
      List<? extends BusinessEdgeType> results = iterator.getAll();

      return new LinkedList<BusinessEdgeType>(results);
    }
  }

  public List<BusinessEdgeType> getEdgeTypes()
  {
    BusinessEdgeTypeQuery query = new BusinessEdgeTypeQuery(new QueryFactory());
    query.WHERE(query.getParentType().EQ(this.getMdVertex()));
    query.OR(query.getChildType().EQ(this.getMdVertex()));
    query.ORDER_BY_DESC(query.getDisplayLabel().localize());

    try (OIterator<? extends BusinessEdgeType> iterator = query.getIterator())
    {
      List<? extends BusinessEdgeType> results = iterator.getAll();

      return new LinkedList<BusinessEdgeType>(results);
    }
  }

  public static BusinessType getByCode(String code)
  {
    BusinessTypeQuery query = new BusinessTypeQuery(new QueryFactory());
    query.WHERE(query.getCode().EQ(code));

    try (OIterator<? extends BusinessType> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return null;
  }

  public static JsonArray listByOrg()
  {
    JsonArray response = new JsonArray();

    final List<? extends Organization> orgs = Organization.getOrganizations();

    for (Organization org : orgs)
    {
      final boolean isMember = Organization.isMember(org);

      BusinessTypeQuery query = new BusinessTypeQuery(new QueryFactory());
      query.WHERE(query.getOrganization().EQ(org));
      query.ORDER_BY_DESC(query.getDisplayLabel().localize());

      JsonArray types = new JsonArray();

      try (OIterator<? extends BusinessType> it = query.getIterator())
      {
        while (it.hasNext())
        {
          BusinessType type = it.next();

          if (isMember)
          {
            types.add(type.toJSON());
          }
        }
      }

      JsonObject object = new JsonObject();
      object.addProperty("oid", org.getOid());
      object.addProperty("code", org.getCode());
      object.addProperty("label", org.getDisplayLabel().getValue());
      object.addProperty("write", Organization.isRegistryAdmin(org));
      object.add("types", types);

      response.add(object);
    }

    return response;
  }

  public static JsonArray getAll()
  {
    JsonArray response = new JsonArray();

    Organization.getOrganizations().stream().filter(o -> Organization.isMember(o)).forEach(org -> {

      BusinessTypeQuery query = new BusinessTypeQuery(new QueryFactory());
      query.WHERE(query.getOrganization().EQ(org));
      query.ORDER_BY_DESC(query.getDisplayLabel().localize());

      try (OIterator<? extends BusinessType> it = query.getIterator())
      {
        while (it.hasNext())
        {
          BusinessType type = it.next();
          response.add(type.toJSON());
        }
      }
    });

    return response;
  }

  public static boolean isEdgeABusinessType(MdEdgeDAOIF mdEdge)
  {
    return ( BusinessType.getByMdEdge(mdEdge) != null );
  }

  public static BusinessType getByMdEdge(MdEdgeDAOIF mdEdge)
  {
    BusinessTypeQuery query = new BusinessTypeQuery(new QueryFactory());
    query.WHERE(query.getMdEdge().EQ(mdEdge.getOid()));

    try (OIterator<? extends BusinessType> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return null;
  }

  public static BusinessType getByMdVertex(MdVertexDAOIF mdVertex)
  {
    BusinessTypeQuery query = new BusinessTypeQuery(new QueryFactory());
    query.WHERE(query.getMdVertex().EQ(mdVertex.getOid()));

    try (OIterator<? extends BusinessType> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return null;
  }

}
