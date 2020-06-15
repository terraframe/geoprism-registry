package net.geoprism.registry.graph;

import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.GraphObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;

import net.geoprism.registry.Organization;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.view.JsonSerializable;

public abstract class ExternalSystem extends ExternalSystemBase implements JsonSerializable
{
  private static final long serialVersionUID = 1516759164;

  public ExternalSystem()
  {
    super();
  }

  protected void populate(JsonObject json)
  {
    this.setId(json.get(ExternalSystem.ID).getAsString());

    if (json.has(ExternalSystem.ORGANIZATION))
    {
      this.setOrganizationId(json.get(ExternalSystem.ORGANIZATION).getAsString());
    }

    LocalizedValue label = LocalizedValue.fromJSON(json.get(ExternalSystem.LABEL).getAsJsonObject());
    LocalizedValue description = LocalizedValue.fromJSON(json.get(ExternalSystem.DESCRIPTION).getAsJsonObject());

    LocalizedValueConverter.populate(this, ExternalSystem.LABEL, label);
    LocalizedValueConverter.populate(this, ExternalSystem.DESCRIPTION, description);
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = new JsonObject();
    object.addProperty("type", this.getMdClass().getTypeName());
    object.addProperty(ExternalSystem.OID, this.getOid());
    object.addProperty(ExternalSystem.ID, this.getId());
    object.addProperty(ExternalSystem.ORGANIZATION, this.getOrganizationOid());
    object.add(ExternalSystem.LABEL, LocalizedValueConverter.convert((GraphObject) this.getLabel()).toJSON());
    object.add(ExternalSystem.DESCRIPTION, LocalizedValueConverter.convert((GraphObject) this.getDescription()).toJSON());

    return object;
  }

  public static List<ExternalSystem> getExternalSystemsForOrg(Organization organization, Integer pageNumber, Integer pageSize)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(ExternalSystem.CLASS);
    MdAttributeDAOIF oAttribute = mdVertex.definesAttribute(ExternalSystem.ORGANIZATION);

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT FROM " + mdVertex.getDBClassName());
    builder.append(" WHERE " + oAttribute.getColumnName() + " = :organization");
    builder.append(" ORDER BY id");
    builder.append(" SKIP " + ( ( pageNumber - 1 ) * pageSize ) + " LIMIT " + pageSize);

    final GraphQuery<ExternalSystem> query = new GraphQuery<ExternalSystem>(builder.toString());
    query.setParameter("organization", organization.getOid());

    return query.getResults();
  }

  public static long getCount(Organization organization)
  {
    final MdVertexDAOIF mdVertex = MdVertexDAO.getMdVertexDAO(ExternalSystem.CLASS);
    MdAttributeDAOIF oAttribute = mdVertex.definesAttribute(ExternalSystem.ORGANIZATION);

    StringBuilder builder = new StringBuilder();
    builder.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName());
    builder.append(" WHERE " + oAttribute.getColumnName() + " = :organization");

    final GraphQuery<Long> query = new GraphQuery<Long>(builder.toString());
    query.setParameter("organization", organization.getOid());

    return query.getSingleResult();
  }

  public static ExternalSystem desieralize(JsonObject json)
  {
    String type = json.get("type").getAsString();
    String oid = json.get(ExternalSystem.OID).getAsString();

    ExternalSystem system = getExternalSystem(oid, type);
    system.populate(json);

    return system;
  }

  private static ExternalSystem getExternalSystem(String oid, String type)
  {
    if (oid != null && oid.length() > 0)
    {
      return ExternalSystem.get(oid);
    }

    if (type.equals(RevealExternalSystem.class.getSimpleName()))
    {
      return new RevealExternalSystem();
    }

    return new DHIS2ExternalSystem();
  }
}
