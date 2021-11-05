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
package com.runwaysdk.build.domain;

import org.commongeoregistry.adapter.dataaccess.LocalizedValue;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.action.ChangeRequest;
import net.geoprism.registry.action.ChangeRequestQuery;
import net.geoprism.registry.model.ServerGeoObjectType;

public class ChangeRequestSortingPatch
{
  public static void main(String[] args)
  {
    new ChangeRequestSortingPatch().doIt();
  }
  
  @Transaction
  private void doIt()
  {
    ChangeRequestQuery crq = new ChangeRequestQuery(new QueryFactory());
    
    OIterator<? extends ChangeRequest> it = crq.getIterator();
    
    for (ChangeRequest cr : it)
    {
      LocalizedValue goLabel = cr.getGeoObjectDisplayLabel();
      ServerGeoObjectType type = cr.getGeoObjectType();
      
      cr.appLock();
      cr.getGeoObjectLabel().setLocaleMap(goLabel.getLocaleMap());
      cr.getGeoObjectTypeLabel().setLocaleMap(type.getLabel().getLocaleMap());
      cr.apply();
    }
  }
}
