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
package net.geoprism.registry.adapter;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.ontology.Term;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdBusiness;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import net.geoprism.ontology.GeoEntityUtil;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.service.ConversionService;
import net.geoprism.registry.service.RegistryIdService;

public class ServerLeafGeoObject implements ServerGeoObjectIF
{
  private Logger logger = LoggerFactory.getLogger(ServerLeafGeoObject.class);
  
  private GeoObject go;
  
  ServerLeafGeoObject(GeoObject go)
  {
    this.go = go;
  }
  
  public MdBusiness getLeafMdBusiness()
  {
    Universal uni = ConversionService.getInstance().geoObjectTypeToUniversal(go.getType());
    return uni.getMdBusiness();
  }
  
  @Override
  public Business getBusiness()
  {
    String runwayId = RegistryIdService.getInstance().registryIdToRunwayId(go.getUid(), go.getType());
    return Business.get(runwayId);
  }
  
  @Override
  public String bbox()
  {
    String definesType = this.getLeafMdBusiness().definesType();
    
    try
    {
      MdBusinessDAOIF mdBusiness = MdBusinessDAO.getMdBusinessDAO(definesType);
      MdAttributeConcreteDAOIF mdAttribute = mdBusiness.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

      StringBuffer sql = new StringBuffer();
      sql.append("SELECT ST_AsText(ST_Extent(" + mdAttribute.getColumnName() + ")) AS bbox");
      sql.append(" FROM " + mdBusiness.getTableName());
      sql.append(" WHERE " + GeoEntity.OID + " = '" + this.getBusiness().getOid() + "'");

      try (ResultSet resultSet = Database.query(sql.toString()))
      {
        if (resultSet.next())
        {
          String bbox = resultSet.getString("bbox");

          if (bbox != null)
          {
            if (bbox.contains("POLYGON"))
            {
              Pattern p = Pattern.compile("POLYGON\\(\\((.*)\\)\\)");
              Matcher m = p.matcher(bbox);

              if (m.matches())
              {
                String coordinates = m.group(1);
                List<Coordinate> coords = new LinkedList<Coordinate>();

                for (String c : coordinates.split(","))
                {
                  String[] xAndY = c.split(" ");
                  double x = Double.valueOf(xAndY[0]);
                  double y = Double.valueOf(xAndY[1]);

                  coords.add(new Coordinate(x, y));
                }

                Envelope e = new Envelope(coords.get(0), coords.get(2));

                JSONArray bboxArr = new JSONArray();
                bboxArr.put(e.getMinX());
                bboxArr.put(e.getMinY());
                bboxArr.put(e.getMaxX());
                bboxArr.put(e.getMaxY());

                return bboxArr.toString();
              }
              else
              {
                throw new ProgrammingErrorException("Pattern did not match on bbox " + bbox);
              }
            }
            else if (bbox.contains("POINT"))
            {
              Pattern p = Pattern.compile("POINT\\((.*)\\)");
              Matcher m = p.matcher(bbox);

              if (m.matches())
              {
                String sCoordinate = m.group(1);

                String[] xAndY = sCoordinate.split(" ");
                double x = Double.valueOf(xAndY[0]);
                double y = Double.valueOf(xAndY[1]);
                Coordinate coordinate = new Coordinate(x, y);

                Envelope e = new Envelope(coordinate, coordinate);

                JSONArray bboxArr = new JSONArray();
                bboxArr.put(e.getMinX());
                bboxArr.put(e.getMinY());
                bboxArr.put(e.getMaxX());
                bboxArr.put(e.getMaxY());

                return bboxArr.toString();
              }
              else
              {
                throw new ProgrammingErrorException("Pattern did not match on bbox " + bbox);
              }
            }
            else
            {
              throw new UnsupportedOperationException("Unsupported bbox geometry type for bbox [" + bbox + "].");
            }
          }
        }
      }
    }
    catch (Exception e)
    {
      logger.error("Unable to compute bounding box for leaf type [" + definesType + "].", e);
    }

    // Extent of the continental United States
    JSONArray bboxArr = new JSONArray();
    bboxArr.put(-125.0011);
    bboxArr.put(24.9493);
    bboxArr.put(-66.9326);
    bboxArr.put(49.5904);

    return bboxArr.toString();
  
  }
}
