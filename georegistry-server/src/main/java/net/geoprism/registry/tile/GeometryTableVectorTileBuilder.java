/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either history 3 of the License, or (at your
 * option) any later history.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.tile;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.system.gis.geo.GeoEntity;

import net.geoprism.registry.jobs.GPRJobHistory;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.view.TypeClass;

public class GeometryTableVectorTileBuilder
{
  private GPRJobHistory history;

  public GeometryTableVectorTileBuilder(GPRJobHistory history)
  {
    this.history = history;
  }

  public byte[] write(int zoom, int x, int y)
  {
    List<String> geometryTables = history.getTypesAsList().stream() //
        .filter(t -> t.getTypeClass().equals(TypeClass.GEO_OBJECT_TYPE)) //
        .map(t -> ServerGeoObjectType.get(t.getTypeCode())) //
        .map(t -> t.getGeometryTable().getTableName()) //
        .toList();

    if (geometryTables.size() == 0)
    {
      return new byte[] {};
    }

    StringBuilder statement = new StringBuilder();

    // Filter the data to remove entries which have points too close the poles
    // Those points cannot be transformed
    statement.append("WITH _fdata AS (" + "\n");
    statement.append(" SELECT geom_tab.* " + "\n");
    statement.append(" FROM job_history_geometry AS jhg " + "\n");
    statement.append(" JOIN ( " + "\n");
    statement.append("   SELECT * " + "\n");
    statement.append("   FROM " + "\n");

    for (int i = 0; i < geometryTables.size(); i++)
    {
      if (i == 0)
      {
        statement.append("     " + geometryTables.get(i) + "\n");
      }
      else
      {
        statement.append("     UNION ALL ( SELECT * FROM " + geometryTables.get(i) + ")\n");
      }
    }

    statement.append("   ) AS geom_tab ON geom_tab.oid = jhg.child_oid \n");

    statement.append(" WHERE jhg.parent_oid = '" + history.getOid() + "'" + "\n");
    statement.append(" AND (ST_XMax(geom_tab.geometry) BETWEEN -180 AND 180)" + "\n");
    statement.append(" AND (ST_YMax(geom_tab.geometry) BETWEEN -89.9 AND 89.9)" + "\n");
    statement.append(")," + "\n");

    // Generate geometry layers
    statement.append(generateTileStatement(zoom, x, y, "ST_MultiPolygon", "polygon") + ",\n");
    statement.append(generateTileStatement(zoom, x, y, "ST_MultiLine", "line") + ",\n");
    statement.append(generateTileStatement(zoom, x, y, "ST_MultiPoint", "point") + "\n");

    // Create the tile layer
    statement.append("SELECT");
    statement.append(" (polygon_tile.mvt || point_tile.mvt || line_tile.mvt) AS mvt" + "\n");
    statement.append("FROM polygon_tile, point_tile, line_tile" + "\n");

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

  public String generateTileStatement(int zoom, int x, int y, String geometryType, String layername)
  {
    StringBuilder statement = new StringBuilder();
    statement.append("mvt" + layername + " AS (" + "\n");
    statement.append(" SELECT " + "\n");
    statement.append("  ge.oid AS " + GeoEntity.OID + "\n");
    statement.append(", ge.uid AS " + DefaultAttribute.UID.getName() + "\n");
    statement.append(", ge.code AS " + DefaultAttribute.CODE.getName() + "\n");
    statement.append(", ge.display_label AS label" + "\n");
    statement.append(", ST_AsMVTGeom(" + "\n");
    statement.append("    ST_Transform( ge.geometry, 3857 )" + "\n");
    statement.append("    , ST_TileEnvelope(" + zoom + ", " + x + ", " + y + ")" + "\n");
    statement.append("    , extent => 4096" + "\n");
    statement.append("    , buffer => 64" + "\n");
    statement.append("  ) AS " + VectorTileBuilder.GEOM_COLUMN + "\n");
    statement.append(" FROM _fdata AS ge" + "\n");
    statement.append(" WHERE ST_GeometryType(ge.geometry) = '" + geometryType + "' \n");
    statement.append(" AND ST_Transform( ge.geometry, 3857 ) && ST_TileEnvelope(" + zoom + ", " + x + ", " + y + ", margin => (64.0 / 4096))" + "\n");
    statement.append(")," + "\n");
    statement.append(layername + "_tile AS (");
    statement.append(" SELECT ST_AsMVT(mvt" + layername + ".*, '" + layername + "') AS mvt" + "\n");
    statement.append(" FROM mvt" + layername + "\n");
    statement.append(")");

    return statement.toString();
  }
}
