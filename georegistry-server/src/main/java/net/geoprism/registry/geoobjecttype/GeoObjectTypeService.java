package net.geoprism.registry.geoobjecttype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.session.Session;

import net.geoprism.registry.model.GeoObjectTypeMetadata;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.ServiceFactory;

public class GeoObjectTypeService
{
  private RegistryAdapter adapter;
  
  public GeoObjectTypeService(RegistryAdapter adapter)
  {
    this.adapter = adapter;
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
  public List<GeoObjectType> getGeoObjectTypes(String[] codes, String[] hierarchies)
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
          net.geoprism.registry.DataNotFoundException ex = new net.geoprism.registry.DataNotFoundException();
          ex.setTypeLabel(GeoObjectTypeMetadata.get().getClassDisplayLabel());
          ex.setDataIdentifier(codes[i]);
          ex.setAttributeLabel(GeoObjectTypeMetadata.get().getAttributeDisplayLabel(DefaultAttribute.CODE.getName()));
          throw ex;
        }
      }
    }
    
    SingleActorDAOIF actor = null;
    if (Session.getCurrentSession() != null)
    {
      actor = Session.getCurrentSession().getUser();
    }
    
    Iterator<GeoObjectType> it = gots.iterator();
    while (it.hasNext())
    {
      GeoObjectType got = it.next();
      
      ServerGeoObjectType serverGot = ServerGeoObjectType.get(got);
      
      // Filter ones that they can't see due to permissions
      if (!ServiceFactory.getGeoObjectTypePermissionService().canRead(actor, serverGot.getOrganization().getCode()))
      {
        it.remove();
      }
      
      if (hierarchies != null && hierarchies.length > 0)
      {
        List<ServerHierarchyType> hts = serverGot.getHierarchies();
        
        boolean contains = false;
        OuterLoop:
        for (ServerHierarchyType ht : hts)
        {
          for (String hierarchy : hierarchies)
          {
            if (ht.getCode().equals(hierarchy))
            {
              contains = true;
              break OuterLoop;
            }
          }
        }
        
        if (!contains)
        {
          it.remove();
        }
      }
    }

    return gots;
  }
  
  
  
}
