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

import com.runwaysdk.dataaccess.database.Database;

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

    statement.append("WITH bounds AS (\n");
    statement.append("  SELECT ST_TileEnvelope(" + zoom + ", " + x + ", " + y + ") AS env,\n");
    statement.append("         ST_Transform(ST_TileEnvelope(" + zoom + ", " + x + ", " + y + ", margin => 64.0/4096), 4326) AS env_4326\n");
    statement.append("),\n");
    statement.append("candidates AS (\n");

    // UNION ALL ... one branch per table
    for (int i = 0; i < geometryTables.size(); i++)
    {
      if (i != 0)
      {
        statement.append("     UNION ALL \n");
      }

      String table = geometryTables.get(i);

      statement.append("  SELECT g.oid, g.uid, g.code, g.display_label AS label, g.geometry\n");
      statement.append("  FROM " + table + " g\n");
      statement.append("  CROSS JOIN bounds b\n");
      statement.append("  WHERE g.geometry && b.env_4326\n");
      statement.append("  AND ST_XMax(g.geometry) BETWEEN -180 AND 180\n");
      statement.append("  AND ST_YMax(g.geometry) BETWEEN -89.9 AND 89.9\n");
      statement.append("  AND EXISTS (\n");
      statement.append("    SELECT 1 FROM job_history_geometry jhg\n");
      statement.append("    WHERE jhg.parent_oid = '" + history.getOid() + "'\n");
      statement.append("    AND jhg.child_oid  = g.oid\n");
      statement.append("  )\n");
    }
    statement.append("),\n");
    statement.append("fdata AS (\n");
    statement.append("  SELECT c.oid, c.uid, c.code, c.label,\n");
    statement.append("         ST_GeometryType(c.geometry) AS gtype,\n");
    statement.append("         ST_AsMVTGeom(ST_Transform(c.geometry, 3857), b.env,\n");
    statement.append("                      extent => 4096, buffer => 64) AS geom\n");
    statement.append("  FROM candidates c CROSS JOIN bounds b\n");
    statement.append(")\n");
    statement.append("SELECT\n");
    statement.append("    (SELECT ST_AsMVT(t, 'polygon') FROM (SELECT oid, uid, code, label, geom FROM fdata\n");
    statement.append("       WHERE gtype = 'ST_MultiPolygon'    AND geom IS NOT NULL) t)\n");
    statement.append(" || (SELECT ST_AsMVT(t, 'line')    FROM (SELECT oid, uid, code, label, geom FROM fdata\n");
    statement.append("       WHERE gtype = 'ST_MultiLineString' AND geom IS NOT NULL) t)\n");
    statement.append(" || (SELECT ST_AsMVT(t, 'point')   FROM (SELECT oid, uid, code, label, geom FROM fdata\n");
    statement.append("       WHERE gtype = 'ST_MultiPoint'      AND geom IS NOT NULL) t)\n");
    statement.append(" AS mvt;\n");

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
