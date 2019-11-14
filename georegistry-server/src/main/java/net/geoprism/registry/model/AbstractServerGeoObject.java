package net.geoprism.registry.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.ontology.TermAndRel;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.ontology.TermUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.AttributeHierarchy;
import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.ServerHierarchyTypeBuilder;
import net.geoprism.registry.service.ConversionService;
import net.geoprism.registry.service.ServerGeoObjectService;

public abstract class AbstractServerGeoObject implements ServerGeoObjectIF
{
  private ServerGeoObjectType type;

  private Business            business;

  public AbstractServerGeoObject(ServerGeoObjectType type, Business business)
  {
    this.type = type;
    this.business = business;
  }

  public ServerGeoObjectType getType()
  {
    return type;
  }

  public void setType(ServerGeoObjectType type)
  {
    this.type = type;
  }

  public Business getBusiness()
  {
    return business;
  }

  public void setBusiness(Business business)
  {
    this.business = business;
  }

  @Override
  public String getUid()
  {
    return this.getBusiness().getValue(RegistryConstants.UUID);
  }

  @Override
  public void setUid(String uid)
  {
    this.getBusiness().setValue(RegistryConstants.UUID, uid);
  }

  @Override
  public void setStatus(GeoObjectStatus status)
  {
    this.getBusiness().setValue(DefaultAttribute.STATUS.getName(), status.getOid());
  }

  @Override
  public String getValue(String attributeName)
  {
    return this.getBusiness().getValue(attributeName);
  }

  @Override
  public void setValue(String attributeName, Object value)
  {
    this.getBusiness().setValue(attributeName, value);
  }

  @SuppressWarnings("unchecked")
  public void populate(GeoObject geoObject)
  {
    GeoObjectStatus gos = this.business.isNew() ? GeoObjectStatus.PENDING : ConversionService.getInstance().termToGeoObjectStatus(geoObject.getStatus());

    Map<String, AttributeType> attributes = geoObject.getType().getAttributeMap();
    attributes.forEach((attributeName, attribute) -> {
      if (attributeName.equals(DefaultAttribute.STATUS.getName()) || attributeName.equals(DefaultAttribute.DISPLAY_LABEL.getName()) || attributeName.equals(DefaultAttribute.CODE.getName()) || attributeName.equals(DefaultAttribute.UID.getName()))
      {
        // Ignore the attributes
      }
      else if (this.business.hasAttribute(attributeName) && !this.business.getMdAttributeDAO(attributeName).isSystem())
      {
        if (attribute instanceof AttributeTermType)
        {
          Iterator<String> it = (Iterator<String>) geoObject.getValue(attributeName);

          if (it.hasNext())
          {
            String code = it.next();

            String classifierKey = Classifier.buildKey(RegistryConstants.REGISTRY_PACKAGE, code);
            Classifier classifier = Classifier.getByKey(classifierKey);

            this.business.setValue(attributeName, classifier.getOid());
          }
          else
          {
            this.business.setValue(attributeName, (String) null);
          }
        }
        else
        {
          Object value = geoObject.getValue(attributeName);

          if (value != null)
          {
            this.business.setValue(attributeName, value);
          }
          else
          {
            this.business.setValue(attributeName, (String) null);
          }
        }
      }
    });

    this.setUid(geoObject.getUid());
    this.setCode(geoObject.getCode());
    this.setStatus(gos);
    this.setLabel(geoObject.getDisplayLabel());
    this.setGeometry(geoObject.getGeometry());
  }

  public Map<String, ServerHierarchyType> getHierarchyTypeMap(String[] relationshipTypes)
  {
    Map<String, ServerHierarchyType> map = new HashMap<String, ServerHierarchyType>();

    for (String relationshipType : relationshipTypes)
    {
      MdTermRelationship mdRel = (MdTermRelationship) MdTermRelationship.getMdRelationship(relationshipType);

      ServerHierarchyType ht = new ServerHierarchyTypeBuilder().get(mdRel);

      map.put(relationshipType, ht);
    }

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

  protected static ParentTreeNode internalGetParentGeoObjects(ServerGeoObjectIF child, String[] parentTypes, boolean recursive, ServerHierarchyType htIn)
  {
    ServerGeoObjectService service = new ServerGeoObjectService();

    ParentTreeNode tnRoot = new ParentTreeNode(child.toGeoObject(), htIn != null ? htIn.getType() : null);

    if (child.getType().isLeaf())
    {
      List<MdAttributeDAOIF> mdAttributes = child.getMdAttributeDAOs().stream().filter(mdAttribute -> {
        if (mdAttribute instanceof MdAttributeReferenceDAOIF)
        {
          MdBusinessDAOIF referenceMdBusiness = ( (MdAttributeReferenceDAOIF) mdAttribute ).getReferenceMdBusinessDAO();

          if (referenceMdBusiness.definesType().equals(GeoEntity.CLASS))
          {
            return true;
          }
        }

        return false;
      }).collect(Collectors.toList());

      mdAttributes.forEach(mdAttribute -> {

        String parentRunwayId = child.getValue(mdAttribute.definesAttribute());

        if (parentRunwayId != null && parentRunwayId.length() > 0)
        {
          GeoEntity geParent = GeoEntity.get(parentRunwayId);
          ServerGeoObjectIF parent = service.build(geParent);
          Universal uni = parent.getType().getUniversal();

          if (parentTypes == null || parentTypes.length == 0 || ArrayUtils.contains(parentTypes, uni.getKey()))
          {
            ParentTreeNode tnParent;

            ServerHierarchyType ht = AttributeHierarchy.getHierarchyType(mdAttribute.getKey());

            if (recursive)
            {
              tnParent = AbstractServerGeoObject.internalGetParentGeoObjects(parent, parentTypes, recursive, ht);
            }
            else
            {
              tnParent = new ParentTreeNode(parent.toGeoObject(), ht.getType());
            }

            tnRoot.addParent(tnParent);
          }
        }
      });

    }
    else
    {
      String[] relationshipTypes = TermUtil.getAllChildRelationships(child.getRunwayId());

      Map<String, ServerHierarchyType> htMap = child.getHierarchyTypeMap(relationshipTypes);

      TermAndRel[] tnrParents = TermUtil.getDirectAncestors(child.getRunwayId(), relationshipTypes);
      for (TermAndRel tnrParent : tnrParents)
      {
        GeoEntity geParent = (GeoEntity) tnrParent.getTerm();
        Universal uni = geParent.getUniversal();

        if (!geParent.getOid().equals(GeoEntity.getRoot().getOid()) && ( parentTypes == null || parentTypes.length == 0 || ArrayUtils.contains(parentTypes, uni.getKey()) ))
        {
          ServerGeoObjectIF parent = service.build(geParent);
          ServerHierarchyType ht = htMap.get(tnrParent.getRelationshipType());

          ParentTreeNode tnParent;
          if (recursive)
          {
            tnParent = AbstractServerGeoObject.internalGetParentGeoObjects(parent, parentTypes, recursive, ht);
          }
          else
          {
            tnParent = new ParentTreeNode(parent.toGeoObject(), ht.getType());
          }

          tnRoot.addParent(tnParent);
        }
      }
    }

    return tnRoot;
  }

}
