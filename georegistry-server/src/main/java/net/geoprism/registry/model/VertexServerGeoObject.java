package net.geoprism.registry.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import net.geoprism.dashboard.GeometryUpdateException;
import net.geoprism.ontology.Classifier;
import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.GeometryTypeException;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.service.ConversionService;

public class VertexServerGeoObject implements ServerGeoObjectIF
{
  private ServerGeoObjectType type;

  private GeoObject           geoObject;

  private VertexObject        vertex;

  public VertexServerGeoObject(ServerGeoObjectType type, GeoObject go, VertexObject vertex)
  {
    this.type = type;
    this.vertex = vertex;
    this.geoObject = go;
  }

  public ServerGeoObjectType getType()
  {
    return type;
  }

  public void setType(ServerGeoObjectType type)
  {
    this.type = type;
  }

  public GeoObject getGeoObject()
  {
    return geoObject;
  }

  public void setGeoObject(GeoObject geoObject)
  {
    this.geoObject = geoObject;
  }

  public VertexObject getVertex()
  {
    return vertex;
  }

  public void setVertex(VertexObject vertex)
  {
    this.vertex = vertex;
  }

  @SuppressWarnings("unchecked")
  protected Term populateVertex(String statusCode)
  {
    GeoObjectStatus gos = this.vertex.isNew() ? GeoObjectStatus.PENDING : ConversionService.getInstance().termToGeoObjectStatus(geoObject.getStatus());

    if (statusCode != null)
    {
      gos = ConversionService.getInstance().termToGeoObjectStatus(statusCode);
    }

    this.vertex.setValue(RegistryConstants.UUID, geoObject.getUid());
    this.vertex.setValue(GeoVertex.GEOID, geoObject.getUid());
    this.vertex.setValue(DefaultAttribute.CODE.getName(), geoObject.getCode());
    this.vertex.setValue(DefaultAttribute.STATUS.getName(), gos.getOid());
    LocalizedValueConverter.populate(this.vertex.getEmbeddedComponent(DefaultAttribute.DISPLAY_LABEL.getName()), geoObject.getDisplayLabel());

    Geometry geom = this.getGeoObject().getGeometry();

    if (geom != null)
    {
      if (!this.isValidGeometry(geom))
      {
        GeometryTypeException ex = new GeometryTypeException();
        ex.setActualType(geom.getGeometryType());
        ex.setExpectedType(this.getGeoObject().getGeometryType().name());

        throw ex;
      }

      // Populate the correct geom field
      String geometryAttribute = this.getGeometryAttributeName();

      this.getVertex().setValue(geometryAttribute, geom);
    }

    Map<String, AttributeType> attributes = geoObject.getType().getAttributeMap();
    attributes.forEach((attributeName, attribute) -> {
      if (attributeName.equals(DefaultAttribute.STATUS.getName()) || attributeName.equals(DefaultAttribute.DISPLAY_LABEL.getName()) || attributeName.equals(DefaultAttribute.CODE.getName()) || attributeName.equals(DefaultAttribute.UID.getName()))
      {
        // Ignore the attributes
      }
      else if (this.vertex.hasAttribute(attributeName) && !this.vertex.getMdAttributeDAO(attributeName).isSystem())
      {
        if (attribute instanceof AttributeTermType)
        {
          Iterator<String> it = (Iterator<String>) geoObject.getValue(attributeName);

          if (it.hasNext())
          {
            String code = it.next();

            String classifierKey = Classifier.buildKey(RegistryConstants.REGISTRY_PACKAGE, code);
            Classifier classifier = Classifier.getByKey(classifierKey);

            this.vertex.setValue(attributeName, classifier.getOid());
          }
          else
          {
            this.vertex.setValue(attributeName, (String) null);
          }
        }
        else
        {
          Object value = geoObject.getValue(attributeName);

          if (value != null)
          {
            this.vertex.setValue(attributeName, value);
          }
          else
          {
            this.vertex.setValue(attributeName, (String) null);
          }
        }
      }
    });

    return ConversionService.getInstance().geoObjectStatusToTerm(gos);
  }

  private String getGeometryAttributeName()
  {
    GeometryType geometryType = this.type.getGeometryType();

    if (geometryType.equals(GeometryType.LINE))
    {
      return GeoVertex.GEOLINE;
    }
    else if (geometryType.equals(GeometryType.MULTILINE))
    {
      return GeoVertex.GEOMULTILINE;
    }
    else if (geometryType.equals(GeometryType.POINT))
    {
      return GeoVertex.GEOPOINT;
    }
    else if (geometryType.equals(GeometryType.MULTIPOINT))
    {
      return GeoVertex.GEOMULTIPOINT;
    }
    else if (geometryType.equals(GeometryType.POLYGON))
    {
      return GeoVertex.GEOPOLYGON;
    }
    else if (geometryType.equals(GeometryType.MULTIPOLYGON))
    {
      return GeoVertex.GEOMULTIPOLYGON;
    }

    throw new UnsupportedOperationException("Unsupported geometry type [" + geometryType.name() + "]");
  }

  public Map<String, ServerHierarchyType> getHierarchyTypeMap(String[] relationshipTypes)
  {
    Map<String, ServerHierarchyType> map = new HashMap<String, ServerHierarchyType>();

    // TODO
    // for (String relationshipType : relationshipTypes)
    // {
    // MdEdgeDAOIF mdRel = MdEdgeDAO.getMdEdgeDAO(relationshipType);
    //
    // ServerHierarchyType ht = new ServerHierarchyTypeBuilder().get(mdRel);
    //
    // map.put(relationshipType, ht);
    // }

    return map;
  }

  protected boolean isValidGeometry(Geometry geometry)
  {
    if (geometry != null)
    {
      GeometryType type = this.type.getGeometryType();

      if (type.equals(GeometryType.LINE) && ! ( geometry instanceof LineString ))
      {
        return false;
      }
      else if (type.equals(GeometryType.MULTILINE) && ! ( geometry instanceof MultiLineString ))
      {
        return false;
      }
      else if (type.equals(GeometryType.POINT) && ! ( geometry instanceof Point ))
      {
        return false;
      }
      else if (type.equals(GeometryType.MULTIPOINT) && ! ( geometry instanceof MultiPoint ))
      {
        return false;
      }
      else if (type.equals(GeometryType.POLYGON) && ! ( geometry instanceof Polygon ))
      {
        return false;
      }
      else if (type.equals(GeometryType.MULTIPOLYGON) && ! ( geometry instanceof MultiPolygon ))
      {
        return false;
      }

      return true;
    }

    return true;
  }

  @Override
  public String getCode()
  {
    return (String) this.vertex.getObjectValue(DefaultAttribute.CODE.getName());
  }

  @Override
  public String getUid()
  {
    return (String) this.vertex.getObjectValue(DefaultAttribute.UID.getName());
  }

  @Override
  public String getRunwayId()
  {
    return this.vertex.getOid();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<? extends MdAttributeConcreteDAOIF> getMdAttributeDAOs()
  {
    return (List<? extends MdAttributeConcreteDAOIF>) this.vertex.getMdAttributeDAOs();
  }

  @Override
  public String getValue(String attributeName)
  {
    return this.vertex.getValue(attributeName);
  }

  @Override
  public void apply(String statusCode, boolean isImport)
  {
    if (!isImport && !this.vertex.isNew() && !this.getGeoObject().getType().isGeometryEditable() && this.vertex.isModified(this.getGeometryAttributeName()))
    {
      throw new GeometryUpdateException();
    }

    Term status = this.populateVertex(statusCode);
    this.getVertex().apply();

    /*
     * Update the returned GeoObject
     */
    this.getGeoObject().setStatus(status);
  }

  @Override
  public String bbox()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Transaction
  public void removeChild(ServerGeoObjectIF child, String hierarchyCode)
  {
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(hierarchyCode);
    child.removeParent(this, hierarchyType);
  }

  @Transaction
  @Override
  public ParentTreeNode addChild(ServerGeoObjectIF child, String hierarchyCode)
  {
    ServerHierarchyType hierarchyType = ServerHierarchyType.get(hierarchyCode);
    return child.addParent(this, hierarchyType);
  }

  @Override
  public ChildTreeNode getChildGeoObjects(String[] childrenTypes, Boolean recursive)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ParentTreeNode getParentGeoObjects(String[] parentTypes, Boolean recursive)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ParentTreeNode addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    if (!hierarchyType.getUniversalType().equals(AllowedIn.CLASS))
    {
      GeoEntity.validateUniversalRelationship(this.getType().getUniversal(), parent.getType().getUniversal(), hierarchyType.getUniversalType());
    }

    this.getVertex().addParent( ( (VertexServerGeoObject) parent ).getVertex(), hierarchyType.getMdEdge()).apply();

    ParentTreeNode node = new ParentTreeNode(this.getGeoObject(), hierarchyType.getType());
    node.addParent(new ParentTreeNode(parent.getGeoObject(), hierarchyType.getType()));

    return node;
  }

  @Override
  public void removeParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    this.getVertex().removeParent( ( (VertexServerGeoObject) parent ).getVertex(), hierarchyType.getMdEdge());
  }
}
