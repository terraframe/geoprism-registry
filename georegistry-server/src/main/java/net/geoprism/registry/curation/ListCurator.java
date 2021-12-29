package net.geoprism.registry.curation;

import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.query.OIterator;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.curation.CurationProblem.CurationResolution;
import net.geoprism.registry.curation.GeoObjectProblem.GeoObjectProblemType;
import net.geoprism.registry.model.ServerGeoObjectType;

public class ListCurator
{
  private ListTypeVersion version;
  
  private ListCurationHistory history;
  
  public ListCurator(ListCurationHistory history, ListTypeVersion version)
  {
    this.history = history;
    this.version = version;
  }
  
  public void run()
  {
    final ListType masterlist = version.getListType();
    final ServerGeoObjectType type = ServerGeoObjectType.get(masterlist.getUniversal());
//    final MdBusinessDAO mdBusiness = MdBusinessDAO.get(version.getMdBusinessOid()).getBusinessDAO();
    
    BusinessQuery query = this.version.buildQuery(null);
    query.ORDER_BY_DESC(query.aCharacter(DefaultAttribute.CODE.getName()));
    
    history.appLock();
    history.setWorkTotal(query.getCount());
    history.setWorkProgress(0L);
    history.apply();

    OIterator<Business> objects = query.getIterator();
    
    try
    {

      while (objects.hasNext())
      {
        Business row = objects.next();

        final Geometry geom = row.getObjectValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
        final String code = row.getValue(DefaultAttribute.CODE.getName());
        
        if (geom == null)
        {
          GeoObjectProblem problem = new GeoObjectProblem();
          problem.setHistory(history);
          problem.setResolution(CurationResolution.UNRESOLVED.name());
          problem.setProblemType(GeoObjectProblemType.NO_GEOMETRY.name());
          problem.setTypeCode(type.getCode());
          problem.setGoCode(code);
          problem.apply();
        }
        
        history.appLock();
        history.setWorkProgress(history.getWorkProgress() + 1);
        history.apply();
      }
    }
    finally
    {
      objects.close();
    }
  }
}
