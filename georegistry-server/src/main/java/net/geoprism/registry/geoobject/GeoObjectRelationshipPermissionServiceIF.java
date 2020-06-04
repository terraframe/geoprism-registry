package net.geoprism.registry.geoobject;

import com.runwaysdk.business.rbac.SingleActorDAOIF;

public interface GeoObjectRelationshipPermissionServiceIF
{
  public boolean canAddChild(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);

  public void enforceCanAddChild(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);
  
  public boolean canViewChild(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);
  
  public void enforceCanViewChild(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);

  public boolean canAddChildCR(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);

  public void enforceCanAddChildCR(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);
  
  public boolean canRemoveChild(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);

  public void enforceCanRemoveChild(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);

  public boolean canRemoveChildCR(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);

  public void enforceCanRemoveChildCR(SingleActorDAOIF actor, String orgCode, String parentGeoObjectTypeCode, String childGeoObjectTypeCode);
}
