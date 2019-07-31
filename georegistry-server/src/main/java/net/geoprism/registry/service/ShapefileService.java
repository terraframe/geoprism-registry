/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FilenameUtils;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.RunwayException;
import com.runwaysdk.business.SmartException;
import com.runwaysdk.constants.VaultProperties;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;

import net.geoprism.gis.geoserver.SessionPredicate;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.io.GeoObjectConfiguration;
import net.geoprism.registry.io.ImportAttributeSerializer;
import net.geoprism.registry.io.ImportProblemException;
import net.geoprism.registry.io.PostalCodeFactory;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.shapefile.GeoObjectShapefileImporter;
import net.geoprism.registry.shapefile.NullLogger;

public class ShapefileService
{
  @Request(RequestType.SESSION)
  public JsonObject getShapefileConfiguration(String sessionId, String type, String fileName, InputStream fileStream)
  {
    // Save the file to the file system
    try
    {
      ServerGeoObjectType geoObjectType = ServerGeoObjectType.get(type);

      String name = SessionPredicate.generateId();

      File root = new File(new File(VaultProperties.getPath("vault.default"), "files"), name);
      root.mkdirs();

      File directory = new File(root, FilenameUtils.getBaseName(fileName));
      directory.mkdirs();

      this.extract(fileStream, directory);

      File[] dbfs = directory.listFiles(new FilenameFilter()
      {
        @Override
        public boolean accept(File dir, String name)
        {
          return name.endsWith(".dbf");
        }
      });

      if (dbfs.length > 0)
      {
        JsonArray hierarchies = ServiceFactory.getUtilities().getHierarchiesForType(geoObjectType, false);

        JsonObject object = new JsonObject();
        object.add(GeoObjectConfiguration.TYPE, this.getType(geoObjectType));
        object.add(GeoObjectConfiguration.HIERARCHIES, hierarchies);
        object.add(GeoObjectConfiguration.SHEET, this.getSheetInformation(dbfs[0]));
        object.addProperty(GeoObjectConfiguration.DIRECTORY, root.getName());
        object.addProperty(GeoObjectConfiguration.FILENAME, fileName);
        object.addProperty(GeoObjectConfiguration.HAS_POSTAL_CODE, PostalCodeFactory.isAvailable(geoObjectType));

        return object;
      }
      else
      {
        // TODO Change exception type
        throw new ProgrammingErrorException("Zip file does not contain a valid shapefile");
      }

    }
    catch (RunwayException | SmartException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  private JsonObject getType(ServerGeoObjectType geoObjectType)
  {
    JsonObject type = geoObjectType.toJSON(new ImportAttributeSerializer(Session.getCurrentLocale(), false, SupportedLocaleDAO.getSupportedLocales()));
    JsonArray attributes = type.get(GeoObjectType.JSON_ATTRIBUTES).getAsJsonArray();

    for (int i = 0; i < attributes.size(); i++)
    {
      JsonObject attribute = attributes.get(i).getAsJsonObject();
      String attributeType = attribute.get(AttributeType.JSON_TYPE).getAsString();

      attribute.addProperty(GeoObjectConfiguration.BASE_TYPE, GeoObjectConfiguration.getBaseType(attributeType));
    }

    return type;
  }

  private JsonObject getSheetInformation(File dbf)
  {
    try
    {
      ShapefileDataStore store = new ShapefileDataStore(dbf.toURI().toURL());

      try
      {
        String[] typeNames = store.getTypeNames();

        if (typeNames.length > 0)
        {
          String typeName = typeNames[0];

          FeatureSource<SimpleFeatureType, SimpleFeature> source = store.getFeatureSource(typeName);

          SimpleFeatureType schema = source.getSchema();

          List<AttributeDescriptor> descriptors = schema.getAttributeDescriptors();

          JsonObject attributes = new JsonObject();
          attributes.add(AttributeBooleanType.TYPE, new JsonArray());
          attributes.add(GeoObjectConfiguration.TEXT, new JsonArray());
          attributes.add(GeoObjectConfiguration.NUMERIC, new JsonArray());
          attributes.add(AttributeDateType.TYPE, new JsonArray());

          for (AttributeDescriptor descriptor : descriptors)
          {
            if (! ( descriptor instanceof GeometryDescriptor ))
            {
              String name = descriptor.getName().getLocalPart();
              String baseType = GeoObjectConfiguration.getBaseType(descriptor.getType());

              attributes.get(baseType).getAsJsonArray().add(name);

              if (baseType.equals(GeoObjectConfiguration.NUMERIC))
              {
                attributes.get(GeoObjectConfiguration.TEXT).getAsJsonArray().add(name);
              }
            }
          }

          JsonObject sheet = new JsonObject();
          sheet.addProperty("name", typeName);
          sheet.add("attributes", attributes);

          return sheet;
        }
        else
        {
          // TODO Change exception type
          throw new ProgrammingErrorException("Shapefile does not contain any types");
        }
      }
      finally
      {
        store.dispose();
      }
    }
    catch (RuntimeException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  public void extract(InputStream iStream, File directory)
  {
    // create a buffer to improve copy performance later.
    byte[] buffer = new byte[2048];

    try
    {
      ZipInputStream zstream = new ZipInputStream(iStream);

      ZipEntry entry;

      while ( ( entry = zstream.getNextEntry() ) != null)
      {
        File file = new File(directory, entry.getName());

        try (FileOutputStream output = new FileOutputStream(file))
        {
          int len = 0;

          while ( ( len = zstream.read(buffer) ) > 0)
          {
            output.write(buffer, 0, len);
          }
        }
      }
    }
    catch (IOException e1)
    {
      throw new ProgrammingErrorException(e1);
    }
  }

  @Request(RequestType.SESSION)
  public JsonObject importShapefile(String sessionId, String config)
  {
    GeoObjectConfiguration configuration = GeoObjectConfiguration.parse(config, false);

    String dir = configuration.getDirectory();
    String fname = configuration.getFilename();

    File root = new File(new File(VaultProperties.getPath("vault.default"), "files"), dir);
    root.mkdirs();

    File directory = new File(root, FilenameUtils.getBaseName(fname));
    directory.mkdirs();

    File[] dbfs = directory.listFiles(new FilenameFilter()
    {
      @Override
      public boolean accept(File dir, String name)
      {
        return name.endsWith(".dbf");
      }
    });

    if (dbfs.length > 0)
    {
      try
      {
        this.importShapefile(configuration, dbfs[0]);
      }
      catch (ProgrammingErrorException e)
      {
        if (e.getCause() instanceof ImportProblemException)
        {
          // Do nothing: configuration should contain the details of the problem
        }
        else
        {
          throw e;
        }
      }
    }

    return configuration.toJson();
  }

  @Transaction
  private void importShapefile(GeoObjectConfiguration configuration, File shapefile)
  {
    try
    {
      GeoObjectShapefileImporter importer = new GeoObjectShapefileImporter(shapefile, configuration);
      importer.run(new NullLogger());
    }
    catch (MalformedURLException | InvocationTargetException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Request(RequestType.SESSION)
  public InputStream exportShapefile(String sessionId, String code, String hierarchyCode)
  {
    return GeoRegistryUtil.exportShapefile(code, hierarchyCode);
  }
}
