package net.geoprism.registry;

import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.business.ontology.Term;
import com.runwaysdk.business.ontology.TermAndRel;
import com.runwaysdk.business.ontology.TermHacker;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.Condition;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdBusiness;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.ontology.ImmutableRootException;
import com.runwaysdk.system.ontology.TermUtil;

import net.geoprism.registry.service.ConversionService;
import net.geoprism.registry.service.ServiceFactory;

public class ServerGeoObjectType
{
  private Logger logger = LoggerFactory.getLogger(ServerGeoObject.class);
  
  private GeoObjectType got;
  
  private ServerGeoObjectType(GeoObjectType go)
  {
    this.got = got;
  }
  
  public static ServerGeoObjectType getFromGeoObjectType(GeoObjectType got)
  {
    return new ServerGeoObjectType(got);
  }
  
  public static HierarchyType removeChild(String hierarchyTypeCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    String mdTermRelKey = ConversionService.buildMdTermRelUniversalKey(hierarchyTypeCode);
    MdTermRelationship mdTermRelationship = MdTermRelationship.getByKey(mdTermRelKey);
    
    removeFromHierarchy(mdTermRelationship, hierarchyTypeCode, parentGeoObjectTypeCode, childGeoObjectTypeCode);

    // No exceptions thrown. Refresh the HierarchyType object to include the new
    // relationships.
    HierarchyType ht = ServiceFactory.getConversionService().mdTermRelationshipToHierarchyType(mdTermRelationship);

    ServiceFactory.getAdapter().getMetadataCache().addHierarchyType(ht);

    return ht;
  }
  
  @Transaction
  private static void removeFromHierarchy(MdTermRelationship mdTermRelationship, String hierarchyTypeCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode)
  {
    Universal parent;
    if (parentGeoObjectTypeCode == null)
    {
      parent = Universal.getRoot();
    }
    else
    {
      parent = Universal.getByKey(parentGeoObjectTypeCode);
    }
    
    Universal child = Universal.getByKey(childGeoObjectTypeCode);
    
    boolean hasData = hasData(child);
    
    removeAllChildrenFromHierarchy(parent, mdTermRelationship);
    
    if (hasData)
    {
      child.enforceValidRemoveLink(parent, mdTermRelationship.definesType());
    }

    if (child.getIsLeafType())
    {
      ConversionService.removeParentReferenceToLeafType(hierarchyTypeCode, parent, child);
    }
  }
  
  private static boolean hasData(Universal uni)
  {
    if (uni.getIsLeafType())
    {
      MdBusiness mdBiz = uni.getMdBusiness();
      
      BusinessQuery bq = new QueryFactory().businessQuery(mdBiz.definesType());
      
      if (bq.getCount() > 0)
      {
        GeoObjectTypeHasDataException ex = new GeoObjectTypeHasDataException();
        ex.setName(uni.getDisplayLabel().getValue());
        throw ex;
      }
    }
    else
    {
      GeoEntityQuery query = new GeoEntityQuery(new QueryFactory());
      Condition condition = query.getUniversal().EQ(uni);
  
      Term[] thisAllDescends = TermUtil.getAllDescendants(uni.getOid(), TermUtil.getAllParentRelationships(uni.getOid()));
      for (int i = 0; i < thisAllDescends.length; ++i)
      {
        condition = condition.OR(query.getUniversal().EQ((Universal) thisAllDescends[i]));
      }
      query.WHERE(condition);
      
//      return query.getCount() > 0;
      
      if (query.getCount() > 0)
      {
        GeoObjectTypeHasDataException ex = new GeoObjectTypeHasDataException();
        ex.setName(uni.getDisplayLabel().getValue());
        throw ex;
      }
    }
    
    return false;
  }
  
  private static void removeAllChildrenFromHierarchy(Universal parent, MdTermRelationship mdTermRelationship)
  {
    TermAndRel[] tnrChildren = TermUtil.getDirectDescendants(parent.getOid(), new String[] {mdTermRelationship.definesType()});
    for (TermAndRel tnrChild : tnrChildren)
    {
      Universal child = (Universal) tnrChild.getTerm();

      removeAllChildrenFromHierarchy(child, mdTermRelationship);
      
      removeLink(parent, child, mdTermRelationship.definesType());
    }
  }
  
  private static void removeLink(Universal parent, Universal child, String relationshipType)
  {
    if (child.getKey().equals(Term.ROOT_KEY))
    {
      ImmutableRootException exception = new ImmutableRootException("Cannot modify the root Term.");
      exception.setRootName(child.getDisplayLabel().getValue());
      exception.apply();
      
      throw exception;
    }
    
    // Remove the relationship
    parent.removeAllChildren(child, relationshipType);
    
    // Update the strategy
    TermHacker.getStrategy(child).removeLink(parent, child, relationshipType);
  }
}
