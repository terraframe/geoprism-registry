/**
 * Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.tile;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.json.JSONException;
import org.postgis.jts.JtsGeometry;

import com.runwaysdk.dataaccess.AttributeIF;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;
import com.runwaysdk.gis.dataaccess.AttributeGeometryIF;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.mapping.GeoserverFacade;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.wdtinc.mapbox_vector_tile.VectorTile;
import com.wdtinc.mapbox_vector_tile.VectorTile.Tile;
import com.wdtinc.mapbox_vector_tile.VectorTile.Tile.Layer;
import com.wdtinc.mapbox_vector_tile.adapt.jts.IGeometryFilter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.IUserDataConverter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.JtsAdapter;
import com.wdtinc.mapbox_vector_tile.adapt.jts.TileGeomResult;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerBuild;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerParams;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerProps;

import net.geoprism.ontology.UserDataConverter;
import net.geoprism.ontology.VectorLayerPublisherIF;
import net.geoprism.registry.MasterListVersion;
import net.geoprism.registry.RegistryConstants;

public class VectorTileBuilder implements VectorLayerPublisherIF
{
  private MasterListVersion version;

  private List<Locale>      locales;

  public VectorTileBuilder(MasterListVersion version)
  {
    this.version = version;
    this.locales = SupportedLocaleDAO.getSupportedLocales();
  }

  private ResultSet getResultSet()
  {
    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(this.version.getMdBusinessOid());
    MdAttributeConcreteDAOIF geomAttribute = mdBusiness.definesAttribute(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);

    MdAttributeConcreteDAOIF labelAttribute = mdBusiness.definesAttribute(DefaultAttribute.DISPLAY_LABEL.getName() + MasterListVersion.DEFAULT_LOCALE);

    String column = geomAttribute.getColumnName();
    String labelColumn = labelAttribute.getColumnName();

    StringBuilder sql = new StringBuilder();
    sql.append("SELECT ge.oid");
    sql.append(", ge." + labelColumn + " AS default_locale");

    for (Locale locale : locales)
    {
      MdAttributeConcreteDAOIF localeAttribute = mdBusiness.definesAttribute(DefaultAttribute.DISPLAY_LABEL.getName() + locale.toString());

      if (localeAttribute != null)
      {
        sql.append(", ge." + localeAttribute.getColumnName() + " AS " + locale.toString());
      }
    }

    sql.append(", ST_Transform(ge." + column + ", 3857) AS " + GeoserverFacade.GEOM_COLUMN + "\n");
    sql.append("FROM " + mdBusiness.getTableName() + " AS ge\n");
    sql.append("WHERE ge." + column + " IS NOT NULL\n");

    return Database.query(sql.toString());
  }

  @Override
  public List<Layer> writeVectorLayers(Envelope envelope, Envelope bounds)
  {
    try (ResultSet resultSet = this.getResultSet())
    {
      List<Layer> layers = new LinkedList<Layer>();
      layers.add(this.writeVectorLayer("context", bounds, resultSet));

      return layers;
    }
    catch (JSONException | IOException | SQLException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public byte[] writeVectorTiles(Envelope envelope, Envelope bounds)
  {
    // Add built layer to MVT
    final VectorTile.Tile.Builder builder = VectorTile.Tile.newBuilder();

    List<Layer> layers = this.writeVectorLayers(envelope, bounds);

    for (Layer layer : layers)
    {
      builder.addLayers(layer);
    }

    /// Build MVT
    Tile mvt = builder.build();

    return mvt.toByteArray();
  }

  protected byte[] writeVectorTiles(String layerName, Envelope envelope, ValueQuery query) throws IOException
  {
    OIterator<ValueObject> iterator = query.getIterator();

    try
    {
      List<Geometry> geometries = new LinkedList<Geometry>();

      while (iterator.hasNext())
      {
        ValueObject object = iterator.next();

        AttributeGeometryIF attributeIF = (AttributeGeometryIF) object.getAttributeIF(GeoserverFacade.GEOM_COLUMN);

        Geometry geometry = attributeIF.getGeometry();
        geometry.setUserData(this.getUserData(object));

        geometries.add(geometry);
      }

      GeometryFactory geomFactory = new GeometryFactory();
      IGeometryFilter acceptAllGeomFilter = geometry -> true;

      MvtLayerParams layerParams = new MvtLayerParams();

      TileGeomResult tileGeom = JtsAdapter.createTileGeom(geometries, envelope, geomFactory, layerParams, acceptAllGeomFilter);

      final VectorTile.Tile.Builder tileBuilder = VectorTile.Tile.newBuilder();

      // Create MVT layer
      final MvtLayerProps layerProps = new MvtLayerProps();
      final IUserDataConverter ignoreUserData = new UserDataConverter();

      // MVT tile geometry to MVT features
      final List<VectorTile.Tile.Feature> features = JtsAdapter.toFeatures(tileGeom.mvtGeoms, layerProps, ignoreUserData);

      final VectorTile.Tile.Layer.Builder layerBuilder = MvtLayerBuild.newLayerBuilder(layerName, layerParams);
      layerBuilder.addAllFeatures(features);

      MvtLayerBuild.writeProps(layerBuilder, layerProps);

      // Build MVT layer
      final VectorTile.Tile.Layer layer = layerBuilder.build();

      // Add built layer to MVT
      tileBuilder.addLayers(layer);

      /// Build MVT
      Tile mvt = tileBuilder.build();

      return mvt.toByteArray();
    }
    finally
    {
      iterator.close();
    }
  }

  protected byte[] writeVectorTiles(String layerName, Envelope envelope, ResultSet resultSet) throws IOException
  {
    // Add built layer to MVT
    final VectorTile.Tile.Builder builder = VectorTile.Tile.newBuilder();

    builder.addLayers(this.writeVectorLayer(layerName, envelope, resultSet));

    /// Build MVT
    Tile mvt = builder.build();

    return mvt.toByteArray();
  }

  public VectorTile.Tile.Layer writeVectorLayer(String layerName, Envelope bounds, ResultSet resultSet) throws IOException
  {
    try
    {
      List<Geometry> geometries = new LinkedList<Geometry>();

      while (resultSet.next())
      {
        String label = resultSet.getString("default_locale");

        Map<String, String> data = new TreeMap<String, String>();
        data.put(GeoEntity.OID, resultSet.getString("oid"));
        data.put(GeoEntity.DISPLAYLABEL, label);

        for (Locale locale : locales)
        {
          data.put(GeoEntity.DISPLAYLABEL + "_" + locale.toString().toLowerCase(), resultSet.getString(locale.toString()));
        }

        // data.put(GeoEntity.GEOID, resultSet.getString("geo_id"));
        // data.put("height", "15"); // TODO: This should be set on the
        // GeoEntity
        // data.put("base", "0"); // TODO: This should be set on the GeoEntity
        // data.put("isClickable", "true");

        JtsGeometry geom = (JtsGeometry) resultSet.getObject(GeoserverFacade.GEOM_COLUMN);

        if (geom != null)
        {
          Geometry geometry = geom.getGeometry();
          geometry.setUserData(data);

          geometries.add(geometry);
        }
      }

      GeometryFactory geomFactory = new GeometryFactory();
      IGeometryFilter acceptAllGeomFilter = geometry -> true;

      MvtLayerParams layerParams = new MvtLayerParams();

      TileGeomResult tileGeom = JtsAdapter.createTileGeom(geometries, bounds, geomFactory, layerParams, acceptAllGeomFilter);

      // Create MVT layer
      final MvtLayerProps layerProps = new MvtLayerProps();
      final IUserDataConverter ignoreUserData = new UserDataConverter();

      // MVT tile geometry to MVT features
      final List<VectorTile.Tile.Feature> features = JtsAdapter.toFeatures(tileGeom.mvtGeoms, layerProps, ignoreUserData);

      final VectorTile.Tile.Layer.Builder layerBuilder = MvtLayerBuild.newLayerBuilder(layerName, layerParams);
      layerBuilder.addAllFeatures(features);

      MvtLayerBuild.writeProps(layerBuilder, layerProps);

      // Build MVT layer
      return layerBuilder.build();
    }
    catch (SQLException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  protected Map<String, String> getUserData(ValueObject object)
  {
    AttributeIF[] attributes = object.getAttributeArrayIF();

    Map<String, String> data = new TreeMap<String, String>();

    for (AttributeIF attribute : attributes)
    {
      String name = attribute.getName();

      if (!name.equals(GeoserverFacade.GEOM_COLUMN))
      {
        data.put(name, attribute.getValue());
      }
    }

    return data;
  }

}
