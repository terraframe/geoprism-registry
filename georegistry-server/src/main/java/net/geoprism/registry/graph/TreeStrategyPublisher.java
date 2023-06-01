package net.geoprism.registry.graph;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.graph.AbstractGraphVersionPublisher;
import net.geoprism.graph.GeoObjectTypeSnapshot;
import net.geoprism.graph.HierarchyTypeSnapshot;
import net.geoprism.graph.LabeledPropertyGraphType;
import net.geoprism.graph.LabeledPropertyGraphTypeVersion;
import net.geoprism.graph.StrategyPublisher;
import net.geoprism.graph.TreeStrategyConfiguration;
import net.geoprism.registry.InvalidMasterListException;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.model.ServerChildTreeNode;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.ServerGeoObjectService;

public class TreeStrategyPublisher extends AbstractGraphVersionPublisher implements StrategyPublisher
{
  private static class Snapshot
  {
    private ServerGeoObjectIF node;

    private MdEdge            hierarchy;

    private VertexObject      parent;

    public Snapshot(ServerGeoObjectIF node, MdEdge hierarchy, VertexObject parent)
    {
      super();
      this.node = node;
      this.hierarchy = hierarchy;
      this.parent = parent;
    }

    public Snapshot(ServerGeoObjectIF node)
    {
      this(node, null, null);
    }

  }

  private TreeStrategyConfiguration configuration;

  private Set<String>               uids;

  private Stack<Snapshot>           stack;

  public TreeStrategyPublisher(TreeStrategyConfiguration configuration)
  {
    this.configuration = configuration;

    this.uids = new TreeSet<String>();
    this.stack = new Stack<Snapshot>();
  }

  @Override
  public void publish(LabeledPropertyGraphTypeVersion version)
  {
    version.lock();

    try
    {
      LabeledPropertyGraphType type = version.getGraphType();

      if (!type.isValid())
      {
        throw new InvalidMasterListException();
      }

      ServerHierarchyType hierarchyType = ServerHierarchyType.get(type.getHierarchy());
      List<ServerGeoObjectType> geoObjectTypes = hierarchyType.getAllTypes(false);

      geoObjectTypes.stream().filter(t -> t.getIsAbstract()).collect(Collectors.toList()).forEach(t -> {
        geoObjectTypes.addAll(t.getSubtypes());
      });

      Date forDate = version.getForDate();

      ServerGeoObjectService service = new ServerGeoObjectService();
      ServerGeoObjectIF root = service.getGeoObjectByCode(configuration.getCode(), configuration.getTypeCode());

      if (root != null && geoObjectTypes.contains(root.getType()))
      {
        root.setDate(forDate);

        if (!root.getInvalid() && root.getExists(forDate))
        {
          stack.push(new Snapshot(root));
        }
      }

      while (!stack.isEmpty())
      {
        Snapshot snapshot = stack.pop();

        GeoObjectTypeSnapshot graphVertex = GeoObjectTypeSnapshot.get(version, snapshot.node.getType().getCode());
        MdVertex mdVertex = graphVertex.getGraphMdVertex();

        VertexObject vertex = null;

        if (!this.uids.contains(snapshot.node.getUid()))
        {
          vertex = this.publish(mdVertex, snapshot.node.toGeoObject(forDate));

          final VertexObject parent = vertex;

          ServerChildTreeNode node = snapshot.node.getChildGeoObjects(hierarchyType, null, false, forDate);
          List<ServerChildTreeNode> children = node.getChildren();

          if (children.size() > 0)
          {
            HierarchyTypeSnapshot graphEdge = HierarchyTypeSnapshot.get(version, hierarchyType.getCode());
            MdEdge mdEdge = graphEdge.getGraphMdEdge();

            for (ServerChildTreeNode childNode : children)
            {
              ServerGeoObjectIF child = childNode.getGeoObject();
              child.setDate(forDate);

              if (child.getExists(forDate) && !child.getInvalid() && geoObjectTypes.contains(child.getType()))
              {
                stack.push(new Snapshot(child, mdEdge, parent));
              }
            }
          }

          this.uids.add(snapshot.node.getUid());
        }
        else
        {
          vertex = this.get(mdVertex, snapshot.node.getUid());
        }

        if (snapshot.parent != null && snapshot.hierarchy != null)
        {

          snapshot.parent.addChild(vertex, snapshot.hierarchy.definesType()).apply();
        }
      }
    }
    finally
    {
      version.unlock();
    }
  }

  private VertexObject get(MdVertex mdVertex, String uid)
  {
    MdVertexDAOIF mdVertexDAO = (MdVertexDAOIF) BusinessFacade.getEntityDAO(mdVertex);
    MdAttributeDAOIF attribute = mdVertexDAO.definesAttribute(RegistryConstants.UUID);

    StringBuffer statement = new StringBuffer();
    statement.append("SELECT FROM " + mdVertex.getDbClassName());
    statement.append(" WHERE " + attribute.getColumnName() + " = :uid");

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
    query.setParameter("uid", uid);

    return query.getSingleResult();
  }

}
