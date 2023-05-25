package net.geoprism.registry;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.graph.MdEdgeInfo;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdGraphClassQuery;

import net.geoprism.registry.conversion.AttributeTypeConverter;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.model.ServerHierarchyType;

public class HierarchyTypeSnapshot extends HierarchyTypeSnapshotBase
{
  public static final String PREFIX           = "g_";

  public static final String SPLIT            = "__";

  @SuppressWarnings("unused")
  private static final long  serialVersionUID = 2091038362;

  public HierarchyTypeSnapshot()
  {
    super();
  }

  @Override
  public void delete()
  {
    String mdEdgeOid = this.getGraphMdEdgeOid();

    super.delete();

    MdEdgeDAO.get(mdEdgeOid).getBusinessDAO().delete();
  }

  public JsonObject toJSON(GeoObjectTypeSnapshot root)
  {
    JsonArray nodes = new JsonArray();

    try (OIterator<? extends GeoObjectTypeSnapshot> it = root.getAllChildSnapshot())
    {
      it.forEach(snapshot -> {
        nodes.add(this.toNode(snapshot));
      });
    }

    JsonObject hierarchyObject = new JsonObject();
    hierarchyObject.addProperty(CODE, this.getCode());
    hierarchyObject.add(DISPLAYLABEL, AttributeTypeConverter.convertNoAutoCoalesce(this.getDisplayLabel()).toJSON());
    hierarchyObject.add(DESCRIPTION, AttributeTypeConverter.convertNoAutoCoalesce(this.getDescription()).toJSON());
    hierarchyObject.add("nodes", nodes);

    return hierarchyObject;
  }

  private JsonObject toNode(GeoObjectTypeSnapshot snapshot)
  {
    JsonArray children = new JsonArray();

    try (OIterator<? extends GeoObjectTypeSnapshot> it = snapshot.getAllChildSnapshot())
    {
      it.forEach(child -> {
        children.add(this.toNode(child));
      });
    }

    JsonObject node = new JsonObject();
    node.addProperty(GeoObjectTypeSnapshot.CODE, snapshot.getCode());
    node.add("nodes", children);

    return node;
  }

  private static String getTableName(String className)
  {
    int count = 0;

    String name = PREFIX + count + SPLIT + className;

    if (name.length() > 25)
    {
      name = name.substring(0, 25);
    }

    while (isTableNameInUse(name))
    {
      count++;

      name = PREFIX + count + className;

      if (name.length() > 25)
      {
        name = name.substring(0, 25);
      }
    }

    return name;
  }

  private static boolean isTableNameInUse(String name)
  {
    MdGraphClassQuery query = new MdGraphClassQuery(new QueryFactory());
    query.WHERE(query.getDbClassName().EQ(name));

    return query.getCount() > 0;
  }

  private static String getEdgeName(ServerHierarchyType type)
  {

    MdEdgeDAOIF mdEdge = type.getMdEdgeDAO();

    String className = mdEdge.getDBClassName();

    return getTableName(className);
  }

  public static HierarchyTypeSnapshot create(LabeledPropertyGraphTypeVersion version, ServerHierarchyType type, GeoObjectTypeSnapshot root)
  {
    String viewName = getEdgeName(type);

    MdEdgeDAO mdEdgeDAO = MdEdgeDAO.newInstance();
    mdEdgeDAO.setValue(MdEdgeInfo.PACKAGE, RegistryConstants.UNIVERSAL_GRAPH_PACKAGE);
    mdEdgeDAO.setValue(MdEdgeInfo.NAME, viewName);
    mdEdgeDAO.setValue(MdEdgeInfo.DB_CLASS_NAME, viewName);
    mdEdgeDAO.setValue(MdEdgeInfo.PARENT_MD_VERTEX, root.getGraphMdVertexOid());
    mdEdgeDAO.setValue(MdEdgeInfo.CHILD_MD_VERTEX, root.getGraphMdVertexOid());
    LocalizedValueConverter.populate(mdEdgeDAO, MdEdgeInfo.DISPLAY_LABEL, type.getLabel());
    LocalizedValueConverter.populate(mdEdgeDAO, MdEdgeInfo.DESCRIPTION, LocalizedValueConverter.convertNoAutoCoalesce(type.getDescription()));
    mdEdgeDAO.setValue(MdEdgeInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
    mdEdgeDAO.apply();

    MdEdge mdEdge = (MdEdge) BusinessFacade.get(mdEdgeDAO);

    LabeledPropertyGraphTypeVersion.assignDefaultRolePermissions(mdEdge);

    HierarchyTypeSnapshot snapshot = new HierarchyTypeSnapshot();
    snapshot.setVersion(version);
    snapshot.setGraphMdEdge(mdEdge);
    snapshot.setCode(type.getCode());
    LocalizedValueConverter.populate(snapshot.getDisplayLabel(), type.getLabel());
    LocalizedValueConverter.populate(snapshot.getDescription(), type.getDescription());
    snapshot.apply();

    return snapshot;
  }

  public static HierarchyTypeSnapshot create(LabeledPropertyGraphTypeVersion version, JsonObject type, GeoObjectTypeSnapshot root)
  {
    String code = type.get(CODE).getAsString();
    String viewName = getTableName(code);
    LocalizedValue label = LocalizedValue.fromJSON(type.get(DISPLAYLABEL).getAsJsonObject());
    LocalizedValue description = LocalizedValue.fromJSON(type.get(DESCRIPTION).getAsJsonObject());

    MdEdgeDAO mdEdgeDAO = MdEdgeDAO.newInstance();
    mdEdgeDAO.setValue(MdEdgeInfo.PACKAGE, RegistryConstants.UNIVERSAL_GRAPH_PACKAGE);
    mdEdgeDAO.setValue(MdEdgeInfo.NAME, viewName);
    mdEdgeDAO.setValue(MdEdgeInfo.DB_CLASS_NAME, viewName);
    mdEdgeDAO.setValue(MdEdgeInfo.PARENT_MD_VERTEX, root.getGraphMdVertexOid());
    mdEdgeDAO.setValue(MdEdgeInfo.CHILD_MD_VERTEX, root.getGraphMdVertexOid());
    LocalizedValueConverter.populate(mdEdgeDAO, MdEdgeInfo.DISPLAY_LABEL, label);
    LocalizedValueConverter.populate(mdEdgeDAO, MdEdgeInfo.DESCRIPTION, description);
    mdEdgeDAO.setValue(MdEdgeInfo.ENABLE_CHANGE_OVER_TIME, MdAttributeBooleanInfo.FALSE);
    mdEdgeDAO.apply();

    MdEdge mdEdge = (MdEdge) BusinessFacade.get(mdEdgeDAO);

    LabeledPropertyGraphTypeVersion.assignDefaultRolePermissions(mdEdge);

    HierarchyTypeSnapshot snapshot = new HierarchyTypeSnapshot();
    snapshot.setVersion(version);
    snapshot.setGraphMdEdge(mdEdge);
    snapshot.setCode(code);
    LocalizedValueConverter.populate(snapshot.getDisplayLabel(), label);
    LocalizedValueConverter.populate(snapshot.getDescription(), description);
    snapshot.apply();

    // Assign the relationship information
    createHierarchyRelationship(version, type, root);

    return snapshot;

  }

  private static void createHierarchyRelationship(LabeledPropertyGraphTypeVersion version, JsonObject type, GeoObjectTypeSnapshot parent)
  {
    type.get("nodes").getAsJsonArray().forEach(node -> {
      JsonObject object = node.getAsJsonObject();
      String code = object.get(CODE).getAsString();

      GeoObjectTypeSnapshot child = GeoObjectTypeSnapshot.get(version, code);

      parent.addChildSnapshot(child).apply();

      createHierarchyRelationship(version, object, child);
    });
  }

}
