package net.geoprism.registry.hierarchy;

import com.runwaysdk.business.rbac.SingleActorDAOIF;

import net.geoprism.registry.Organization;
import net.geoprism.registry.model.ServerHierarchyType;

public interface HierarchyPermissionServiceIF
{
  
  public boolean canRead(SingleActorDAOIF actor, Organization org);
  
  public void enforceCanRead(SingleActorDAOIF actor, Organization org);

  public boolean canWrite(SingleActorDAOIF actor, Organization org);
  
  public void enforceCanWrite(SingleActorDAOIF actor, Organization org);
  
  public boolean canCreate(SingleActorDAOIF actor, Organization org);
  
  public void enforceCanCreate(SingleActorDAOIF actor, Organization org);

  public boolean canAddChild(SingleActorDAOIF user, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);
  
  public void enforceCanAddChild(SingleActorDAOIF user, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);
  
  public boolean canAddChildCR(SingleActorDAOIF user, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);
  
  public void enforceCanAddChildCR(SingleActorDAOIF user, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);
  
  public boolean canRemoveChild(SingleActorDAOIF user, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);
  
  public void enforceCanRemoveChild(SingleActorDAOIF user, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);
  
  public boolean canRemoveChildCR(SingleActorDAOIF user, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);
  
  public void enforceCanRemoveChildCR(SingleActorDAOIF user, ServerHierarchyType ht, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);
  
}
