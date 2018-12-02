package net.geoprism.georegistry.service;

import java.util.UUID;

import net.geoprism.georegistry.IdRecord;
import net.geoprism.georegistry.IdRecordQuery;
import net.geoprism.georegistry.InvalidRegistryIdException;
import net.geoprism.georegistry.RegistryConstants;

import org.commongeoregistry.adapter.id.AdapterIdServiceIF;
import org.commongeoregistry.adapter.id.EmptyIdCacheException;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdBusiness;

public class RegistryIdService implements AdapterIdServiceIF
{
  public RegistryIdService()
  {
  }
  
  public String runwayIdToRegistryId(String runwayId, Universal uni)
  {
    // TODO virtual leaf node ?
    
    QueryFactory qf = new QueryFactory();
    ValueQuery vq = new ValueQuery(qf);
    BusinessQuery bq = qf.businessQuery(uni.getMdBusiness().definesType());
    
    vq.SELECT(bq.get(RegistryConstants.UUID));
    vq.WHERE(bq.get(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME).EQ(runwayId));
    
    OIterator<ValueObject> it = vq.getIterator();
    
    try
    {
      if (!it.hasNext())
      {
        InvalidRegistryIdException ex = new InvalidRegistryIdException();
        ex.setRegistryId(runwayId);
        throw ex;
      }
      
      return it.next().getValue(RegistryConstants.UUID);
    }
    finally
    {
      it.close();
    }
  }
  
  public String registryIdToRunwayId(String registryId, GeoObjectType got)
  {
    if (registryId == null || registryId.length() != 36)
    {
      InvalidRegistryIdException ex = new InvalidRegistryIdException();
      ex.setRegistryId(registryId);
      throw ex;
    }
    
    if (!got.isLeaf())
    {
      Universal uni = ServiceFactory.getConversionService().geoObjectTypeToUniversal(got);
      
      QueryFactory qf = new QueryFactory();
      ValueQuery vq = new ValueQuery(qf);
      BusinessQuery bq = qf.businessQuery(uni.getMdBusiness().definesType());
      
      vq.SELECT(bq.get(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME));
      vq.WHERE(bq.get(RegistryConstants.UUID).EQ(registryId));
      
      OIterator<ValueObject> it = vq.getIterator();
      
//      System.out.println(((MdBusiness)MdBusiness.get("com.runwaysdk.system.metadata.MdBusiness", RegistryConstants.UNIVERSAL_MDBUSINESS_PACKAGE + "." + uni.getUniversalId())).getTableName());
      
      try
      {
        if (!it.hasNext())
        {
          InvalidRegistryIdException ex = new InvalidRegistryIdException();
          ex.setRegistryId(registryId);
          throw ex;
        }
        
        return it.next().getValue(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME);
      }
      finally
      {
        it.close();
      }
    }
    else
    {
      throw new UnsupportedOperationException(); // TODO virtual leaf node ?
    }
  }
  
  @Override
  public void populate(int size)
  {
    // Intentionally empty. The RegistryIdService doesn't need to be populated and will always return ids.
  }

  @Override
  public String next() throws EmptyIdCacheException
  {
    String id = UUID.randomUUID().toString();
    
    IdRecord record = new IdRecord();
    record.setRegistryId(id);
    record.apply();
    
    return id;
  }

  /**
   * Convenience method for fetching a large number of ids. Equivalent to invoking next @amount times.
   * 
   * @param amount
   * @return
   */
  public String[] getUids(int amount)
  {
    String[] ids = new String[amount];
    
    for (int i = 0; i < amount; ++i)
    {
      String id = this.next();
      
      ids[i] = id;
    }
    
    return ids;
  }
  
  public boolean isIssuedId(String id)
  {
    IdRecordQuery query = new IdRecordQuery(new QueryFactory());
    query.WHERE(query.getRegistryId().EQ(id));
    return query.getCount() > 0;
  }

  public static synchronized RegistryIdService getInstance()
  {
    return ServiceFactory.getIdService();
  }
}
