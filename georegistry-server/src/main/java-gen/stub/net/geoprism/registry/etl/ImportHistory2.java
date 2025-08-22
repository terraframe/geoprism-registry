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
package net.geoprism.registry.etl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.model.ServerGeoObjectType;

public class ImportHistory2 extends ImportHistoryBase
{
  private static final long serialVersionUID = 752640606;

  public ImportHistory2()
  {
    super();
  }

  public boolean hasImportErrors()
  {
    ImportErrorQuery query = new ImportErrorQuery(new QueryFactory());
    query.WHERE(query.getHistory().EQ(this));
    return query.getCount() > 0;
  }

  public void deleteAllImportErrors()
  {
    ImportErrorQuery query = new ImportErrorQuery(new QueryFactory());
    query.WHERE(query.getHistory().EQ(this));

    OIterator<? extends ImportError> it = query.getIterator();

    try
    {
      while (it.hasNext())
      {
        it.next().delete();
      }
    }
    finally
    {
      it.close();
    }
  }

  public ImportConfiguration getConfig()
  {
    return ImportConfiguration.build(this.getConfigJson());
  }

  public void deleteAllValidationProblems()
  {
    ValidationProblemQuery query = new ValidationProblemQuery(new QueryFactory());
    query.WHERE(query.getHistory().EQ(this));

    OIterator<? extends ValidationProblem> it = query.getIterator();

    try
    {
      while (it.hasNext())
      {
        it.next().delete();
      }
    }
    finally
    {
      it.close();
    }
  }

  @Override
  public void delete()
  {
    deleteAllImportErrors();

    deleteAllValidationProblems();

    super.delete();
  }

  public ServerGeoObjectType getServerGeoObjectType()
  {
    return ServerGeoObjectType.get(this.getGeoObjectTypeCode());
  }

  public void enforceExecutePermissions()
  {
    JsonObject jo = JsonParser.parseString(this.getConfigJson()).getAsJsonObject();
    if (jo.has(ImportConfiguration.OBJECT_TYPE) && !jo.get(ImportConfiguration.OBJECT_TYPE).getAsString().equals("LPG") && !jo.get(ImportConfiguration.OBJECT_TYPE).getAsString().contains("RDF"))
      getConfig().enforceExecutePermissions();
  }
}
