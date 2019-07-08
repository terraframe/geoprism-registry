package net.geoprism.registry.adapter;

import java.util.ArrayList;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.business.ontology.Term;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.gis.geo.GeoEntity;

import net.geoprism.ontology.GeoEntityUtil;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.service.ConversionService;
import net.geoprism.registry.service.RegistryIdService;

public class ServerTreeGeoObject implements ServerGeoObjectIF
{
  private Logger logger = LoggerFactory.getLogger(ServerTreeGeoObject.class);
  
  private GeoObject go;
  
  ServerTreeGeoObject(GeoObject go)
  {
    this.go = go;
  }
  
  @Override
  public Business getBusiness()
  {
    GeoEntity ge = this.getGeoEntity();
    
    QueryFactory qf = new QueryFactory();
    BusinessQuery bq = qf.businessQuery(ge.getUniversal().getMdBusiness().definesType());
    bq.WHERE(bq.aReference(RegistryConstants.GEO_ENTITY_ATTRIBUTE_NAME).EQ(ge));
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
  
  public GeoEntity getGeoEntity()
  {
    String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(go.getUid(), go.getType());

    GeoEntity ge = GeoEntity.get(runwayId);
    
    return ge;
  }
  
  @Override
  public String bbox()
  {
    String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(go.getUid(), go.getType());
    
    return GeoEntityUtil.getEntitiesBBOX(new String[] {runwayId});
  }
}
