/**
 * Copyright (c) 2023 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism(tm).
 *
 * Geoprism(tm) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Geoprism(tm) is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;

@Component
public class GeometryUtilService
{
  public final int    SRS_CODE   = 4326;

  public final String SRS        = "EPSG:" + SRS_CODE;

  public int          MINX_INDEX = 0;

  public int          MINY_INDEX = 1;

  public int          MAXX_INDEX = 2;

  public int          MAXY_INDEX = 3;

  private Log         log        = LogFactory.getLog(GeometryUtilService.class);

  /**
   * Calculates the bounding box of all the layers.
   * 
   * @param views
   * @return double[] {minx, miny, maxx, maxy}
   */
  public double[] getBBOX(String... views)
  {
    // collect all the views and extend the bounding box
    ValueQuery union = new ValueQuery(new QueryFactory());
    if (views.length == 1)
    {
      String view = views[0];
      union.SELECT(union.aSQLClob(GeometryUtilFacade.GEOM_COLUMN, GeometryUtilFacade.GEOM_COLUMN, GeometryUtilFacade.GEOM_COLUMN));
      union.FROM(view, view);
    }
    else if (views.length > 1)
    {
      ValueQuery[] unionVQs = new ValueQuery[views.length];

      for (int i = 0; i < unionVQs.length; i++)
      {
        String view = views[i];
        ValueQuery vq = new ValueQuery(union.getQueryFactory());
        vq.SELECT(vq.aSQLClob(GeometryUtilFacade.GEOM_COLUMN, GeometryUtilFacade.GEOM_COLUMN, GeometryUtilFacade.GEOM_COLUMN));
        vq.FROM(view, view);

        unionVQs[i] = vq;
      }

      union.UNION_ALL(unionVQs);
    }
    else
    {
      throw new ProgrammingErrorException("The map has no layers");
    }

    ValueQuery collected = new ValueQuery(union.getQueryFactory());
    collected.SELECT(collected.aSQLAggregateClob("collected", "st_collect(" + GeometryUtilFacade.GEOM_COLUMN + ")", "collected"));
    collected.FROM("(" + union.getSQL() + ")", "unioned");

    ValueQuery outer = new ValueQuery(union.getQueryFactory());
    outer.SELECT(union.aSQLAggregateDouble("minx", "st_xmin(collected)"), union.aSQLAggregateDouble("miny", "st_ymin(collected)"), union.aSQLAggregateDouble("maxx", "st_xmax(collected)"), union.aSQLAggregateDouble("maxy", "st_ymax(collected)"));

    outer.FROM("(" + collected.getSQL() + ")", "collected");

    OIterator<? extends ValueObject> iter = outer.getIterator();

    try
    {
      ValueObject o = iter.next();

      double[] bbox = new double[4];
      bbox[MINX_INDEX] = Double.parseDouble(o.getValue("minx"));
      bbox[MINY_INDEX] = Double.parseDouble(o.getValue("miny"));
      bbox[MAXX_INDEX] = Double.parseDouble(o.getValue("maxx"));
      bbox[MAXY_INDEX] = Double.parseDouble(o.getValue("maxy"));

      return bbox;
    }
    catch (Exception e)
    {
      return null;
      // throw new NoLayerDataException();
    }
    finally
    {
      iter.close();
    }
  }

  public double[] getExpandedBBOX(List<String> views, double expandVal)
  {
    // collect all the views and extend the bounding box
    ValueQuery union = new ValueQuery(new QueryFactory());
    if (views.size() == 1)
    {
      String view = views.iterator().next();
      union.SELECT(union.aSQLClob(GeometryUtilFacade.GEOM_COLUMN, GeometryUtilFacade.GEOM_COLUMN, GeometryUtilFacade.GEOM_COLUMN));
      union.FROM(view, view);
    }
    else if (views.size() > 1)
    {
      ValueQuery[] unionVQs = new ValueQuery[views.size()];

      for (int i = 0; i < unionVQs.length; i++)
      {
        String view = views.get(i);
        ValueQuery vq = new ValueQuery(union.getQueryFactory());
        vq.SELECT(vq.aSQLClob(GeometryUtilFacade.GEOM_COLUMN, GeometryUtilFacade.GEOM_COLUMN, GeometryUtilFacade.GEOM_COLUMN));
        vq.FROM(view, view);

        unionVQs[i] = vq;
      }

      union.UNION_ALL(unionVQs);
    }
    else
    {
      throw new ProgrammingErrorException("The map has no layers");
    }

    ValueQuery collected = new ValueQuery(union.getQueryFactory());
    collected.SELECT(collected.aSQLAggregateClob("collected", "ST_Expand(ST_Collect(" + GeometryUtilFacade.GEOM_COLUMN + "), " + expandVal + ")", "collected"));
    collected.FROM("(" + union.getSQL() + ")", "unioned");

    ValueQuery outer = new ValueQuery(union.getQueryFactory());
    outer.SELECT(union.aSQLAggregateDouble("minx", "st_xmin(collected)"), union.aSQLAggregateDouble("miny", "st_ymin(collected)"), union.aSQLAggregateDouble("maxx", "st_xmax(collected)"), union.aSQLAggregateDouble("maxy", "st_ymax(collected)"));

    outer.FROM("(" + collected.getSQL() + ")", "collected");

    OIterator<? extends ValueObject> iter = outer.getIterator();

    try
    {
      ValueObject o = iter.next();

      double[] bbox = new double[4];
      bbox[MINX_INDEX] = Double.parseDouble(o.getValue("minx"));
      bbox[MINY_INDEX] = Double.parseDouble(o.getValue("miny"));
      bbox[MAXX_INDEX] = Double.parseDouble(o.getValue("maxx"));
      bbox[MAXY_INDEX] = Double.parseDouble(o.getValue("maxy"));

      return bbox;
    }
    catch (Exception e)
    {
      return null;
      // throw new NoLayerDataException();
    }
    finally
    {
      iter.close();
    }
  }
}
