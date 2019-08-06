package net.geoprism.registry.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.business.ontology.TermAndRel;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.gis.geometry.GeometryHelper;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.gis.geo.AllowedIn;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.WKTParsingProblem;
import com.runwaysdk.system.ontology.TermUtil;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.dashboard.GeometryUpdateException;
import net.geoprism.ontology.GeoEntityUtil;
import net.geoprism.registry.AttributeHierarchy;
import net.geoprism.registry.GeometryTypeException;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.ServerGeoObjectFactory;
import net.geoprism.registry.service.ServiceFactory;

public class ServerTreeGeoObject extends AbstractServerGeoObject implements ServerGeoObjectIF
{
  private Logger    logger = LoggerFactory.getLogger(ServerTreeGeoObject.class);

  private GeoEntity geoEntity;

  public ServerTreeGeoObject(ServerGeoObjectType type, GeoObject geoObject, GeoEntity geoEntity, Business business)
  {
    super(type, geoObject, business);

    this.geoEntity = geoEntity;
  }

  public GeoEntity getGeoEntity()
  {
    return geoEntity;
  }

  public void setGeoEntity(GeoEntity geoEntity)
  {
    this.geoEntity = geoEntity;
  }

  @Override
  public String getCode()
  {
    return this.getGeoObject().getCode();
  }

  @Override
  public String getRunwayId()
  {
    return this.geoEntity.getOid();
  }

  @Override
  public String getUid()
  {
    return this.getGeoObject().getUid();
  }

  @Override
  public List<? extends MdAttributeConcreteDAOIF> getMdAttributeDAOs()
  {
    return this.getBusiness().getMdAttributeDAOs();
  }

  @Override
  public String getValue(String attributeName)
  {
    return this.getBusiness().getValue(attributeName);
  }

  @Override
  public String bbox()
  {
    return GeoEntityUtil.getEntitiesBBOX(new String[] { this.geoEntity.getOid() });
  }

  @Override
  public ParentTreeNode getParentGeoObjects(String[] parentTypes, Boolean recursive)
  {
    return AbstractServerGeoObject.internalGetParentGeoObjects(this, parentTypes, recursive, null);
  }

  @Override
  public ChildTreeNode getChildGeoObjects(String[] childrenTypes, Boolean recursive)
  {
    return ServerTreeGeoObject.internalGetChildGeoObjects(this, childrenTypes, recursive, null);
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
  public ParentTreeNode addParent(ServerTreeGeoObject parent, ServerHierarchyType hierarchyType)
  {
    if (!hierarchyType.getUniversalType().equals(AllowedIn.CLASS))
    {
      GeoEntity.validateUniversalRelationship(this.getType().getUniversal(), parent.getType().getUniversal(), hierarchyType.getUniversalType());
    }

    this.geoEntity.addLink(parent.getGeoEntity(), hierarchyType.getEntityType());

    ParentTreeNode node = new ParentTreeNode(this.getGeoObject(), hierarchyType.getType());
    node.addParent(new ParentTreeNode(parent.getGeoObject(), hierarchyType.getType()));

    return node;
  }

  @Override
  public void removeParent(ServerTreeGeoObject parent, ServerHierarchyType hierarchyType)
  {
    this.geoEntity.removeLink( ( (ServerTreeGeoObject) parent ).getGeoEntity(), hierarchyType.getEntityType());
  }

  @Override
  protected Term populateBusiness(String statusCode)
  {
    this.getBusiness().setValue(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME, this.geoEntity.getOid());

    return super.populateBusiness(statusCode);
  }

  @Override
  public void apply(String statusCode, boolean isImport)
  {
    if (!this.geoEntity.isNew())
    {
      this.geoEntity.appLock();
      this.getBusiness().appLock();
    }

    if (this.getGeoObject().getCode() != null)
    {
      this.geoEntity.setGeoId(this.getGeoObject().getCode());
    }

    ServiceFactory.getConversionService().populate(this.geoEntity.getDisplayLabel(), this.getGeoObject().getDisplayLabel());

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

      try
      {
        GeometryHelper geometryHelper = new GeometryHelper();
        this.geoEntity.setGeoPoint(geometryHelper.getGeoPoint(geom));
        this.geoEntity.setGeoMultiPolygon(geometryHelper.getGeoMultiPolygon(geom));
        this.geoEntity.setWkt(geom.toText());
      }
      catch (Exception e)
      {
        String msg = "Error parsing WKT";

        WKTParsingProblem p = new WKTParsingProblem(msg);
        p.setNotification(this.geoEntity, GeoEntity.WKT);
        p.setReason(e.getLocalizedMessage());
        p.apply();
        p.throwIt();
      }
    }

    if (!isImport && !this.geoEntity.isNew() && !this.getGeoObject().getType().isGeometryEditable() && this.geoEntity.isModified(GeoEntity.WKT))
    {
      throw new GeometryUpdateException();
    }

    this.geoEntity.apply();

    Term statusTerm = populateBusiness(statusCode);

    this.getBusiness().apply();

    /*
     * Update the returned GeoObject
     */
    this.getGeoObject().setStatus(statusTerm);
  }

  private static ChildTreeNode internalGetChildGeoObjects(ServerGeoObjectIF parent, String[] childrenTypes, Boolean recursive, ServerHierarchyType htIn)
  {
    String[] relationshipTypes = TermUtil.getAllParentRelationships(parent.getRunwayId());
    Map<String, ServerHierarchyType> htMap = parent.getHierarchyTypeMap(relationshipTypes);

    ChildTreeNode tnRoot = new ChildTreeNode(parent.getGeoObject(), htIn != null ? htIn.getType() : null);

    /*
     * Handle leaf node children
     */
    if (childrenTypes != null)
    {
      for (int i = 0; i < childrenTypes.length; ++i)
      {
        ServerGeoObjectType childType = ServerGeoObjectType.get(childrenTypes[i]);

        if (childType.isLeaf())
        {
          if (ArrayUtils.contains(childrenTypes, childType.getCode()))
          {
            List<MdAttributeDAOIF> mdAttributes = childType.definesAttributes().stream().filter(mdAttribute -> {
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

            for (MdAttributeDAOIF mdAttribute : mdAttributes)
            {
              ServerHierarchyType ht = AttributeHierarchy.getHierarchyType(mdAttribute.getKey());

              BusinessQuery query = new QueryFactory().businessQuery(childType.definesType());
              query.WHERE(query.get(mdAttribute.definesAttribute()).EQ(parent.getRunwayId()));

              OIterator<Business> it = query.getIterator();

              try
              {
                List<Business> children = it.getAll();

                for (Business child : children)
                {
                  // Do something
                  ServerGeoObjectIF goChild = ServerGeoObjectFactory.build(childType, child);

                  tnRoot.addChild(new ChildTreeNode(goChild.getGeoObject(), ht.getType()));
                }
              }
              finally
              {
                it.close();
              }
            }
          }
        }
      }
    }

    /*
     * Handle tree node children
     */
    TermAndRel[] tnrChildren = TermUtil.getDirectDescendants(parent.getRunwayId(), relationshipTypes);

    for (TermAndRel tnrChild : tnrChildren)
    {
      GeoEntity geChild = (GeoEntity) tnrChild.getTerm();
      Universal uni = geChild.getUniversal();

      if (childrenTypes == null || childrenTypes.length == 0 || ArrayUtils.contains(childrenTypes, uni.getKey()))
      {
        ServerHierarchyType ht = htMap.get(tnrChild.getRelationshipType());

        ServerGeoObjectType childType = ServerGeoObjectType.get(uni);
        ServerGeoObjectIF child = ServerGeoObjectFactory.build(childType, geChild);

        ChildTreeNode tnChild;

        if (recursive)
        {
          tnChild = ServerTreeGeoObject.internalGetChildGeoObjects(child, childrenTypes, recursive, ht);
        }
        else
        {
          tnChild = new ChildTreeNode(child.getGeoObject(), ht.getType());
        }

        tnRoot.addChild(tnChild);
      }
    }

    return tnRoot;
  }

  public static Business getBusiness(GeoEntity entity)
  {
    QueryFactory qf = new QueryFactory();
    BusinessQuery bq = qf.businessQuery(entity.getUniversal().getMdBusiness().definesType());
    bq.WHERE(bq.aReference(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME).EQ(entity));
    OIterator<? extends Business> bit = bq.getIterator();
    try
    {
      if (bit.hasNext())
      {
        return bit.next();
      }
    }
    finally
    {
      bit.close();
    }

    return null;
  }
}
