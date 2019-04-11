package net.geoprism.registry;

import net.geoprism.registry.service.RegistryService;
import net.geoprism.registry.service.ServiceFactory;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.session.Request;

public class MultipleParentTestDataGenerator
{
 public static void main(String[] args)
 {
   mainInReq();
 }

 @Request
 public static void mainInReq()
 {
   GeoObject child = ServiceFactory.getUtilities().getGeoObjectByCode("855 0109", "Cambodia_District");
   HierarchyType ht = ServiceFactory.getAdapter().getMetadataCache().getHierachyType("Hierarchy2").get();

   GeoObject parent = ServiceFactory.getUtilities().getGeoObjectByCode("855 01", "Cambodia_Province");
   GeoObject newParent = ServiceFactory.getUtilities().getGeoObjectByCode("855 02", "Cambodia_Province");

   RegistryService.getInstance().removeChildInTransaction(parent.getUid(), parent.getType().getCode(), child.getUid(), child.getType().getCode(), ht.getCode());
   RegistryService.getInstance().addChildInTransaction(newParent.getUid(), newParent.getType().getCode(), child.getUid(), child.getType().getCode(), ht.getCode());
 }
}