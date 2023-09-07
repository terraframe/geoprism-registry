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
package net.geoprism.registry.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.conversion.OrganizationConverter;
import net.geoprism.registry.model.OrganizationMetadata;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.view.Page;

@Component
public class OrganizationService
{
  /**
   * Returns the {@link OrganizationDTO}s with the given codes or all
   * {@link OrganizationDTO}s if no codes are provided.
   * 
   * @param sessionId
   * @param codes
   *          codes of the {@link OrganizationDTO}s.
   * @return the {@link OrganizationDTO}s with the given codes or all
   *         {@link OrganizationDTO}s if no codes are provided.
   */
  @Request(RequestType.SESSION)
  public OrganizationDTO[] getOrganizations(String sessionId, String[] codes)
  {
    List<OrganizationDTO> orgs = new LinkedList<OrganizationDTO>();

    if (codes == null || codes.length == 0)
    {
      List<OrganizationDTO> cachedOrgs = ServiceFactory.getAdapter().getMetadataCache().getAllOrganizations();

      for (OrganizationDTO cachedOrg : cachedOrgs)
      {
        orgs.add(cachedOrg);
      }
    }
    else
    {
      for (int i = 0; i < codes.length; ++i)
      {
        Optional<OrganizationDTO> optional = ServiceFactory.getAdapter().getMetadataCache().getOrganization(codes[i]);

        if (optional.isPresent())
        {
          orgs.add(optional.get());
        }
        else
        {
          net.geoprism.registry.DataNotFoundException ex = new net.geoprism.registry.DataNotFoundException();
          ex.setTypeLabel(OrganizationMetadata.get().getClassDisplayLabel());
          ex.setDataIdentifier(codes[i]);
          ex.setAttributeLabel(OrganizationMetadata.get().getAttributeDisplayLabel(DefaultAttribute.CODE.getName()));
          throw ex;
        }
      }
    }

    // Filter out orgs based on permissions
    Iterator<OrganizationDTO> it = orgs.iterator();
    while (it.hasNext())
    {
      OrganizationDTO orgDTO = it.next();

      if (!ServiceFactory.getOrganizationPermissionService().canActorRead(orgDTO.getCode()))
      {
        it.remove();
      }
    }

    // Group by depth first parent
    Map<String, List<OrganizationDTO>> map = new HashMap<>();

    orgs.forEach(org -> {
      String parentCode = org.getParentCode() != null ? org.getParentCode() : "";

      map.putIfAbsent(parentCode, new LinkedList<>());

      map.get(parentCode).add(org);
    });

    List<OrganizationDTO> list = new LinkedList<OrganizationDTO>();

    this.sortOrganizations(map, list, "");

    return list.toArray(new OrganizationDTO[orgs.size()]);
  }

  private void sortOrganizations(Map<String, List<OrganizationDTO>> map, List<OrganizationDTO> list, String code)
  {
    map.get(code).forEach(org -> {
      list.add(org);

      if (map.containsKey(org.getCode()))
      {
        this.sortOrganizations(map, list, org.getCode());
      }
    });
  }

  /**
   * Creates a {@link OrganizationDTO} from the given JSON.
   * 
   * @param sessionId
   * @param json
   *          JSON of the {@link OrganizationDTO} to be created.
   * @return newly created {@link OrganizationDTO}
   */
  @Request(RequestType.SESSION)
  public OrganizationDTO createOrganization(String sessionId, String json)
  {
    OrganizationDTO organizationDTO = OrganizationDTO.fromJSON(json);

    final ServerOrganization org = new OrganizationConverter().create(organizationDTO);

    // If this did not error out then add to the cache
    ServiceFactory.getMetadataCache().addOrganization(org);

    return ServiceFactory.getAdapter().getMetadataCache().getOrganization(org.getCode()).get();
  }

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
  @Request(RequestType.SESSION)
  public OrganizationDTO updateOrganization(String sessionId, String json)
  {
    OrganizationDTO organizationDTO = OrganizationDTO.fromJSON(json);

    final ServerOrganization org = new OrganizationConverter().update(organizationDTO);

    // If this did not error out then add to the cache
    ServiceFactory.getMetadataCache().addOrganization(org);

    SerializedListTypeCache.getInstance().clear();

    return ServiceFactory.getAdapter().getMetadataCache().getOrganization(org.getCode()).get();
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
    ServerOrganization organization = ServerOrganization.getByCode(code);

    ServiceFactory.getOrganizationPermissionService().enforceActorCanDelete();

    organization.delete();

    SerializedListTypeCache.getInstance().clear();

    // If this did not error out then remove from the cache
    ServiceFactory.getMetadataCache().removeOrganization(code);
  }

  @Request(RequestType.SESSION)
  public void addChild(String sessionId, String parentCode, String childCode)
  {
    ServerOrganization parent = ServerOrganization.getByCode(parentCode);
    ServerOrganization child = ServerOrganization.getByCode(childCode);

    parent.addChild(child);

    ServiceFactory.getMetadataCache().addOrganization(child);
  }

  @Request(RequestType.SESSION)
  public void removeChild(String sessionId, String parentCode, String childCode)
  {
    ServerOrganization parent = ServerOrganization.getByCode(parentCode);
    ServerOrganization child = ServerOrganization.getByCode(childCode);

    parent.removeChild(child);

    ServiceFactory.getMetadataCache().addOrganization(child);
  }

  @Request(RequestType.SESSION)
  public JsonObject getChildren(String sessionId, String code, Integer pageSize, Integer pageNumber)
  {
    if (code != null)
    {
      ServerOrganization parent = ServerOrganization.getByCode(code);

      return parent.getChildren(pageSize, pageNumber).toJSON();
    }

    List<ServerOrganization> roots = ServerOrganization.getRoots();

    return new Page<ServerOrganization>(roots.size(), pageNumber, pageSize, roots).toJSON();
  }

  @Request(RequestType.SESSION)
  public JsonObject getAncestorTree(String sessionId, String rootCode, String code, Integer pageSize)
  {
    ServerOrganization child = ServerOrganization.getByCode(code);

    return child.getAncestorTree(rootCode, pageSize).toJSON();
  }

  @Request(RequestType.SESSION)
  public void move(String sessionId, String code, String parentCode)
  {
    ServerOrganization organization = ServerOrganization.getByCode(code);
    ServerOrganization newParent = ServerOrganization.getByCode(parentCode);

    organization.move(newParent);

    // Rebuild the entire organization cache
    ServiceFactory.getMetadataCache().addOrganization(organization);
  }

  @Request(RequestType.SESSION)
  public void removeAllParents(String sessionId, String code)
  {
    ServerOrganization organization = ServerOrganization.getByCode(code);

    organization.removeAllParents();

    // Rebuild the entire organization cache
    ServiceFactory.getMetadataCache().addOrganization(organization);
  }

}
