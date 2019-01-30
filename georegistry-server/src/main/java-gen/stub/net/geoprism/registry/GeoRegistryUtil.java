package net.geoprism.registry;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.business.rbac.Authenticate;

import net.geoprism.georegistry.service.ServiceFactory;

public class GeoRegistryUtil extends GeoRegistryUtilBase
{
  private static final long serialVersionUID = 2034796376;

  public GeoRegistryUtil()
  {
    super();
  }

  @Authenticate
  public static String createHierarchyType(String htJSON)
  {
    RegistryAdapter adapter = ServiceFactory.getAdapter();

    HierarchyType hierarchyType = HierarchyType.fromJSON(htJSON, adapter);

    hierarchyType = ServiceFactory.getConversionService().createHierarchyType(hierarchyType);

    // The transaction did not error out, so it is safe to put into the cache.
    ServiceFactory.getAdapter().getMetadataCache().addHierarchyType(hierarchyType);

    return hierarchyType.getCode();
  }

}
