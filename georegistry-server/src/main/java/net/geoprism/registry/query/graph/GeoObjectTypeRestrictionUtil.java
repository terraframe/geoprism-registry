package net.geoprism.registry.query.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.metadata.RegistryRole;

import com.runwaysdk.business.rbac.RoleDAOIF;
import com.runwaysdk.business.rbac.SingleActorDAOIF;
import com.runwaysdk.session.Session;

import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.service.ServiceFactory;

/**
 * A general utility class for adding graph sql conditions for GeoObjectTypes.
 * 
 * @author rrowlands
 */
public class GeoObjectTypeRestrictionUtil
{
  public static String buildTypeWritePermissionsFilter(String orgColumnAlias, String gotCodeAlias)
  {
    if (ServiceFactory.getRolePermissionService().isSRA())
    {
      return "";
    }
    
    List<String> conditions = hasMandateOnType(orgColumnAlias, gotCodeAlias, false);
    
    if (conditions.size() > 0)
    {
      return "(" + StringUtils.join(conditions, " OR ") + ")";
    }
    else
    {
      return "";
    }
  }
  
  public static String buildTypeReadPermissionsFilter(String orgColumnAlias, String gotCodeAlias)
  {
    if (ServiceFactory.getRolePermissionService().isSRA())
    {
      return "";
    }
    
    final List<String> privateTypes = ServiceFactory.getMetadataCache().getAllGeoObjectTypes().stream().filter(type -> type.getIsPrivate()).map(type -> "'" + type.getCode() + "'").collect(Collectors.toList());
    
    StringBuilder builder = new StringBuilder();
    
    builder.append("(");
    
    // Must be a public type
    builder.append(gotCodeAlias + " NOT IN [" + StringUtils.join(privateTypes, ", ") + "]");
    
    // Or they have CGR mandate permissions on this type 
    List<String> conditions = hasMandateOnType(orgColumnAlias, gotCodeAlias, true);
    if (conditions.size() > 0)
    {
      builder.append(" OR (" + StringUtils.join(conditions, " OR ") + ")");
    }
    
    builder.append(")");
    
    return builder.toString();
  }

  public static List<String> hasMandateOnType(String orgCodeAttr, String gotCodeAttr, boolean allowRC)
  {
    List<String> criteria = new ArrayList<String>();
    List<String> raOrgs = new ArrayList<String>();
    List<String> goRoles = new ArrayList<String>();

    SingleActorDAOIF actor = Session.getCurrentSession().getUser();
    for (RoleDAOIF role : actor.authorizedRoles())
    {
      String roleName = role.getRoleName();

      if (RegistryRole.Type.isOrgRole(roleName) && !RegistryRole.Type.isRootOrgRole(roleName))
      {
        if (RegistryRole.Type.isRA_Role(roleName))
        {
          String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);
          raOrgs.add(roleOrgCode);
        }
        else if (RegistryRole.Type.isRM_Role(roleName))
        {
          goRoles.add(roleName);
        }
        else if (allowRC && RegistryRole.Type.isRC_Role(roleName))
        {
          goRoles.add(roleName);
        }
      }
    }

    for (String orgCode : raOrgs)
    {
      criteria.add("(" + orgCodeAttr + " = '" + orgCode + "')");
    }

    for (String roleName : goRoles)
    {
      String roleOrgCode = RegistryRole.Type.parseOrgCode(roleName);
      String gotCode = RegistryRole.Type.parseGotCode(roleName);

      criteria.add("(" + orgCodeAttr + " = '" + roleOrgCode + "' AND " + gotCodeAttr + " = '" + gotCode + "')");

      // If they have permission to an abstract parent type, then they also have
      // permission to all its children.
      Optional<ServerGeoObjectType> op = ServiceFactory.getMetadataCache().getGeoObjectType(gotCode);

      if (op.isPresent() && op.get().getIsAbstract())
      {
        List<ServerGeoObjectType> subTypes = op.get().getSubtypes();

        for (ServerGeoObjectType subType : subTypes)
        {
          criteria.add("(" + orgCodeAttr + " = '" + subType.getOrganization().getCode() + "' AND " + gotCodeAttr + " = '" + subType.getCode() + "')");
        }
      }
    }

    return criteria;
  }
}
