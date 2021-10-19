package net.geoprism.registry;

import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.graph.MdVertexInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeMultiTermDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.cache.DataNotFoundException;
import com.runwaysdk.dataaccess.metadata.MdAttributeConcreteDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.constants.MdGeoVertexInfo;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.metadata.MdAttributeConcrete;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.conversion.AttributeTypeConverter;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.ServiceFactory;
import net.geoprism.registry.view.JsonSerializable;

public class ProgrammaticType extends ProgrammaticTypeBase implements JsonSerializable
{
  private static final long serialVersionUID = 88826735;

  public ProgrammaticType()
  {
    super();
  }

  @Override
  @Transaction
  public void delete()
  {
    MdVertex mdVertex = this.getMdVertex();

    super.delete();

    mdVertex.delete();
  }

  public MdVertexDAOIF getMdVertexDAO()
  {
    return MdVertexDAO.get(this.getMdVertexOid());
  }

  public AttributeType createAttributeType(AttributeType attributeType)
  {
    MdAttributeConcrete mdAttribute = ServerGeoObjectType.createMdAttributeFromAttributeType(this.getMdVertex(), attributeType);

    // Refresh the users session
    if (Session.getCurrentSession() != null)
    {
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
      return !attr.isSystem() && !attr.definesAttribute().equals(ProgrammaticType.SEQ);
    }).map(attr -> converter.build(attr)).collect(Collectors.toMap(AttributeType::getName, attr -> attr));
  }

  public AttributeType getAttribute(String name)
  {
    AttributeTypeConverter converter = new AttributeTypeConverter();

    MdVertexDAOIF mdVertex = this.getMdVertexDAO();
    MdAttributeConcreteDAOIF mdAttribute = (MdAttributeConcreteDAOIF) mdVertex.definesAttribute(name);

    return converter.build(mdAttribute);
  }

  @Override
  public JsonObject toJSON()
  {
    return toJSON(false);
  }

  public JsonObject toJSON(boolean includeAttribute)
  {
    Organization organization = this.getOrganization();

    JsonObject object = new JsonObject();
    object.addProperty(ProgrammaticType.CODE, this.getCode());
    object.addProperty(ProgrammaticType.ORGANIZATION, organization.getCode());
    object.addProperty("organizationLabel", organization.getDisplayLabel().getValue());
    object.add(ProgrammaticType.DISPLAYLABEL, LocalizedValueConverter.convert(this.getDisplayLabel()).toJSON());

    if (this.isAppliedToDB())
    {
      object.addProperty(ProgrammaticType.OID, this.getOid());
    }

    if (includeAttribute)
    {
      Collector<Object, JsonArray, JsonArray> collector = Collector.of(() -> new JsonArray(), (r, t) -> r.add((JsonObject) t), (x1, x2) -> {
        x1.addAll(x2);
        return x1;
      });

      JsonArray attributes = this.getAttributeMap().values().stream().sorted((a, b) -> {
        return a.getName().compareTo(b.getName());
      }).map(a -> a.toJSON()).collect(collector);

      object.add("attributes", attributes);
    }

    return object;
  }

  @Transaction
  public static ProgrammaticType apply(JsonObject object)
  {
    String code = object.get(ProgrammaticType.CODE).getAsString();
    String organizationCode = object.get(ProgrammaticType.ORGANIZATION).getAsString();
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

    ProgrammaticType programmaticType = ( object.has(OID) && !object.get(OID).isJsonNull() ) ? ProgrammaticType.get(object.get(OID).getAsString()) : new ProgrammaticType();
    programmaticType.setCode(code);
    programmaticType.setOrganization(organization);
    LocalizedValueConverter.populate(programmaticType.getDisplayLabel(), localizedValue);

    if (programmaticType.isNew())
    {
      MdVertexDAO mdVertex = MdVertexDAO.newInstance();
      mdVertex.setValue(MdGeoVertexInfo.PACKAGE, RegistryConstants.PROGRAMMATIC_PACKAGE);
      mdVertex.setValue(MdGeoVertexInfo.NAME, code);
      mdVertex.setValue(MdGeoVertexInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
      mdVertex.setValue(MdGeoVertexInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
      LocalizedValueConverter.populate(mdVertex, MdVertexInfo.DISPLAY_LABEL, localizedValue);
      mdVertex.apply();

      // TODO CREATE the edge between this class and GeoVertex??

      programmaticType.setMdVertexId(mdVertex.getOid());
    }

    programmaticType.apply();

    return programmaticType;
  }

  public static ProgrammaticType getByCode(String code)
  {
    ProgrammaticTypeQuery query = new ProgrammaticTypeQuery(new QueryFactory());
    query.WHERE(query.getCode().EQ(code));

    try (OIterator<? extends ProgrammaticType> it = query.getIterator())
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

      ProgrammaticTypeQuery query = new ProgrammaticTypeQuery(new QueryFactory());
      query.WHERE(query.getOrganization().EQ(org));
      query.ORDER_BY_DESC(query.getDisplayLabel().localize());

      JsonArray types = new JsonArray();

      try (OIterator<? extends ProgrammaticType> it = query.getIterator())
      {
        while (it.hasNext())
        {
          ProgrammaticType type = it.next();

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

}
