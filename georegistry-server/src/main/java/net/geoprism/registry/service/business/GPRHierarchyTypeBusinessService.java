package net.geoprism.registry.service.business;

import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.ListType;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.request.SerializedListTypeCache;

@Service
@Primary
public class GPRHierarchyTypeBusinessService extends HierarchyTypeBusinessService implements HierarchyTypeBusinessServiceIF
{
  @Override
  public void refresh(ServerHierarchyType sht)
  {
    super.refresh(sht);
    SerializedListTypeCache.getInstance().clear();
  }

  @Override
  @Transaction
  protected void deleteInTrans(ServerHierarchyType sht)
  {
    super.deleteInTrans(sht);

    ListType.markAllAsInvalid(sht, null);
  }

  @Override
  @Transaction
  protected void removeFromHierarchy(ServerHierarchyType sht, ServerGeoObjectType parentType, ServerGeoObjectType childType, boolean migrateChildren)
  {
    super.removeFromHierarchy(sht, parentType, childType, migrateChildren);

    ListType.markAllAsInvalid(sht, childType);
    SerializedListTypeCache.getInstance().clear();
  }

  @Override
  @Transaction
  protected ServerHierarchyType createHierarchyTypeInTrans(HierarchyType dto)
  {
    ServerHierarchyType hierarchyType = super.createHierarchyTypeInTrans(dto);

    // Assign GPR permissions
    RoleDAO maintainer = RoleDAO.findRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE).getBusinessDAO();
    RoleDAO consumer = RoleDAO.findRole(RegistryConstants.API_CONSUMER_ROLE).getBusinessDAO();
    RoleDAO contributor = RoleDAO.findRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE).getBusinessDAO();

    MdEdgeDAOIF objectEdge = hierarchyType.getObjectEdge();

    this.grantWritePermissionsOnMdTermRel(objectEdge);
    this.grantWritePermissionsOnMdTermRel(maintainer, objectEdge);
    this.grantReadPermissionsOnMdTermRel(consumer, objectEdge);
    this.grantReadPermissionsOnMdTermRel(contributor, objectEdge);

    MdEdgeDAOIF definitionEdge = hierarchyType.getDefinitionEdge();

    this.grantWritePermissionsOnMdTermRel(definitionEdge);
    this.grantWritePermissionsOnMdTermRel(maintainer, definitionEdge);
    this.grantReadPermissionsOnMdTermRel(consumer, definitionEdge);
    this.grantReadPermissionsOnMdTermRel(contributor, definitionEdge);

    return hierarchyType;
  }
}
