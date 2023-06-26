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

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import org.locationtech.jts.geom.Envelope;

public class PublisherUtil
{
  public static Envelope getTileBounds(JSONObject object)
  {
    try
    {
      int x = object.getInt("x");
      int y = object.getInt("y");
      int zoom = object.getInt("z");

      return PublisherUtil.getTileBounds(x, y, zoom);
    }
    catch (JSONException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public static Envelope getEnvelope(JSONObject object)
  {
    try
    {
      int x = object.getInt("x");
      int y = object.getInt("y");
      int zoom = object.getInt("z");

      return PublisherUtil.getEnvelope(x, y, zoom);
    }
    catch (JSONException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public static Envelope getTileBounds(int x, int y, int zoom)
  {
    Envelope envelope = getEnvelope(x, y, zoom);
    return getTileBounds(envelope);
  }

  public static Envelope getTileBounds(Envelope envelope)
  {
    try
    {
      CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326", true);
      CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:3857", true);
      MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);

      return JTS.transform(envelope, transform);
    }
    catch (FactoryException | TransformException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public static Envelope getEnvelope(int x, int y, int zoom)
  {
    return new Envelope(getLong(x, zoom), getLong(x + 1, zoom), getLat(y, zoom), getLat(y + 1, zoom));
  }

  public static double getLong(int x, int zoom)
  {
    return ( x / Math.pow(2, zoom) * 360 - 180 );
  }

  public static double getLat(int y, int zoom)
  {
    double n = Math.PI - 2 * Math.PI * y / Math.pow(2, zoom);
    // return ( 180 / Math.PI * Math.atan(0.5 * ( Math.exp(n) - Math.exp(-n) )) );
    return Math.toDegrees(Math.atan(Math.sinh(n)));
  }
}
