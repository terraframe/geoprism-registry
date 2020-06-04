package net.geoprism.registry.geoobjecttype;

import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.registry.model.ServerHierarchyType;

public interface GeoObjectTypeRelationshipPermissionServiceIF
{
  public boolean canAddChild(SingleActorDAOIF actor, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);

  public void enforceCanAddChild(SingleActorDAOIF actor, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);

  public boolean canRemoveChild(SingleActorDAOIF actor, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);

  public void enforceCanRemoveChild(SingleActorDAOIF actor, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);

}
