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
package net.geoprism.registry;

import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.transaction.Transaction;

public class SingleListType extends SingleListTypeBase
{
  private static final long serialVersionUID = 1505919949;

  public SingleListType()
  {
    super();
  }
  
  @Override
  public JsonObject toJSON(boolean includeEntries)
  {
    JsonObject object = super.toJSON(includeEntries);
    object.addProperty(LIST_TYPE, SINGLE);
    object.addProperty(VALIDON, GeoRegistryUtil.formatDate(this.getValidOn(), false));

    return object;
  }

  @Override
  protected void parse(JsonObject object)
  {
    super.parse(object);

    this.setValidOn(GeoRegistryUtil.parseDate(object.get(SingleListType.VALIDON).getAsString()));
  }

  @Override
  @Transaction
  public void createEntries()
  {
    if (!this.isValid())
    {
      throw new InvalidMasterListException();
    }

    this.getOrCreateEntry(this.getValidOn());
  }

  @Override
  protected String formatVersionLabel(LabeledVersion version)
  {
    return GeoRegistryUtil.formatDate(this.getValidOn(), false);
  }

}
