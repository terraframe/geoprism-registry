package net.geoprism.registry.graph;

import java.util.Date;
import java.util.List;

import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.system.metadata.MdEdge;
import com.runwaysdk.system.metadata.MdVertex;

import net.geoprism.registry.InvalidMasterListException;
import net.geoprism.registry.LabeledPropertyGraphEdge;
import net.geoprism.registry.LabeledPropertyGraphType;
import net.geoprism.registry.LabeledPropertyGraphTypeVersion;
import net.geoprism.registry.LabeledPropertyGraphVertex;
import net.geoprism.registry.model.ServerChildTreeNode;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.ServerGeoObjectService;

public class TreeStrategyPublisher extends AbstractStrategyPublisher implements StrategyPublisher
{
  private TreeStrategyConfiguration configuration;

  public TreeStrategyPublisher(TreeStrategyConfiguration configuration)
  {
    this.configuration = configuration;
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

      List<ServerGeoObjectType> geoObjectTypes = type.getGeoObjectTypes();
      List<ServerHierarchyType> hierarchyTypes = type.getHierarchyTypes();

      ServerGeoObjectService service = new ServerGeoObjectService();
      ServerGeoObjectIF root = service.getGeoObjectByCode(configuration.getCode(), configuration.getTypeCode());

      if (root != null && geoObjectTypes.contains(root.getType()))
      {
        Date forDate = version.getForDate();

        root.setDate(forDate);

        publish(version, geoObjectTypes, hierarchyTypes, root, forDate);

      }
    }
    finally
    {
      version.unlock();
    }
  }

  private VertexObject publish(LabeledPropertyGraphTypeVersion version, List<ServerGeoObjectType> geoObjectTypes, List<ServerHierarchyType> hierarchyTypes, ServerGeoObjectIF root, Date forDate)
  {
    // TODO Check for duplicates
    LabeledPropertyGraphVertex graphVertex = version.getMdVertexForType(root.getType());
    MdVertex mdVertex = graphVertex.getGraphMdVertex();
    
    VertexObject parentVertex = this.publish(root, mdVertex);

    hierarchyTypes.forEach(ht -> {
      ServerChildTreeNode node = root.getChildGeoObjects(ht, null, false, forDate);
      List<ServerChildTreeNode> children = node.getChildren();

      if (children.size() > 0)
      {
        LabeledPropertyGraphEdge graphEdge = version.getMdEdgeForType(ht);
        MdEdge mdEdge = graphEdge.getGraphMdEdge();

        for (ServerChildTreeNode childNode : children)
        {
          ServerGeoObjectIF child = childNode.getGeoObject();
          child.setDate(forDate);

          if (geoObjectTypes.contains(child.getType()))
          {
            VertexObject childVertex = this.publish(version, geoObjectTypes, hierarchyTypes, child, forDate);

            parentVertex.addChild(childVertex, mdEdge.definesType()).apply();
          }
        }
      }

    });

    return parentVertex;
  }

}
