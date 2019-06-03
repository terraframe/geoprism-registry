package net.geoprism.registry;

import java.util.List;

import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdTermRelationship;

import net.geoprism.registry.service.ServiceFactory;

public class AttributeHierarchy extends AttributeHierarchyBase
{
  private static final long serialVersionUID = -1818416302;

  public AttributeHierarchy()
  {
    super();
  }

  public static HierarchyType getHierarchyType(String key)
  {
    AttributeHierarchy hierarchy = AttributeHierarchy.getByKey(key);
    MdTermRelationship mdTermRelationship = hierarchy.getMdTermRelationship();

    return ServiceFactory.getConversionService().mdTermRelationshipToHierarchyType(mdTermRelationship);
  }

  public static void deleteByUniversal(Universal uni)
  {
    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(uni.getMdBusinessOid());
    
    deleteByMdBusiness(mdBusiness);
  }
  
  public static void deleteByMdBusiness(MdBusinessDAOIF mdBusiness)
  {
    List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdBusiness.definesAttributes();

    for (MdAttributeConcreteDAOIF mdAttribute : mdAttributes)
    {
      if (mdAttribute instanceof MdAttributeReferenceDAOIF)
      {
        MdBusinessDAOIF referencedMdBusiness = ( (MdAttributeReferenceDAOIF) mdAttribute ).getReferenceMdBusinessDAO();

        if (referencedMdBusiness.definesType().equals(GeoEntity.CLASS))
        {
          AttributeHierarchyQuery query = new AttributeHierarchyQuery(new QueryFactory());
          query.WHERE(query.getMdAttribute().EQ(mdAttribute.getOid()));

          List<? extends AttributeHierarchy> hierarchies = query.getIterator().getAll();

          for (AttributeHierarchy hierarchy : hierarchies)
          {
            hierarchy.delete();
          }
        }
      }
    }
  }

  public static void deleteByRelationship(MdTermRelationship mdRelationship)
  {
    AttributeHierarchyQuery query = new AttributeHierarchyQuery(new QueryFactory());
    query.WHERE(query.getMdTermRelationship().EQ(mdRelationship));

    List<? extends AttributeHierarchy> hierarchies = query.getIterator().getAll();

    for (AttributeHierarchy hierarchy : hierarchies)
    {
      hierarchy.delete();
    }
  }
}
