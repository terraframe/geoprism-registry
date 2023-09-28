package net.geoprism.registry.hierarchy;

import java.util.List;

import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import net.geoprism.graphrepo.permission.GeoObjectTypePermissionServiceIF;
import net.geoprism.graphrepo.permission.HierarchyTypePermissionServiceIF;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.permission.RolePermissionService;
import net.geoprism.registry.service.GPRServiceFactory;
import net.geoprism.registry.service.ServiceFactory;

@Component
public class GPRHierarchyService extends HierarchyService implements HierarchyServiceIF
{
  @Request(RequestType.SESSION)
  public JsonArray getHierarchyGroupedTypes(String sessionId)
  {
    final HierarchyTypePermissionServiceIF hierarchyPermissions = ServiceFactory.getHierarchyPermissionService();
    final GeoObjectTypePermissionServiceIF typePermissions = ServiceFactory.getGeoObjectTypePermissionService();
    final RolePermissionService rps = GPRServiceFactory.getRolePermissionService();
    final boolean isSRA = rps.isSRA();

    JsonArray allHiers = new JsonArray();

    List<ServerHierarchyType> shts = ServiceFactory.getMetadataCache().getAllHierarchyTypes();

    for (ServerHierarchyType sht : shts)
    {
      final String htOrgCode = sht.getOrganizationCode();

      if (hierarchyPermissions.canRead(htOrgCode) && ( isSRA || rps.isRA(htOrgCode) || rps.isRM(htOrgCode) ))
      {
        JsonObject hierView = new JsonObject();
        hierView.addProperty("code", sht.getCode());
        hierView.addProperty("label", sht.getDisplayLabel().getValue());
        hierView.addProperty("orgCode", sht.getOrganizationCode());

        JsonArray allHierTypes = new JsonArray();

        List<ServerGeoObjectType> types = sht.getAllTypes(false);

        for (ServerGeoObjectType type : types)
        {
          final String gotOrgCode = type.getOrganizationCode();

          if (typePermissions.canRead(gotOrgCode, type, type.getIsPrivate()) && ( isSRA || rps.isRA(gotOrgCode) || rps.isRM(gotOrgCode, type) ))
          {
            if (type.getIsAbstract())
            {
              JsonObject superView = new JsonObject();
              superView.addProperty("code", type.getCode());
              superView.addProperty("label", type.getLabel().getValue());
              superView.addProperty("orgCode", type.getOrganizationCode());
              superView.addProperty("isAbstract", true);

              List<ServerGeoObjectType> subtypes = type.getSubtypes();

              for (ServerGeoObjectType subtype : subtypes)
              {
                JsonObject typeView = new JsonObject();
                typeView.addProperty("code", subtype.getCode());
                typeView.addProperty("label", subtype.getLabel().getValue());
                typeView.addProperty("orgCode", subtype.getOrganization().getCode());
                typeView.add("super", superView);

                allHierTypes.add(typeView);
              }
            }
            else
            {
              JsonObject typeView = new JsonObject();
              typeView.addProperty("code", type.getCode());
              typeView.addProperty("label", type.getLabel().getValue());
              typeView.addProperty("orgCode", type.getOrganizationCode());

              allHierTypes.add(typeView);
            }
          }
        }

        hierView.add("types", allHierTypes);

        allHiers.add(hierView);
      }
    }

    return allHiers;
  }
  
  @Override
  @Request(RequestType.SESSION)
  public HierarchyType createHierarchyType(String sessionId, String htJSON)
  {
    String code = GeoRegistryUtil.createHierarchyType(htJSON);

    ( (Session) Session.getCurrentSession() ).reloadPermissions();

    return ServiceFactory.getAdapter().getMetadataCache().getHierachyType(code).get();
  }
}
