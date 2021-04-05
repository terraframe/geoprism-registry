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
package net.geoprism.registry.geoobjecttype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.commongeoregistry.adapter.Optional;
import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.metadata.GeoObjectType;

import net.geoprism.registry.model.GeoObjectTypeMetadata;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.permission.PermissionContext;
import net.geoprism.registry.service.ServiceFactory;

public class GeoObjectTypeService
{
  private RegistryAdapter adapter;

  public GeoObjectTypeService(RegistryAdapter adapter)
  {
    this.adapter = adapter;
  }

  /**
   * Returns the {@link GeoObjectType}s with the given codes or all
   * {@link GeoObjectType}s if no codes are provided.
   * 
   * @param codes
   *          codes of the {@link GeoObjectType}s.
   * @param hierarchies
   * @param context
   * @return the {@link GeoObjectType}s with the given codes or all
   *         {@link GeoObjectType}s if no codes are provided.
   */
  public List<GeoObjectType> getGeoObjectTypes(String[] codes, String[] hierarchies, PermissionContext context)
  {
    List<GeoObjectType> gots;

    if (codes == null || codes.length == 0)
    {
      gots = adapter.getMetadataCache().getAllGeoObjectTypes();
    }
    else
    {
      gots = new ArrayList<GeoObjectType>(codes.length);

      for (int i = 0; i < codes.length; ++i)
      {
        Optional<GeoObjectType> optional = adapter.getMetadataCache().getGeoObjectType(codes[i]);

        if (optional.isPresent())
        {
          gots.add(optional.get());
        }
        else
        {
          net.geoprism.registry.DataNotFoundException ex = new net.geoprism.registry.DataNotFoundException();
          ex.setTypeLabel(GeoObjectTypeMetadata.sGetClassDisplayLabel());
          ex.setDataIdentifier(codes[i]);
          ex.setAttributeLabel(GeoObjectTypeMetadata.getAttributeDisplayLabel(DefaultAttribute.CODE.getName()));
          throw ex;
        }
      }
    }

    Iterator<GeoObjectType> it = gots.iterator();
    while (it.hasNext())
    {
      GeoObjectType got = it.next();

      ServerGeoObjectType serverGot = ServerGeoObjectType.get(got);

      // Filter ones that they can't see due to permissions
      if (context.equals(PermissionContext.READ))
      {
        if (!ServiceFactory.getGeoObjectTypePermissionService().canRead(serverGot.getOrganization().getCode(), serverGot, serverGot.getIsPrivate()))
        {
          it.remove();
          continue; // If we don't have continue here, then it could invoke it.remove twice which throws an error.
        }
      }
      else
      {
        if (!ServiceFactory.getGeoObjectTypePermissionService().canWrite(serverGot.getOrganization().getCode(), serverGot, serverGot.getIsPrivate()))
        {
          it.remove();
          continue; // If we don't have continue here, then it could invoke it.remove twice which throws an error.
        }
      }

      if (hierarchies != null && hierarchies.length > 0)
      {
        List<ServerHierarchyType> hts = serverGot.getHierarchies();

        boolean contains = false;
        OuterLoop: for (ServerHierarchyType ht : hts)
        {
          for (String hierarchy : hierarchies)
          {
            if (ht.getCode().equals(hierarchy))
            {
              contains = true;
              break OuterLoop;
            }
          }
        }

        if (!contains)
        {
          it.remove();
          continue;
        }
      }
    }

    return gots;
  }

}
