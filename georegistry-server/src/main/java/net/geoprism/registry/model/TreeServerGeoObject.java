package net.geoprism.registry.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.DefaultTerms;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.Attribute;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.commongeoregistry.adapter.dataaccess.UnknownTermException;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessEnumeration;
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
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

import net.geoprism.dashboard.GeometryUpdateException;
import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.GeoEntityUtil;
import net.geoprism.registry.AttributeHierarchy;
import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.GeometryTypeException;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.conversion.LocalizedValueConverter;
import net.geoprism.registry.io.TermValueException;
import net.geoprism.registry.service.RegistryIdService;
import net.geoprism.registry.service.ServerGeoObjectService;
import net.geoprism.registry.service.ServiceFactory;

public class TreeServerGeoObject extends AbstractServerGeoObject implements ServerGeoObjectIF
{
  private GeoEntity geoEntity;

  public TreeServerGeoObject(ServerGeoObjectType type, GeoEntity geoEntity, Business business)
  {
    super(type, business);

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
    return this.geoEntity.getGeoId();
  }

  @Override
  public void setCode(String code)
  {
    this.geoEntity.setGeoId(code);
    this.getBusiness().setValue(DefaultAttribute.CODE.getName(), code);
  }

  @Override
  public void setGeometry(Geometry geometry)
  {
    if (geometry != null)
    {
      if (!this.isValidGeometry(geometry))
      {
        GeometryTypeException ex = new GeometryTypeException();
        ex.setActualType(geometry.getGeometryType());
        ex.setExpectedType(this.getType().getGeometryType().name());

        throw ex;
      }

      try
      {
        GeometryHelper geometryHelper = new GeometryHelper();

        Point point = geometryHelper.getGeoPoint(geometry);
        MultiPolygon multipolygon = geometryHelper.getGeoMultiPolygon(geometry);

        this.geoEntity.setGeoPoint(point);
        this.geoEntity.setGeoMultiPolygon(multipolygon);
        this.geoEntity.setWkt(geometry.toText());
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
  }

  @Override
  public void setLabel(LocalizedValue label)
  {
    LocalizedValueConverter.populate(this.geoEntity.getDisplayLabel(), label);
  }

  @Override
  public String getRunwayId()
  {
    return this.geoEntity.getOid();
  }

  @Override
  public List<? extends MdAttributeConcreteDAOIF> getMdAttributeDAOs()
  {
    return this.getBusiness().getMdAttributeDAOs();
  }

  @Override
  public String getValue(String attributeName)
  {
    if (attributeName.equals(DefaultAttribute.CODE.getName()))
    {
      return this.getGeoEntity().getGeoId();
    }
    else if (attributeName.equals(DefaultAttribute.DISPLAY_LABEL.getName()))
    {
      return this.getGeoEntity().getDisplayLabel().getValue();
    }

    return this.getBusiness().getValue(attributeName);
  }

  @Override
  public void setValue(String attributeName, Object value)
  {
    if (attributeName.equals(DefaultAttribute.CODE.getName()))
    {
      this.getGeoEntity().setGeoId((String) value);
    }
    else if (attributeName.equals(DefaultAttribute.DISPLAY_LABEL.getName()))
    {
      this.getGeoEntity().getDisplayLabel().setValue((String) value);
    }

    this.getBusiness().setValue(attributeName, value);
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
    return TreeServerGeoObject.internalGetChildGeoObjects(this, childrenTypes, recursive, null);
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
  public ParentTreeNode addParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    if (!hierarchyType.getUniversalType().equals(AllowedIn.CLASS))
    {
      GeoEntity.validateUniversalRelationship(this.getType().getUniversal(), parent.getType().getUniversal(), hierarchyType.getUniversalType());
    }

    this.geoEntity.addLink( ( (TreeServerGeoObject) parent ).getGeoEntity(), hierarchyType.getEntityType());

    ParentTreeNode node = new ParentTreeNode(this.toGeoObject(), hierarchyType.getType());
    node.addParent(new ParentTreeNode(parent.toGeoObject(), hierarchyType.getType()));

    return node;
  }

  @Override
  public void removeParent(ServerGeoObjectIF parent, ServerHierarchyType hierarchyType)
  {
    this.geoEntity.removeLink( ( (TreeServerGeoObject) parent ).getGeoEntity(), hierarchyType.getEntityType());
  }

  @Override
  public void lock()
  {
    if (!this.geoEntity.isNew())
    {
      this.geoEntity.appLock();
      this.getBusiness().appLock();
    }
  }

  @Override
  public void apply(boolean isImport)
  {
    boolean isNew = this.geoEntity.isNew();

    if (!isImport && !isNew && !this.getType().isGeometryEditable() && this.geoEntity.isModified(GeoEntity.WKT))
    {
      throw new GeometryUpdateException();
    }

    this.geoEntity.apply();

    if (isNew)
    {
      this.getBusiness().setValue(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME, this.geoEntity.getOid());
    }

    this.getBusiness().apply();
  }

  @Override
  public GeoObject toGeoObject()
  {
    ServerGeoObjectType type = this.getType();

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Map<String, Attribute> attributeMap = GeoObject.buildAttributeMap(type.getType());

    GeoObject geoObj = new GeoObject(type.getType(), type.getGeometryType(), attributeMap);

    if (geoEntity.isNew() && !geoEntity.isAppliedToDB())
    {
      geoObj.setUid(RegistryIdService.getInstance().next());

      geoObj.setStatus(ServiceFactory.getAdapter().getMetadataCache().getTerm(DefaultTerms.GeoObjectStatusTerm.NEW.code).get());
    }
    else
    {
      geoObj.setUid(RegistryIdService.getInstance().runwayIdToRegistryId(geoEntity.getOid(), geoEntity.getUniversal()));

      Business biz = TreeServerGeoObject.getBusiness(geoEntity);

      Map<String, AttributeType> attributes = type.getAttributeMap();
      attributes.forEach((attributeName, attribute) -> {
        if (attributeName.equals(DefaultAttribute.STATUS.getName()))
        {
          BusinessEnumeration busEnum = biz.getEnumValues(attributeName).get(0);
          GeoObjectStatus gos = GeoObjectStatus.valueOf(busEnum.name());
          Term statusTerm = ServiceFactory.getConversionService().geoObjectStatusToTerm(gos);

          geoObj.setStatus(statusTerm);
        }
        else if (attributeName.equals(DefaultAttribute.TYPE.getName()))
        {
          // Ignore
        }
        else if (biz.hasAttribute(attributeName))
        {
          String value = biz.getValue(attributeName);

          if (value != null && value.length() > 0)
          {
            if (attribute instanceof AttributeTermType)
            {
              Classifier classifier = Classifier.get(value);

              try
              {
                geoObj.setValue(attributeName, classifier.getClassifierId());
              }
              catch (UnknownTermException e)
              {
                TermValueException ex = new TermValueException();
                ex.setAttributeLabel(e.getAttribute().getLabel().getValue());
                ex.setCode(e.getCode());

                throw e;
              }
            }
            else if (attribute instanceof AttributeDateType)
            {
              try
              {
                geoObj.setValue(attributeName, format.parse(value));
              }
              catch (ParseException e)
              {
                throw new RuntimeException(e);
              }
            }
            else if (attribute instanceof AttributeBooleanType)
            {
              geoObj.setValue(attributeName, new Boolean(value));
            }
            else if (attribute instanceof AttributeFloatType)
            {
              geoObj.setValue(attributeName, new Double(value));
            }
            else if (attribute instanceof AttributeIntegerType)
            {
              geoObj.setValue(attributeName, new Long(value));
            }
            else
            {
              geoObj.setValue(attributeName, value);
            }
          }
        }
      });
    }

    geoObj.setCode(geoEntity.getGeoId());
    geoObj.getDisplayLabel().setValue(geoEntity.getDisplayLabel().getValue());
    geoObj.setGeometry(this.getGeometry());

    return geoObj;
  }

  private Geometry getGeometry()
  {
    GeometryType geometryType = this.getType().getGeometryType();

    if (geometryType.equals(GeometryType.LINE))
    {
      return geoEntity.getGeoLine();
    }
    else if (geometryType.equals(GeometryType.MULTILINE))
    {
      return geoEntity.getGeoMultiLine();
    }
    else if (geometryType.equals(GeometryType.POINT))
    {
      return geoEntity.getGeoPoint();
    }
    else if (geometryType.equals(GeometryType.MULTIPOINT))
    {
      return geoEntity.getGeoMultiPoint();
    }
    else if (geometryType.equals(GeometryType.POLYGON))
    {
      return geoEntity.getGeoPolygon();
    }
    else if (geometryType.equals(GeometryType.MULTIPOLYGON))
    {
      return geoEntity.getGeoMultiPolygon();
    }

    throw new UnsupportedOperationException("Unsupported geometry type [" + geometryType.name() + "]");
  }

  private static ChildTreeNode internalGetChildGeoObjects(ServerGeoObjectIF parent, String[] childrenTypes, Boolean recursive, ServerHierarchyType htIn)
  {
    String[] relationshipTypes = TermUtil.getAllParentRelationships(parent.getRunwayId());
    Map<String, ServerHierarchyType> htMap = parent.getHierarchyTypeMap(relationshipTypes);

    ChildTreeNode tnRoot = new ChildTreeNode(parent.toGeoObject(), htIn != null ? htIn.getType() : null);

    ServerGeoObjectService service = new ServerGeoObjectService();

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
                  ServerGeoObjectIF goChild = service.build(childType, child);

                  tnRoot.addChild(new ChildTreeNode(goChild.toGeoObject(), ht.getType()));
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
        ServerGeoObjectIF child = service.build(childType, geChild);

        ChildTreeNode tnChild;

        if (recursive)
        {
          tnChild = TreeServerGeoObject.internalGetChildGeoObjects(child, childrenTypes, recursive, ht);
        }
        else
        {
          tnChild = new ChildTreeNode(child.toGeoObject(), ht.getType());
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

    try (OIterator<? extends Business> bit = bq.getIterator())
    {
      if (bit.hasNext())
      {
        return bit.next();
      }
    }

    return null;
  }

  public static Business getByCode(ServerGeoObjectType type, String code)
  {
    QueryFactory factory = new QueryFactory();

    BusinessQuery bQuery = factory.businessQuery(type.definesType());
    bQuery.WHERE(bQuery.aCharacter(DefaultAttribute.CODE.getName()).EQ(code));

    try (OIterator<? extends Business> bit = bQuery.getIterator())
    {
      if (bit.hasNext())
      {
        return bit.next();
      }
    }

    return null;
  }

}
