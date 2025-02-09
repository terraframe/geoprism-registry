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
package net.geoprism.registry.tile;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;

import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.localization.LocalizationFacade;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.mapping.GeoserverFacade;

import net.geoprism.registry.ListTypeVersion;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.RegistryConstants;

public class PostgisVectorTileBuilder
{
  private Set<Locale>     locales;

  private String          column;

  private String          labelColumn;

  private MdBusinessDAOIF mdBusiness;

  public PostgisVectorTileBuilder(ListTypeVersion version)
  {
    this.mdBusiness = MdBusinessDAO.get(version.getMdBusinessOid());

    MdAttributeConcreteDAOIF geomAttribute = mdBusiness.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
    MdAttributeConcreteDAOIF labelAttribute = mdBusiness.definesAttribute(DefaultAttribute.DISPLAY_LABEL.getName() + MasterListVersion.DEFAULT_LOCALE);

    this.locales = LocalizationFacade.getInstalledLocales();
    this.column = geomAttribute.getColumnName();
    this.labelColumn = labelAttribute.getColumnName();
  }

  public byte[] write(int zoom, int x, int y)
  {
    StringBuilder statement = new StringBuilder();

    // Filter the data to remove entries which have points too close the poles
    // Those points cannot be transformed
    statement.append("WITH _fdata AS (" + "\n");
    statement.append(" SELECT * FROM " + mdBusiness.getTableName() + "\n");
    statement.append(" WHERE (ST_XMax(geom) BETWEEN -180 AND 180)" + "\n");
    statement.append(" AND (ST_YMax(geom) BETWEEN -89.9 AND 89.9)" + "\n");
    statement.append(")," + "\n");

    // Get the properties used in the tile and convert the geometry to its tile format
    statement.append("mvtgeom AS (" + "\n");
    statement.append(" SELECT " + "\n");
    statement.append("  ge.oid AS " + GeoEntity.OID + "\n");
    statement.append(", ge.uid AS " + DefaultAttribute.UID.getName() + "\n");
    statement.append(", ge.code AS" + DefaultAttribute.CODE.getName() + "\n");
    statement.append(", ge." + labelColumn + " AS label" + "\n");

    for (Locale locale : locales)
    {
      MdAttributeConcreteDAOIF localeAttribute = mdBusiness.definesAttribute(DefaultAttribute.DISPLAY_LABEL.getName() + locale.toString());

      if (localeAttribute != null)
      {
        statement.append(", ge." + localeAttribute.getColumnName() + " AS label_" + locale.toString().toLowerCase() + "\n");
      }
    }
    statement.append(", ST_AsMVTGeom(" + "\n");
    statement.append("    ST_Transform( ge." + column + ", 3857 )" + "\n");
    statement.append("    , ST_TileEnvelope(" + zoom + ", " + x + ", " + y + ")" + "\n");
    statement.append("    , extent => 4096" + "\n");
    statement.append("    , buffer => 64" + "\n");
    statement.append("  ) AS " + GeoserverFacade.GEOM_COLUMN + "\n");
    statement.append(" FROM _fdata AS ge" + "\n");
    statement.append(" WHERE ST_Transform( ge." + column + ", 3857 ) && ST_TileEnvelope(" + zoom + ", " + x + ", " + y + ", margin => (64.0 / 4096))" + "\n");
    statement.append(")" + "\n");
    
    // Create the tile layer
    statement.append("SELECT ST_AsMVT(mvtgeom.*, 'context')" + "\n");
    statement.append("FROM mvtgeom;" + "\n");

    try (ResultSet result = Database.query(statement.toString()))
    {
      if (result.next())
      {
        return result.getBytes(1);
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }

    return new byte[] {};

  }
}
