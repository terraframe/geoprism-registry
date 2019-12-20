/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;

import net.geoprism.registry.service.ServiceFactory;

public class MultipleParentTestDataGenerator
{
 public static void main(String[] args)
 {
   mainInReq();
 }

 @Request
 public static void mainInReq()
 {
   
   GeoObject child = ServiceFactory.getRegistryService().getGeoObjectByCode(Session.getCurrentSession().getOid(), "855 0109", "Cambodia_District");
   HierarchyType ht = ServiceFactory.getAdapter().getMetadataCache().getHierachyType("Hierarchy2").get();

   GeoObject parent = ServiceFactory.getRegistryService().getGeoObjectByCode(Session.getCurrentSession().getOid(), "855 01", "Cambodia_Province");
   GeoObject newParent = ServiceFactory.getRegistryService().getGeoObjectByCode(Session.getCurrentSession().getOid(), "855 02", "Cambodia_Province");

//   RegistryService.getInstance().removeChildInTransaction(parent.getUid(), parent.getType().getCode(), child.getUid(), child.getType().getCode(), ht.getCode());
//   RegistryService.getInstance().addChildInTransaction(newParent.getUid(), newParent.getType().getCode(), child.getUid(), child.getType().getCode(), ht.getCode());
 }
}