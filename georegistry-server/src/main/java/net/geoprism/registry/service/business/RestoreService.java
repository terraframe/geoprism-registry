/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
package net.geoprism.registry.service.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.orientechnologies.common.io.OIOException;
import com.orientechnologies.orient.core.exception.OConcurrentModificationException;
import com.runwaysdk.dataaccess.MdAttributeBooleanDAOIF;
import com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDateDAOIF;
import com.runwaysdk.dataaccess.MdAttributeIntDAOIF;
import com.runwaysdk.dataaccess.MdAttributeNumberDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTextDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.resource.FileResource;
import com.runwaysdk.session.Session;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.ListType;
import net.geoprism.registry.cache.BusinessObjectCache;
import net.geoprism.registry.cache.Cache;
import net.geoprism.registry.cache.GeoObjectCache;
import net.geoprism.registry.cache.LRUCache;
import net.geoprism.registry.etl.BusinessEdgeJsonImporter;
import net.geoprism.registry.etl.EdgeJsonImporter;
import net.geoprism.registry.graph.transition.TransitionEvent;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.EdgeDirection;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.request.SerializedListTypeCache;

@Service
public class RestoreService implements RestoreServiceIF
{
  @Autowired
  private BusinessObjectBusinessServiceIF   bObjectService;

  @Autowired
  private BusinessTypeBusinessServiceIF     bTypeService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF bEdgeService;

  @Autowired
  private TransitionEventBusinessServiceIF  teService;

  @Autowired
  private GeoObjectBusinessServiceIF        gObjectService;

  @Autowired
  private GraphRepoServiceIF                repoService;

  @Override
  public void restoreFromBackup(InputStream istream)
  {
    try
    {
      File directory = Files.createTempDirectory("gpr-restore").toFile();

      try
      {
        unzipFileToFolder(istream, directory);

        // 1) Restore the metadata
        restoreMetadata(directory);

        // 2) Refresh permission
        if (Session.getCurrentSession() != null)
        {
          ( (Session) Session.getCurrentSession() ).reloadPermissions();
        }

        // 3) Import the geo object instance data
        restoreGeoObjectData(directory);

        // 4) Import the hierarchy edges between geo objects
        restoreHierarchyData(directory);

        // 5) Import DAG edges between geo objects
        restoreDagData(directory);

        // 6) Import Undirected Graph edges between geo objects
        restoreUndirectedGraphData(directory);

        // 7) Restore the business objects
        restoreBusinessObjectData(directory);

        // 8) Restore the business edges
        restoreBusinessEdges(directory);

        // 10) Restore transition events
        restoreTransitionEvents(directory);

        // 11) Restore List Type definition
        restoreListTypeData(directory);
      }
      finally
      {
        FileUtils.deleteDirectory(directory);
      }
    }
    catch (IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Transaction
  protected void restoreMetadata(File directory) throws IOException
  {
    File subdir = new File(directory, "metadata");

    File[] files = subdir.listFiles();

    if (files != null)
    {
      List<ServerOrganization> organizations = ServerOrganization.getOrganizations();

      JsonArray orgCodes = organizations.stream().map(org -> org.getCode()).collect(() -> new JsonArray(), (array, element) -> array.add(element), (listA, listB) -> {
      });

      for (File file : files)
      {
        try (FileResource resource = new FileResource(file))
        {
          try (InputStream istream = resource.openNewStream())
          {
            GeoRegistryUtil.importTypes(orgCodes.toString(), istream);

            this.repoService.refreshMetadataCache();

            SerializedListTypeCache.getInstance().clear();
          }
        }
      }
    }
  }

  protected void restoreGeoObjectData(File directory) throws IOException
  {
    RegistryAdapter adapter = ServiceFactory.getAdapter();
    File data = new File(directory, "geo-objects");

    File[] files = data.listFiles();

    if (files != null)
    {

      for (File file : files)
      {
        try (FileResource resource = new FileResource(file))
        {
          try (JsonReader reader = new JsonReader(new FileReader(file)))
          {
            reader.setLenient(true);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            reader.beginArray();

            while (reader.hasNext())
            {
              JsonObject jObject = gson.fromJson(reader, JsonObject.class);

              GeoObjectOverTime object = GeoObjectOverTime.fromJSON(adapter, jObject.toString());

              this.gObjectService.apply(object, true, true, false);
            }

            reader.endArray();
          }
        }
      }
    }
  }

  protected void restoreHierarchyData(File directory) throws IOException
  {
    File data = new File(directory, "hierarchies");

    File[] files = data.listFiles();

    if (files != null)
    {
      Cache<String, ServerGeoObjectIF> cache = new LRUCache<String, ServerGeoObjectIF>(200);

      for (File file : files)
      {
        String name = file.getName();
        String baseName = FilenameUtils.getBaseName(name);

        ServerHierarchyType hierarchy = ServerHierarchyType.get(baseName);

        try (FileResource resource = new FileResource(file))
        {
          try (JsonReader reader = new JsonReader(new FileReader(file)))
          {
            reader.setLenient(true);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            reader.beginArray();

            while (reader.hasNext())
            {
              JsonObject jObject = gson.fromJson(reader, JsonObject.class);

              String parentUid = jObject.get("parentUid").getAsString();
              String parentTypeCode = jObject.get("parentType").getAsString();
              String childUid = jObject.get("childUid").getAsString();
              String childTypeCode = jObject.get("childType").getAsString();
              Date startDate = GeoRegistryUtil.parseDate(jObject.get("startDate").getAsString());
              Date endDate = GeoRegistryUtil.parseDate(jObject.get("endDate").getAsString());
              String uid = jObject.get("uid").getAsString();

              ServerGeoObjectType parentType = ServerGeoObjectType.get(parentTypeCode);
              ServerGeoObjectType childType = ServerGeoObjectType.get(childTypeCode);

              ServerGeoObjectIF parent = cache.get(parentUid).orElseGet(() -> {
                ServerGeoObjectIF object = this.gObjectService.getStrategy(parentType).getGeoObjectByUid(parentUid);

                cache.put(parentUid, object);

                return object;
              });

              ServerGeoObjectIF child = cache.get(childUid).orElseGet(() -> {
                ServerGeoObjectIF object = this.gObjectService.getStrategy(childType).getGeoObjectByUid(childUid);

                cache.put(childUid, object);

                return object;
              });

              this.gObjectService.addChild(parent, child, hierarchy, startDate, endDate, uid, false);
            }

            reader.endArray();
          }
        }
      }
    }
  }

  protected void restoreDagData(File directory) throws IOException
  {
    restoreGraphEdges(new File(directory, "dags"));
  }

  protected void restoreUndirectedGraphData(File directory) throws IOException
  {
    restoreGraphEdges(new File(directory, "undirected-graphs"));
  }

  protected void restoreGraphEdges(File data) throws IOException
  {
    File[] files = data.listFiles();

    if (files != null)
    {

      for (File file : files)
      {
        try (FileResource resource = new FileResource(file))
        {
          EdgeJsonImporter importer = new EdgeJsonImporter(resource, null, null, false);
          importer.importData();
        }
      }
    }
  }

  protected void restoreBusinessObjectData(File directory) throws IOException
  {
    File subdir = new File(directory, "business-objects");

    File[] files = subdir.listFiles();

    if (files != null)
    {

      for (File file : files)
      {
        String name = file.getName();
        String baseName = FilenameUtils.getBaseName(name);

        BusinessType type = this.bTypeService.getByCode(baseName);

        try (FileResource resource = new FileResource(file))
        {
          try (JsonReader reader = new JsonReader(new FileReader(file)))
          {
            reader.setLenient(true);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            reader.beginArray();

            while (reader.hasNext())
            {
              JsonObject jObject = gson.fromJson(reader, JsonObject.class);

              BusinessObject object = this.bObjectService.newInstance(type);

              // TODO: Move parsing from json code into the
              // BusinessObjectBusinessService
              object.setCode(jObject.get("code").getAsString());

              JsonObject properties = jObject.get("data").getAsJsonObject();

              List<? extends MdAttributeConcreteDAOIF> mdAttributes = object.getType().getMdVertexDAO().definesAttributes();

              for (MdAttributeConcreteDAOIF mdAttribute : mdAttributes)
              {
                String attributeName = mdAttribute.definesAttribute();

                if (!attributeName.equals(BusinessObject.CODE))
                {
                  if (properties.has(attributeName))
                  {
                    if (mdAttribute instanceof MdAttributeTermDAOIF)
                    {
                      Classifier classifier = Classifier.get(properties.get(attributeName).getAsString());

                      object.setValue(attributeName, classifier.getOid());
                    }
                    else if (mdAttribute instanceof MdAttributeIntDAOIF)
                    {
                      object.setValue(attributeName, properties.get(attributeName).getAsNumber().longValue());
                    }
                    else if (mdAttribute instanceof MdAttributeNumberDAOIF)
                    {
                      object.setValue(attributeName, properties.get(attributeName).getAsNumber());
                    }
                    else if (mdAttribute instanceof MdAttributeBooleanDAOIF)
                    {
                      object.setValue(attributeName, properties.get(attributeName).getAsBoolean());
                    }
                    else if (mdAttribute instanceof MdAttributeTextDAOIF || mdAttribute instanceof MdAttributeCharacterDAOIF)
                    {
                      object.setValue(attributeName, properties.get(attributeName).getAsString());
                    }
                    else if (mdAttribute instanceof MdAttributeDateDAOIF)
                    {
                      object.setValue(attributeName, GeoRegistryUtil.parseDate(jObject.get(attributeName).getAsString()));
                    }
                  }
                }
              }

              this.bObjectService.apply(object);
            }

            reader.endArray();
          }
        }
      }
    }
  }

  protected void restoreBusinessEdges(File directory) throws IOException
  {
    File subdir = new File(directory, "business-edges");
    File[] files = subdir.listFiles();

    if (files != null)
    {
      for (File file : files)
      {
        try (FileResource resource = new FileResource(file))
        {
          BusinessEdgeJsonImporter importer = new BusinessEdgeJsonImporter(resource, false);
          importer.importData();
        }
      }
    }
  }

  protected void restoreTransitionEvents(File directory) throws IOException
  {
    File data = new File(directory, "events");

    File file = new File(data, "transition-events.json");

    if (file.exists())
    {
      try (FileResource resource = new FileResource(file))
      {
        try (JsonReader reader = new JsonReader(new FileReader(file)))
        {
          reader.setLenient(true);

          Gson gson = new GsonBuilder().setPrettyPrinting().create();

          reader.beginArray();

          while (reader.hasNext())
          {
            JsonObject jObject = gson.fromJson(reader, JsonObject.class);
            jObject.remove(TransitionEvent.OID);

            this.teService.apply(jObject);
          }

          reader.endArray();
        }
      }
    }
  }

  private void restoreListTypeData(File directory) throws IOException
  {
    File subdirectory = new File(directory, "list-type");
    subdirectory.mkdirs();

    File[] files = subdirectory.listFiles();

    if (files != null)
    {
      for (File file : files)
      {
        try (FileResource resource = new FileResource(file))
        {
          try (JsonReader reader = new JsonReader(new FileReader(file)))
          {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            JsonObject jObject = gson.fromJson(reader, JsonObject.class);
            jObject.remove(ListType.OID);

            ListType.apply(jObject);
          }
        }
      }
    }
  }

  protected void unzipFileToFolder(File zipfile, File directory) throws IOException, FileNotFoundException
  {
    try (FileInputStream istream = new FileInputStream(zipfile))
    {
      unzipFileToFolder(istream, directory);
    }
  }

  protected void unzipFileToFolder(InputStream istream, File directory) throws IOException
  {
    byte[] buffer = new byte[1024];

    try (ZipInputStream zis = new ZipInputStream(istream))
    {
      ZipEntry zipEntry = zis.getNextEntry();

      while (zipEntry != null)
      {
        File newFile = new File(directory, zipEntry.getName());

        String destDirPath = directory.getCanonicalPath();
        String destFilePath = newFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator))
        {
          throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        if (zipEntry.isDirectory())
        {
          if (!newFile.isDirectory() && !newFile.mkdirs())
          {
            throw new IOException("Failed to create directory " + newFile);
          }
        }
        else
        {
          // fix for Windows-created archives
          File parent = newFile.getParentFile();
          if (!parent.isDirectory() && !parent.mkdirs())
          {
            throw new IOException("Failed to create directory " + parent);
          }

          // write file content
          try (FileOutputStream fos = new FileOutputStream(newFile))
          {
            int len;
            while ( ( len = zis.read(buffer) ) > 0)
            {
              fos.write(buffer, 0, len);
            }
          }
        }

        zipEntry = zis.getNextEntry();
      }

      zis.closeEntry();
    }
  }

}
