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
package net.geoprism.registry.service.request;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.service.business.CacheProviderIF;
import net.geoprism.registry.service.business.GPROrganizationBusinessService;

@Service
@Primary
public class GPROrganizationService extends OrganizationService implements OrganizationServiceIF
{
  @Autowired
  private CacheProviderIF                provider;

  @Autowired
  private GPROrganizationBusinessService service;

  /**
   * Updates the given {@link OrganizationDTO} represented as JSON.
   * 
   * @pre given {@link OrganizationDTO} must already exist.
   * 
   * @param sessionId
   * @param json
   *          JSON of the {@link OrganizationDTO} to be updated.
   * @return updated {@link OrganizationDTO}
   */
  @Override
  public OrganizationDTO updateOrganization(String sessionId, String json)
  {
    OrganizationDTO dto = super.updateOrganization(sessionId, json);

    SerializedListTypeCache.getInstance().clear();

    return dto;
  }

  /**
   * Deletes the {@link OrganizationDTO} with the given code.
   * 
   * @param sessionId
   * @param code
   *          code of the {@link OrganizationDTO} to delete.
   */
  @Request(RequestType.SESSION)
  public void deleteOrganization(String sessionId, String code)
  {
    super.deleteOrganization(sessionId, code);

    SerializedListTypeCache.getInstance().clear();
  }

  @Request(RequestType.SESSION)
  public void importFile(String sessionId, MultipartFile file) throws IOException
  {
    try (InputStream istream = file.getInputStream())
    {
      try (InputStreamReader reader = new InputStreamReader(istream))
      {
        JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();

        this.service.importJsonTree(array);
      }
    }

    provider.getServerCache().refresh();
  }

}
