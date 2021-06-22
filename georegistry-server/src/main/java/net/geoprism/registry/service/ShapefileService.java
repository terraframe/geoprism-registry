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
package net.geoprism.registry.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;

import com.runwaysdk.RunwayException;
import com.runwaysdk.business.SmartException;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.CloseableFile;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.VaultFile;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.conversion.SupportedLocaleCache;
import net.geoprism.registry.etl.FormatSpecificImporterFactory.FormatImporterType;
import net.geoprism.registry.etl.ObjectImporterFactory;
import net.geoprism.registry.etl.ShapefileFormatException;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.etl.upload.ShapefileImporter;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.io.ImportAttributeSerializer;
import net.geoprism.registry.io.PostalCodeFactory;
import net.geoprism.registry.model.ServerGeoObjectType;

public class ShapefileService
{
  @Request(RequestType.SESSION)
  public JSONObject getShapefileConfiguration(String sessionId, String type, Date startDate, Date endDate, String fileName, InputStream fileStream, ImportStrategy strategy, Boolean copyBlank)
  {
    // Save the file to the file system
    try
    {
      ServerGeoObjectType geoObjectType = ServerGeoObjectType.get(type);

      VaultFile vf = VaultFile.createAndApply(fileName, fileStream);

      try (CloseableFile dbf = ShapefileImporter.getShapefileFromResource(vf, "dbf"))
      {
        SimpleDateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);
        format.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);


        JSONObject object = new JSONObject();
        object.put(GeoObjectImportConfiguration.TYPE, this.getType(geoObjectType));
        object.put(GeoObjectImportConfiguration.SHEET, this.getSheetInformation(dbf));
        object.put(ImportConfiguration.VAULT_FILE_ID, vf.getOid());
        object.put(ImportConfiguration.FILE_NAME, fileName);
        object.put(GeoObjectImportConfiguration.HAS_POSTAL_CODE, PostalCodeFactory.isAvailable(geoObjectType));
        object.put(ImportConfiguration.IMPORT_STRATEGY, strategy.name());
        object.put(ImportConfiguration.FORMAT_TYPE, FormatImporterType.SHAPEFILE.name());
        object.put(ImportConfiguration.OBJECT_TYPE, ObjectImporterFactory.ObjectImportType.GEO_OBJECT.name());
        object.put(ImportConfiguration.COPY_BLANK, copyBlank);

        if (startDate != null)
        {
          object.put(GeoObjectImportConfiguration.START_DATE, format.format(startDate));
        }

        if (endDate != null)
        {
          object.put(GeoObjectImportConfiguration.END_DATE, format.format(endDate));
        }

        return object;
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

  private JSONObject getType(ServerGeoObjectType geoObjectType)
  {
    JSONObject type = new JSONObject(geoObjectType.toJSON(new ImportAttributeSerializer(Session.getCurrentLocale(), false, false, true, SupportedLocaleCache.getLocales())).toString());
    JSONArray attributes = type.getJSONArray(GeoObjectType.JSON_ATTRIBUTES);

    for (int i = 0; i < attributes.length(); i++)
    {
      JSONObject attribute = attributes.getJSONObject(i);
      String attributeType = attribute.getString(AttributeType.JSON_TYPE);

      attribute.put(GeoObjectImportConfiguration.BASE_TYPE, GeoObjectImportConfiguration.getBaseType(attributeType));
    }

    return type;
  }

  private JSONObject getSheetInformation(File dbf)
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

          JSONObject attributes = new JSONObject();
          attributes.put(AttributeBooleanType.TYPE, new JSONArray());
          attributes.put(GeoObjectImportConfiguration.TEXT, new JSONArray());
          attributes.put(GeoObjectImportConfiguration.NUMERIC, new JSONArray());
          attributes.put(AttributeDateType.TYPE, new JSONArray());

          for (AttributeDescriptor descriptor : descriptors)
          {
            if (! ( descriptor instanceof GeometryDescriptor ))
            {
              String name = descriptor.getName().getLocalPart();
              String baseType = GeoObjectImportConfiguration.getBaseType(descriptor.getType());

              attributes.getJSONArray(baseType).put(name);

              if (baseType.equals(GeoObjectImportConfiguration.NUMERIC))
              {
                attributes.getJSONArray(GeoObjectImportConfiguration.TEXT).put(name);
              }
            }
          }

          JSONObject sheet = new JSONObject();
          sheet.put("name", typeName);
          sheet.put("attributes", attributes);

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
      Throwable cause = e.getCause();

      if (cause instanceof IOException)
      {
        throw new ShapefileFormatException(e);
      }

      throw e;
    }
    catch (Exception e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Request(RequestType.SESSION)
  public void cancelImport(String sessionId, String json)
  {
    ImportConfiguration config = ImportConfiguration.build(json);

    String id = config.getVaultFileId();

    VaultFile.get(id).delete();
  }

  @Request(RequestType.SESSION)
  public InputStream exportShapefile(String sessionId, String code, String hierarchyCode)
  {
    return GeoRegistryUtil.exportShapefile(code, hierarchyCode);
  }
}
