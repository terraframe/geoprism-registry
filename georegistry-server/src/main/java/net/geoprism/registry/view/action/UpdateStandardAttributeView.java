/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.view.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.AlternateId;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import net.geoprism.registry.axon.event.repository.ServerGeoObjectEventBuilder;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.SourceAuthority;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.ExternalId;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.business.GPRGeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.service.business.SourceAuthorityBusinessServiceIF;

public class UpdateStandardAttributeView extends AbstractUpdateAttributeView
{

  protected JsonElement oldValue;

  protected JsonElement newValue;

  @Override
  public ServerGeoObjectEventBuilder build(ServerGeoObjectEventBuilder builder)
  {
    VertexServerGeoObject go = builder.getOrThrow(true);

    ServerGeoObjectType type = go.getType();
    AttributeType attr = type.toDTO().getAttribute(this.getAttributeName()).get();

    if (newValue != null)
    {
      Object converted;

      if (attr.getCode().equals(DefaultAttribute.ALT_IDS.getName()))
      {
        JsonArray ja = newValue.getAsJsonArray();
        List<AlternateId> ids = new ArrayList<AlternateId>();

        ja.forEach(ele -> ids.add(AlternateId.fromJSON(ele)));

        this.setAlternateIds(builder, ids);

        // ServiceFactory.getBean(GPRGeoObjectBusinessServiceIF.class).setAlternateIds(go,
        // ids);

        return builder;
      }
      else if (attr instanceof AttributeBooleanType)
      {
        converted = newValue.getAsBoolean();
      }
      else
      {
        throw new UnsupportedOperationException();
      }

      go.setValue(this.getAttributeName(), converted);
    }

    return builder;
  }

  public void setAlternateIds(ServerGeoObjectEventBuilder builder, List<AlternateId> alternateIds)
  {
    GPRGeoObjectBusinessServiceIF service = ServiceFactory.getBean(GPRGeoObjectBusinessServiceIF.class);
    SourceAuthorityBusinessServiceIF authorityService = ServiceFactory.getBean(SourceAuthorityBusinessServiceIF.class);

    if (alternateIds == null)
    {
      alternateIds = new ArrayList<>();
    }

    final List<ExternalId> olds = service.getAllExternalIds((ServerGeoObjectIF) builder.getOrThrow());
    final Set<Integer> newMatched = new HashSet<Integer>();

    for (ExternalId oldId : olds)
    {
      boolean matched = false;

      for (int i = 0; i < alternateIds.size(); ++i)
      {
        org.commongeoregistry.adapter.dataaccess.ExternalId dto = (org.commongeoregistry.adapter.dataaccess.ExternalId) alternateIds.get(i);

        if (!newMatched.contains(i) && oldId.getExternalId().equals(dto.getId()))
        {
          SourceAuthority authority = authorityService.getByCodeOrThrow(dto.getAuthority());

          builder.addExternalId(authority, dto.getId(), ImportStrategy.UPDATE_ONLY);

          matched = true;
          newMatched.add(i);
          break;
        }
      }

      if (!matched)
      {
        builder.removeExternalId(oldId.getParent());
      }
    }

    for (int i = 0; i < alternateIds.size(); ++i)
    {
      org.commongeoregistry.adapter.dataaccess.ExternalId dto = (org.commongeoregistry.adapter.dataaccess.ExternalId) alternateIds.get(i);

      if (!newMatched.contains(i))
      {
        SourceAuthority authority = authorityService.getByCodeOrThrow(dto.getAuthority());

        builder.addExternalId(authority, dto.getId(), ImportStrategy.NEW_ONLY);
      }
    }
  }

}
