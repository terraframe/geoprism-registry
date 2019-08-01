package net.geoprism.registry.model;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.Business;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import net.geoprism.registry.RegistryConstants;

public class ServerLeafGeoObject implements ServerGeoObjectIF
{
  private Logger              logger = LoggerFactory.getLogger(ServerLeafGeoObject.class);

  private ServerGeoObjectType type;

  private GeoObject           geoObject;

  private Business            business;

  public ServerLeafGeoObject(ServerGeoObjectType type, GeoObject go, Business business)
  {
    this.type = type;
    this.geoObject = go;
    this.business = business;
  }

  @Override
  public ServerGeoObjectType getType()
  {
    return this.type;
  }

  public Business getBusiness()
  {
    return business;
  }

  public GeoObject getGeoObject()
  {
    return geoObject;
  }

  @Override
  public String bbox()
  {
    String definesType = this.type.definesType();

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
