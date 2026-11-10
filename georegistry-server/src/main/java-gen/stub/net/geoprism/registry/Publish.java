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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.geoprism.registry.conversion.RegistryLocalizedValueConverter;
import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeClass;

public class Publish extends PublishBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 827828570;

  public Publish()
  {
    super();
  }

  public PublishDTO toDTO()
  {
    PublishDTO configuration = new PublishDTO(this.getDisplayLabel().getValue(), this.getForDate(), this.getStartDate(), this.getEndDate());
    configuration.setUid(this.getUid());
    configuration.setDescription(RegistryLocalizedValueConverter.convertNoAutoCoalesce(this.getDescription()));
    configuration.setConceptSet(this.getConceptSet());

    JsonArray types = JsonParser.parseString(this.getTypeCodes()).getAsJsonArray();

    types.forEach(element -> {
      JsonObject object = element.getAsJsonObject();

      TypeClass type = TypeClass.getByCode(object.get("type").getAsString());
      String code = object.get("code").getAsString();

      configuration.addType(type, code);
    });

    JsonArray exclusions = JsonParser.parseString(this.getExclusions()).getAsJsonArray();

    exclusions.forEach(element -> {
      JsonObject object = element.getAsJsonObject();

      TypeClass type = TypeClass.getByCode(object.get("type").getAsString());
      String code = object.get("code").getAsString();

      configuration.addExclusions(type, code);
    });

    return configuration;
  }

  public boolean hasSameTypes(PublishDTO configuration)
  {
    return this.toDTO().hasSameTypes(configuration);
  }

}
