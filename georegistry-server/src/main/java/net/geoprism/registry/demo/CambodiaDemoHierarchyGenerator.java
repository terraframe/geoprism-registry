/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.demo;

import org.commongeoregistry.adapter.RegistryAdapterServer;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.MetadataFactory;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.Session;

import net.geoprism.registry.service.RegistryIdService;
import net.geoprism.registry.service.ServiceFactory;


public class CambodiaDemoHierarchyGenerator
{
  public static void main(String[] args)
  {
    doInRequest();
  }
  
  @Request
  public static void doInRequest()
  {
    generateHierarchyTypes();
  }
  
  private static void generateHierarchyTypes()
  {
//    RegistryAdapterServer registry = new RegistryAdapterServer(RegistryIdService.getInstance());
//    
//    HierarchyType ht = MetadataFactory.newHierarchyType("Cambodia", new LocalizedValue("Cambodia"), new LocalizedValue(""), null, registry);
//    ServiceFactory.getHierarchyService().createHierarchyType(Session.getCurrentSession().getOid(), ht.toJSON().toString());
  }
}
