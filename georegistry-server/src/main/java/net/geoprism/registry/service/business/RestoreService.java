package net.geoprism.registry.service.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
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
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.resource.FileResource;

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

            ServerGeoObjectType parentType = ServerGeoObjectType.get(parentTypeCode);
            ServerGeoObjectType childType = ServerGeoObjectType.get(childTypeCode);

            ServerGeoObjectIF parent = this.gObjectService.getStrategy(parentType).getGeoObjectByUid(parentUid);
            ServerGeoObjectIF child = this.gObjectService.getStrategy(childType).getGeoObjectByUid(childUid);

            this.gObjectService.addChild(parent, child, hierarchy);
          }

          reader.endArray();
        }
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
