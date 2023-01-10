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
package net.geoprism.registry.curation;

import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.google.gson.JsonObject;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.query.OIterator;
import org.locationtech.jts.geom.Geometry;

import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.curation.CurationProblem.CurationResolution;
import net.geoprism.registry.curation.GeoObjectProblem.GeoObjectProblemType;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

public class ListCurator
{
  private ListTypeVersion     version;

  private ListCurationHistory history;

  public ListCurator(ListCurationHistory history, ListTypeVersion version)
  {
    this.history = history;
    this.version = version;
  }

  public void run()
  {
    final ListType masterlist = version.getListType();
    final ServerGeoObjectType type = ServerGeoObjectType.get(masterlist.getUniversal());
    // final MdBusinessDAO mdBusiness =
    // MdBusinessDAO.get(version.getMdBusinessOid()).getBusinessDAO();

    BusinessQuery query = this.version.buildQuery(new JsonObject());
    query.ORDER_BY_DESC(query.aCharacter(DefaultAttribute.CODE.getName()));

    history.appLock();
    history.setWorkTotal(query.getCount());
    history.setWorkProgress(0L);
    history.apply();

    OIterator<Business> objects = query.getIterator();

    try
    {

      while (objects.hasNext())
      {
        Business row = objects.next();

        final Geometry geom = row.getObjectValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
        final String code = row.getValue(DefaultAttribute.CODE.getName());

        if (geom == null)
        {
          GeoObjectProblem problem = new GeoObjectProblem();
          problem.setHistory(history);
          problem.setResolution(CurationResolution.UNRESOLVED.name());
          problem.setProblemType(GeoObjectProblemType.NO_GEOMETRY.name());
          problem.setTypeCode(type.getCode());
          problem.setGoCode(code);
          problem.setUid(row.getValue(DefaultAttribute.UID.getName()));
          problem.apply();
        }

        history.appLock();
        history.setWorkProgress(history.getWorkProgress() + 1);
        history.apply();

        if (history.getWorkProgress() % 100 == 0)
        {
          NotificationFacade.queue(new GlobalNotificationMessage(MessageType.CURATION_JOB_CHANGE, null));
        }
      }
    }
    finally
    {
      objects.close();
    }
  }
}
