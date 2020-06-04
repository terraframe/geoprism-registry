package net.geoprism.registry.geoobjecttype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.runwaysdk.session.Session;

import net.geoprism.registry.DataNotFoundException;
import net.geoprism.registry.geoobject.GeoObjectPermissionService;
import net.geoprism.registry.geoobject.GeoObjectPermissionServiceIF;
import net.geoprism.registry.model.ServerGeoObjectType;

public class GeoObjectTypeService
{
  private RegistryAdapter adapter;
  
  private GeoObjectPermissionServiceIF goPermServ;
  
  public GeoObjectTypeService(RegistryAdapter adapter)
  {
    this.adapter = adapter;
    this.goPermServ = new GeoObjectPermissionService();
  }
  
  /**
   * Returns the {@link GeoObjectType}s with the given codes or all
   * {@link GeoObjectType}s if no codes are provided.
   * 
   * @param sessionId
   * @param codes
   *          codes of the {@link GeoObjectType}s.
   * @return the {@link GeoObjectType}s with the given codes or all
   *         {@link GeoObjectType}s if no codes are provided.
   */
  public List<GeoObjectType> getGeoObjectTypes(String[] codes)
  {
    List<GeoObjectType> gots;
    
    if (codes == null || codes.length == 0)
    {
      gots = adapter.getMetadataCache().getAllGeoObjectTypes();
    }
    else
    {
      gots = new ArrayList<GeoObjectType>(codes.length);
  
      for (int i = 0; i < codes.length; ++i)
      {
        Optional<GeoObjectType> optional = adapter.getMetadataCache().getGeoObjectType(codes[i]);
  
        if (optional.isPresent())
        {
          gots.add(optional.get());
        }
        else
        {
          DataNotFoundException ex = new DataNotFoundException();
          ex.setDataIdentifier(codes[i]);
          throw ex;
        }
      }
    }
    
    // Filter ones that they can't see due to permissions
    Iterator<GeoObjectType> it = gots.iterator();
    while (it.hasNext())
    {
      GeoObjectType got = it.next();
      
      ServerGeoObjectType serverGot = ServerGeoObjectType.get(got);
      
      if (!this.goPermServ.canRead(Session.getCurrentSession().getUser(), serverGot.getOrganization().getCode(), serverGot.getCode()))
      {
        it.remove();
      }
    }

    return gots;
  }
}
