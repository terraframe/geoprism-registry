package net.geoprism.registry.service.business;

import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.ListType;
import net.geoprism.registry.business.HierarchyTypeBusinessService;
import net.geoprism.registry.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.service.SerializedListTypeCache;

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
  
  
}
