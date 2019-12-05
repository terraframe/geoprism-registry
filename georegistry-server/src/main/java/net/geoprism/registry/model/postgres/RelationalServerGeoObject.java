package net.geoprism.registry.model.postgres;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ValueOverTimeCollectionDTO;
import org.commongeoregistry.adapter.dataaccess.ValueOverTimeDTO;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.ontology.TermAndRel;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;
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
import net.geoprism.registry.io.GeoObjectUtil;
import net.geoprism.registry.model.AbstractServerGeoObject;
import net.geoprism.registry.model.LocationInfo;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.service.ConversionService;
import net.geoprism.registry.service.ServerGeoObjectService;

public abstract class RelationalServerGeoObject extends AbstractServerGeoObject implements ServerGeoObjectIF
{
  private ServerGeoObjectType type;

  private Business            business;

  public RelationalServerGeoObject(ServerGeoObjectType type, Business business)
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
  public void setStatus(GeoObjectStatus status, Date startDate, Date endDate)
  {
    this.setStatus(status);
  }

  @Override
  public GeoObjectStatus getStatus()
  {
    String value = this.getBusiness().getValue(DefaultAttribute.STATUS.getName());

    return GeoObjectStatus.get(value);
  }

  @Override
  public void setDisplayLabel(LocalizedValue value, Date startDate, Date endDate)
  {
    this.setDisplayLabel(value);
  }

  @Override
  public void setGeometry(Geometry geometry, Date startDate, Date endDate)
  {
    this.setGeometry(geometry);
  }

  @Override
  public void setValue(String attributeName, Object value, Date startDate, Date endDate)
  {
    this.setValue(attributeName, value);
  }
  
  @Override
  public Object getValue(String attributeName, Date date)
  {
    return this.getValue(attributeName);
  }

  @Override
  public ValueOverTimeCollection getValuesOverTime(String attributeName)
  {
    throw new UnsupportedOperationException("Value over time operations are only supported on Vertex GeoObjects.");
  }
  
  @Override
  public void setValuesOverTime(String attributeName, ValueOverTimeCollection collection)
  {
    throw new UnsupportedOperationException("Value over time operations are only supported on Vertex GeoObjects.");
  }

  @Override
  public Object getValue(String attributeName)
  {
    if (attributeName.equals(DefaultAttribute.CODE.getName()))
    {
      return this.getCode();
    }
    else if (attributeName.equals(DefaultAttribute.DISPLAY_LABEL.getName()))
    {
      return this.getDisplayLabel();
    }
    else if (attributeName.equals(DefaultAttribute.UID.getName()))
    {
      return this.getUid();
    }

    MdAttributeConcreteDAOIF mdAttribute = this.getBusiness().getMdAttributeDAO(attributeName);

    Object value = this.getBusiness().getObjectValue(attributeName);

    if (value != null && mdAttribute instanceof MdAttributeTermDAOIF)
    {
      return Classifier.get((String) value);
    }

    return value;
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
    this.setDisplayLabel(geoObject.getDisplayLabel());
    this.setGeometry(geoObject.getGeometry());
  }
  
  @Override
  public void populate(GeoObjectOverTime goTime)
  {
    Map<String, AttributeType> attributes = goTime.getType().getAttributeMap();
    attributes.forEach((attributeName, attribute) -> {
      if (attributeName.equals(DefaultAttribute.STATUS.getName()) || attributeName.equals(DefaultAttribute.DISPLAY_LABEL.getName()) || attributeName.equals(DefaultAttribute.CODE.getName()) || attributeName.equals(DefaultAttribute.UID.getName()))
      {
        // Ignore the attributes
      }
      else if (this.business.hasAttribute(attributeName) && !this.business.getMdAttributeDAO(attributeName).isSystem())
      {
        if (attribute instanceof AttributeTermType)
        {
          Iterator<String> it = (Iterator<String>) goTime.getValue(attributeName);

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
          Object value = goTime.getValue(attributeName);

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
    
    ValueOverTimeCollectionDTO votcDL = goTime.getAllValues(DefaultAttribute.DISPLAY_LABEL.getName());
    if (votcDL.size() > 0)
    {
      ValueOverTimeDTO votDTO = votcDL.get(votcDL.size()-1);
      this.setDisplayLabel(goTime.getDisplayLabel(votDTO.getStartDate()));
    }
    
    ValueOverTimeCollectionDTO votcGeom = goTime.getAllValues(DefaultAttribute.GEOMETRY.getName());
    if (votcGeom.size() > 0)
    {
      ValueOverTimeDTO votDTO = votcGeom.get(votcGeom.size()-1);
      this.setGeometry(goTime.getGeometry(votDTO.getStartDate()));
    }
    
    ValueOverTimeCollectionDTO votcStatus = goTime.getAllValues(DefaultAttribute.STATUS.getName());
    if (votcStatus.size() > 0)
    {
      ValueOverTimeDTO votDTO = votcStatus.get(votcStatus.size()-1);
      GeoObjectStatus gos = this.business.isNew() ? GeoObjectStatus.PENDING : ConversionService.getInstance().termToGeoObjectStatus(goTime.getStatus(votDTO.getStartDate()));
      this.setStatus(gos);
    }

    this.setUid(goTime.getUid());
    this.setCode(goTime.getCode());
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

  @Override
  public Map<String, LocationInfo> getAncestorMap(ServerHierarchyType hierarchy)
  {
    return GeoObjectUtil.getAncestorMap(this.toGeoObject(), hierarchy);
  }

  protected static ServerParentTreeNode internalGetParentGeoObjects(ServerGeoObjectIF child, String[] parentTypes, boolean recursive, ServerHierarchyType htIn)
  {
    ServerGeoObjectService service = new ServerGeoObjectService();

    ServerParentTreeNode tnRoot = new ServerParentTreeNode(child, htIn, null);

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

        String parentRunwayId = (String) child.getValue(mdAttribute.definesAttribute());

        if (parentRunwayId != null && parentRunwayId.length() > 0)
        {
          GeoEntity geParent = GeoEntity.get(parentRunwayId);
          ServerGeoObjectIF parent = service.build(geParent);
          Universal uni = parent.getType().getUniversal();

          if (parentTypes == null || parentTypes.length == 0 || ArrayUtils.contains(parentTypes, uni.getKey()))
          {
            ServerParentTreeNode tnParent;

            ServerHierarchyType ht = AttributeHierarchy.getHierarchyType(mdAttribute.getKey());

            if (recursive)
            {
              tnParent = RelationalServerGeoObject.internalGetParentGeoObjects(parent, parentTypes, recursive, ht);
            }
            else
            {
              tnParent = new ServerParentTreeNode(parent, ht, null);
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

          ServerParentTreeNode tnParent;
          if (recursive)
          {
            tnParent = RelationalServerGeoObject.internalGetParentGeoObjects(parent, parentTypes, recursive, ht);
          }
          else
          {
            tnParent = new ServerParentTreeNode(parent, ht, null);
          }

          tnRoot.addParent(tnParent);
        }
      }
    }

    return tnRoot;
  }

}
