package net.geoprism.registry;

import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonObject;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeDateTimeInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.graph.MdEdgeInfo;
import com.runwaysdk.dataaccess.DuplicateDataException;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.MdAttributeDateTimeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.dataaccess.metadata.graph.MdVertexDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdEdge;

import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.conversion.ServerHierarchyTypeBuilder;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerElement;
import net.geoprism.registry.model.graph.DirectedAcyclicGraphStrategy;
import net.geoprism.registry.model.graph.GraphStrategy;
import net.geoprism.registry.view.JsonSerializable;

public class DirectedAcyclicGraphType extends DirectedAcyclicGraphTypeBase implements JsonSerializable, GraphType, ServerElement
{
  private static final long serialVersionUID = 1222275153;

  public DirectedAcyclicGraphType()
  {
    super();
  }

  @Override
  protected String buildKey()
  {
    return this.getCode();
  }

  public MdEdgeDAOIF getMdEdgeDAO()
  {
    return MdEdgeDAO.get(this.getMdEdgeOid());
  }

  @Transaction
  public void update(JsonObject object)
  {
    try
    {
      this.appLock();

      if (object.has(DirectedAcyclicGraphType.DISPLAYLABEL))
      {
        LocalizedValue label = LocalizedValue.fromJSON(object.getAsJsonObject(DirectedAcyclicGraphType.DISPLAYLABEL));

        LocalizedValueConverter.populate(this.getDisplayLabel(), label);
      }

      if (object.has(DirectedAcyclicGraphType.DESCRIPTION))
      {
        LocalizedValue description = LocalizedValue.fromJSON(object.getAsJsonObject(DirectedAcyclicGraphType.DESCRIPTION));

        LocalizedValueConverter.populate(this.getDescription(), description);
      }

      this.apply();
    }
    finally
    {
      this.unlock();
    }
  }

  @Override
  @Transaction
  public void delete()
  {
    MdEdge mdEdge = this.getMdEdge();

    super.delete();

    mdEdge.delete();
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = new JsonObject();
    object.addProperty(DirectedAcyclicGraphType.OID, this.getOid());
    object.addProperty(DirectedAcyclicGraphType.CODE, this.getCode());
    object.add(DirectedAcyclicGraphType.DISPLAYLABEL, LocalizedValueConverter.convertNoAutoCoalesce(this.getDisplayLabel()).toJSON());
    object.add(DirectedAcyclicGraphType.DESCRIPTION, LocalizedValueConverter.convertNoAutoCoalesce(this.getDescription()).toJSON());

    return object;
  }

  public GraphStrategy getStrategy()
  {
    return new DirectedAcyclicGraphStrategy(this);
  }

  public static List<DirectedAcyclicGraphType> getAll()
  {
    DirectedAcyclicGraphTypeQuery query = new DirectedAcyclicGraphTypeQuery(new QueryFactory());

    try (OIterator<? extends DirectedAcyclicGraphType> it = query.getIterator())
    {
      return new LinkedList<DirectedAcyclicGraphType>(it.getAll());
    }
  }

  public static DirectedAcyclicGraphType getByCode(String code)
  {
    return DirectedAcyclicGraphType.getByKey(code);
  }

  public static DirectedAcyclicGraphType getByMdEdge(MdEdge mdEdge)
  {
    DirectedAcyclicGraphTypeQuery query = new DirectedAcyclicGraphTypeQuery(new QueryFactory());
    query.WHERE(query.getMdEdge().EQ(mdEdge));

    try (OIterator<? extends DirectedAcyclicGraphType> it = query.getIterator())
    {
      if (it.hasNext())
      {
        return it.next();
      }
    }

    return null;
  }

  public static DirectedAcyclicGraphType create(JsonObject object)
  {
    String code = object.get(DirectedAcyclicGraphType.CODE).getAsString();
    LocalizedValue label = LocalizedValue.fromJSON(object.getAsJsonObject(DirectedAcyclicGraphType.DISPLAYLABEL));
    LocalizedValue description = LocalizedValue.fromJSON(object.getAsJsonObject(DirectedAcyclicGraphType.DESCRIPTION));

    return create(code, label, description);
  }

  @Transaction
  public static DirectedAcyclicGraphType create(String code, LocalizedValue label, LocalizedValue description)
  {
    RoleDAO maintainer = RoleDAO.findRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE).getBusinessDAO();
    RoleDAO consumer = RoleDAO.findRole(RegistryConstants.API_CONSUMER_ROLE).getBusinessDAO();
    RoleDAO contributor = RoleDAO.findRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE).getBusinessDAO();

    try
    {
      MdVertexDAOIF mdBusGeoEntity = MdVertexDAO.getMdVertexDAO(GeoVertex.CLASS);

      MdEdgeDAO mdEdgeDAO = MdEdgeDAO.newInstance();
      mdEdgeDAO.setValue(MdEdgeInfo.PACKAGE, RegistryConstants.UNIVERSAL_GRAPH_PACKAGE);
      mdEdgeDAO.setValue(MdEdgeInfo.NAME, code);
      mdEdgeDAO.setValue(MdEdgeInfo.PARENT_MD_VERTEX, mdBusGeoEntity.getOid());
      mdEdgeDAO.setValue(MdEdgeInfo.CHILD_MD_VERTEX, mdBusGeoEntity.getOid());
      LocalizedValueConverter.populate(mdEdgeDAO, MdEdgeInfo.DISPLAY_LABEL, label);
      LocalizedValueConverter.populate(mdEdgeDAO, MdEdgeInfo.DESCRIPTION, description);
      mdEdgeDAO.setValue(MdEdgeInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
      mdEdgeDAO.apply();

      MdAttributeDateTimeDAO startDate = MdAttributeDateTimeDAO.newInstance();
      startDate.setValue(MdAttributeDateTimeInfo.NAME, GeoVertex.START_DATE);
      startDate.setStructValue(MdAttributeDateTimeInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, "Start Date");
      startDate.setStructValue(MdAttributeDateTimeInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, "Start Date");
      startDate.setValue(MdAttributeDateTimeInfo.DEFINING_MD_CLASS, mdEdgeDAO.getOid());
      startDate.apply();

      MdAttributeDateTimeDAO endDate = MdAttributeDateTimeDAO.newInstance();
      endDate.setValue(MdAttributeDateTimeInfo.NAME, GeoVertex.END_DATE);
      endDate.setStructValue(MdAttributeDateTimeInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, "End Date");
      endDate.setStructValue(MdAttributeDateTimeInfo.DESCRIPTION, MdAttributeLocalInfo.DEFAULT_LOCALE, "End Date");
      endDate.setValue(MdAttributeDateTimeInfo.DEFINING_MD_CLASS, mdEdgeDAO.getOid());
      endDate.apply();

      ServerHierarchyTypeBuilder permissionBuilder = new ServerHierarchyTypeBuilder();
      permissionBuilder.grantWritePermissionsOnMdTermRel(mdEdgeDAO);
      permissionBuilder.grantWritePermissionsOnMdTermRel(maintainer, mdEdgeDAO);
      permissionBuilder.grantReadPermissionsOnMdTermRel(consumer, mdEdgeDAO);
      permissionBuilder.grantReadPermissionsOnMdTermRel(contributor, mdEdgeDAO);

      DirectedAcyclicGraphType graphType = new DirectedAcyclicGraphType();
      graphType.setCode(code);
      graphType.setMdEdgeId(mdEdgeDAO.getOid());
      LocalizedValueConverter.populate(graphType.getDisplayLabel(), label);
      LocalizedValueConverter.populate(graphType.getDescription(), description);
      graphType.apply();

      return graphType;
    }
    catch (DuplicateDataException ex)
    {
      DuplicateHierarchyTypeException ex2 = new DuplicateHierarchyTypeException();
      ex2.setDuplicateValue(code);
      throw ex2;
    }
  }
}
