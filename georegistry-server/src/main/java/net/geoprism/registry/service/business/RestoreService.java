package net.geoprism.registry.service.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
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
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.runwaysdk.dataaccess.MdAttributeBooleanDAOIF;
import com.runwaysdk.dataaccess.MdAttributeCharacterDAOIF;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDateDAOIF;
import com.runwaysdk.dataaccess.MdAttributeIntDAOIF;
import com.runwaysdk.dataaccess.MdAttributeNumberDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTermDAOIF;
import com.runwaysdk.dataaccess.MdAttributeTextDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.FileResource;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.cache.Cache;
import net.geoprism.registry.cache.LRUCache;
import net.geoprism.registry.etl.BusinessEdgeJsonImporter;
import net.geoprism.registry.etl.EdgeJsonImporter;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.request.GraphRepoServiceIF;
import net.geoprism.registry.service.request.ServiceFactory;
import net.geoprism.registry.xml.XMLImporter;

@Service
public class RestoreService implements RestoreServiceIF
{
  @Autowired
  private BusinessObjectBusinessServiceIF           bObjectService;

  @Autowired
  private BusinessTypeBusinessServiceIF             bTypeService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF         bEdgeTypeService;

  @Autowired
  private UndirectedGraphTypeBusinessServiceIF      ugService;

  @Autowired
  private DirectedAcyclicGraphTypeBusinessServiceIF dagService;

  @Autowired
  private GPRTransitionEventBusinessService         teService;

  @Autowired
  private GeoObjectTypeBusinessServiceIF            gTypeService;

  @Autowired
  private GeoObjectBusinessServiceIF                gObjectService;

  @Autowired
  private HierarchyTypeBusinessServiceIF            hTypeService;

  @Autowired
  private GraphRepoServiceIF                        repoService;

  @Override
  public void restoreFromBackup(File zipfile)
  {
    try
    {
      File directory = Files.createTempDirectory("gpr-restore").toFile();

      try
      {
        unzipFileToFolder(zipfile, directory);

        // 1) Restore the metadata
        restoreMetadata(directory);

        // 2) Refresh the metadata cache
        this.repoService.refreshMetadataCache();

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

        // 7) Restore the business edges
        restoreBusinessEdges(directory);
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

  protected void restoreMetadata(File directory)
  {
    File metadata = new File(directory, "metadata");

    File[] files = metadata.listFiles();

    for (File file : files)
    {
      String name = file.getName();
      String baseName = FilenameUtils.getBaseName(name);

      ServerOrganization organization = ServerOrganization.getByCode(baseName);

      try (FileResource resource = new FileResource(file))
      {
        XMLImporter importer = new XMLImporter();
        importer.importXMLDefinitions(organization, resource);
      }
    }
  }

  protected void restoreGeoObjectData(File directory) throws IOException
  {
    RegistryAdapter adapter = ServiceFactory.getAdapter();
    File data = new File(directory, "geo-objects");

    File[] files = data.listFiles();

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

            this.gObjectService.apply(object, true, true);
          }

          reader.endArray();
        }
      }
    }
  }

  protected void restoreHierarchyData(File directory) throws IOException
  {
    File data = new File(directory, "hierarchies");

    File[] files = data.listFiles();

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

            ServerGeoObjectType parentType = ServerGeoObjectType.get(parentTypeCode);
            ServerGeoObjectType childType = ServerGeoObjectType.get(childTypeCode);

            if (!cache.has(parentUid))
            {
              cache.put(parentUid, this.gObjectService.getStrategy(parentType).getGeoObjectByUid(parentUid));
            }

            if (!cache.has(childUid))
            {
              cache.put(childUid, this.gObjectService.getStrategy(childType).getGeoObjectByUid(childUid));
            }

            ServerGeoObjectIF parent = cache.get(parentUid).orElseThrow(() -> {
              return new ProgrammingErrorException("Unabled to find a geo object of type [" + parentUid + "] with a uid of [" + parentUid + "]");
            });

            ServerGeoObjectIF child = cache.get(childUid).orElseThrow(() -> {
              return new ProgrammingErrorException("Unabled to find a geo object of type [" + childUid + "] with a uid of [" + childUid + "]");
            });

            this.gObjectService.addChild(parent, child, hierarchy, startDate, endDate);
          }

          reader.endArray();
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

    for (File file : files)
    {
      try (FileResource resource = new FileResource(file))
      {
        EdgeJsonImporter importer = new EdgeJsonImporter(resource, null, null, false);
        importer.importData();
      }
    }
  }

  protected void restoreBusinessObjectData(File directory) throws IOException
  {
    File subdir = new File(directory, "business-objects");

    File[] files = subdir.listFiles();

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

  protected void restoreBusinessEdges(File directory) throws IOException
  {
    File subdir = new File(directory, "business-edges");
    File[] files = subdir.listFiles();

    for (File file : files)
    {
      try (FileResource resource = new FileResource(file))
      {
        BusinessEdgeJsonImporter importer = new BusinessEdgeJsonImporter(resource, false);
        importer.importData();
      }
    }
  }

  protected void unzipFileToFolder(File zipfile, File directory) throws IOException, FileNotFoundException
  {
    byte[] buffer = new byte[1024];

    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipfile)))
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
