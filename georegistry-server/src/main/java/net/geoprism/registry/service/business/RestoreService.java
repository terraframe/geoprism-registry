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
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.runwaysdk.business.SmartException;
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
import com.runwaysdk.resource.StreamResource;
import com.runwaysdk.session.Session;

import net.geoprism.GenericException;
import net.geoprism.GeoprismUser;
import net.geoprism.ontology.Classifier;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.ListType;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.UserInfo;
import net.geoprism.registry.cache.Cache;
import net.geoprism.registry.cache.LRUCache;
import net.geoprism.registry.etl.BusinessEdgeJsonImporter;
import net.geoprism.registry.etl.EdgeJsonImporter;
import net.geoprism.registry.graph.BusinessEdgeType;
import net.geoprism.registry.graph.BusinessType;
import net.geoprism.registry.graph.DirectedAcyclicGraphType;
import net.geoprism.registry.graph.UndirectedGraphType;
import net.geoprism.registry.graph.transition.TransitionEvent;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.ServerElement;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.request.SerializedListTypeCache;
import net.geoprism.registry.view.RestoreSummary;
import net.geoprism.registry.xml.XMLImporter;

@Service
public class RestoreService implements RestoreServiceIF
{
  private static final Logger logger = LoggerFactory.getLogger(RestoreService.class);

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

  @Autowired
  private OrganizationBusinessServiceIF     orgService;

  @Autowired
  private CacheProviderIF                   cacheProvider;

  @FunctionalInterface
  private interface RestoreStep
  {
    void run() throws IOException;
  }

  @Override
  public RestoreSummary restoreFromBackup(InputStream istream)
  {
    RestoreSummary summary = new RestoreSummary();

    try
    {
      File directory = Files.createTempDirectory("gpr-restore").toFile();

      try
      {
        runStep("extracting the backup file", () -> unzipFileToFolder(istream, directory));

        // 0) Restore the organizations, types are only restored for existing
        // organizations
        runStep("restoring organizations", () -> restoreOrganizations(directory, summary));

        // 1) Restore the metadata
        runStep("restoring metadata", () -> restoreMetadata(directory, summary));

        // 2) Refresh permission
        if (Session.getCurrentSession() != null)
        {
          ( (Session) Session.getCurrentSession() ).reloadPermissions();
        }

        // 3) Restore the users and their roles
        runStep("restoring users", () -> restoreUserData(directory, summary));

        // 4) Import the geo object instance data
        runStep("restoring geo objects", () -> restoreGeoObjectData(directory, summary));

        // 5) Import the hierarchy edges between geo objects
        runStep("restoring hierarchy relationships", () -> restoreHierarchyData(directory, summary));

        // 6) Import DAG edges between geo objects
        runStep("restoring directed acyclic graph relationships", () -> restoreDagData(directory, summary));

        // 7) Import Undirected Graph edges between geo objects
        runStep("restoring undirected graph relationships", () -> restoreUndirectedGraphData(directory, summary));

        // 8) Restore the business objects
        runStep("restoring business objects", () -> restoreBusinessObjectData(directory, summary));

        // 9) Restore the business edges
        runStep("restoring business relationships", () -> restoreBusinessEdges(directory, summary));

        // 10) Restore transition events
        runStep("restoring transition events", () -> restoreTransitionEvents(directory, summary));

        // 11) Restore List Type definition
        runStep("restoring list types", () -> restoreListTypeData(directory, summary));
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

    return summary;
  }

  /**
   * Runs a step of the restore. Only an SRA can run a restore, so errors which
   * don't already have a user friendly message are re-thrown with the step and
   * the root cause, instead of surfacing as an "unspecified error" in the UI.
   * The full stack trace is logged.
   */
  private void runStep(String step, RestoreStep runnable)
  {
    try
    {
      runnable.run();
    }
    catch (SmartException e)
    {
      // Already has a localized message
      throw e;
    }
    catch (IOException | RuntimeException e)
    {
      logger.error("Restore failed while " + step, e);

      Throwable root = e;

      while (root.getCause() != null && root.getCause() != root)
      {
        root = root.getCause();
      }

      String message = root.getMessage() != null ? root.getMessage() : "";

      throw new GenericException("The restore failed while " + step + ": " + root.getClass().getSimpleName() + ": " + message + " (see the server log for the full stack trace)", e);
    }
  }

  @Transaction
  protected void restoreMetadata(File directory, RestoreSummary summary) throws IOException
  {
    File subdir = new File(directory, "metadata");

    File[] files = subdir.listFiles();

    if (files != null)
    {
      List<ServerOrganization> organizations = ServerOrganization.getOrganizations();

      for (File file : files)
      {
        try (FileResource resource = new FileResource(file))
        {
          try (InputStream istream = resource.openNewStream())
          {
            List<ServerElement> elements = new XMLImporter(false).importXMLDefinitions(new StreamResource(istream, "domain.xml"), organizations);

            elements.forEach(element -> summary.increment(getMetadataLabel(element)));

            this.repoService.refreshMetadataCache();

            SerializedListTypeCache.getInstance().clear();
          }
        }
      }
    }
  }

  private String getMetadataLabel(ServerElement element)
  {
    if (element instanceof ServerGeoObjectType)
    {
      return "Geo object types";
    }
    else if (element instanceof ServerHierarchyType)
    {
      return "Hierarchies";
    }
    else if (element instanceof BusinessType)
    {
      return "Business types";
    }
    else if (element instanceof BusinessEdgeType)
    {
      return "Business edge types";
    }
    else if (element instanceof DirectedAcyclicGraphType)
    {
      return "Directed acyclic graph types";
    }
    else if (element instanceof UndirectedGraphType)
    {
      return "Undirected graph types";
    }

    return element.getClass().getSimpleName();
  }

  protected void restoreOrganizations(File directory, RestoreSummary summary) throws IOException
  {
    File file = new File(new File(directory, "organizations"), "organizations.json");

    if (file.exists())
    {
      try (FileReader reader = new FileReader(file))
      {
        JsonArray organizations = JsonParser.parseReader(reader).getAsJsonArray();

        this.orgService.importJsonTree(organizations);

        summary.add("Organizations", countOrganizations(organizations));
      }
    }
    else
    {
      // Backups made before organizations were included only have the
      // organization codes in the metadata. Create any missing organizations so
      // that their types and data can be restored.
      summary.add("Organizations", createMissingOrganizationsFromMetadata(new File(new File(directory, "metadata"), "metadata.xml")));
    }

    this.cacheProvider.getServerCache().refresh();
  }

  private long countOrganizations(JsonArray organizations)
  {
    long count = 0;

    for (int i = 0; i < organizations.size(); i++)
    {
      JsonObject organization = organizations.get(i).getAsJsonObject();

      count += 1 + countOrganizations(organization.get("children").getAsJsonArray());
    }

    return count;
  }

  /**
   * @return the number of organizations created
   */
  @Transaction
  protected long createMissingOrganizationsFromMetadata(File metadata) throws IOException
  {
    long count = 0;

    if (!metadata.exists())
    {
      return count;
    }

    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);

      Document document = factory.newDocumentBuilder().parse(metadata);
      NodeList nodes = document.getElementsByTagName("organization");

      for (int i = 0; i < nodes.getLength(); i++)
      {
        String code = ( (Element) nodes.item(i) ).getAttribute("code");

        if (code.length() > 0 && ServerOrganization.getByCode(code, false) == null)
        {
          logger.warn("Creating organization [" + code + "] from the backup metadata. Its label will be the same as its code.");

          OrganizationDTO dto = new OrganizationDTO(code, new LocalizedValue(code), new LocalizedValue(""));
          dto.setEnabled(true);

          this.orgService.create(dto);

          count++;
        }
      }
    }
    catch (ParserConfigurationException | SAXException e)
    {
      throw new ProgrammingErrorException(e);
    }

    return count;
  }

  protected void restoreUserData(File directory, RestoreSummary summary) throws IOException
  {
    File file = new File(new File(directory, "users"), "users.json");

    if (file.exists())
    {
      // Don't overwrite the built-in admin or the user running the restore
      String currentUsername = Session.getCurrentSession() != null ? Session.getCurrentSession().getUser().getSingleActorName() : null;

      try (JsonReader reader = new JsonReader(new FileReader(file)))
      {
        reader.setLenient(true);

        Gson gson = new GsonBuilder().create();

        reader.beginArray();

        while (reader.hasNext())
        {
          JsonObject jObject = gson.fromJson(reader, JsonObject.class);

          String username = jObject.get(GeoprismUser.USERNAME).getAsString();

          if (!username.equals(RegistryConstants.ADMIN_USER_NAME) && !username.equals(currentUsername))
          {
            UserInfo.restoreFromBackup(jObject);

            summary.increment("Users");
          }
          else
          {
            summary.increment("Users skipped (admin or the current user)");
          }
        }

        reader.endArray();
      }
    }
  }

  protected void restoreGeoObjectData(File directory, RestoreSummary summary) throws IOException
  {
    RegistryAdapter adapter = ServiceFactory.getAdapter();
    File data = new File(directory, "geo-objects");

    File[] files = data.listFiles();

    if (files != null)
    {

      for (File file : files)
      {
        String typeCode = FilenameUtils.getBaseName(file.getName());

        if (!adapter.getMetadataCache().getGeoObjectType(typeCode).isPresent())
        {
          throw new GenericException("The geo object type [" + typeCode + "] in the backup does not exist on this server. Check that its organization exists and that the metadata was restored.");
        }

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

              // Backups made before the "present" date was changed to
              // 4710-12-31 contain end dates of 5000-12-31
              GeoRegistryUtil.clampEndDates(jObject);

              GeoObjectOverTime object = GeoObjectOverTime.fromJSON(adapter, jObject.toString());

              this.gObjectService.apply(object, true, true, false);

              summary.increment("Geo objects");
            }

            reader.endArray();
          }
        }
      }
    }
  }

  protected void restoreHierarchyData(File directory, RestoreSummary summary) throws IOException
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
              Date endDate = GeoRegistryUtil.clampEndDate(GeoRegistryUtil.parseDate(jObject.get("endDate").getAsString()));
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

              this.gObjectService.addChild(parent, child, hierarchy, startDate, endDate, uid, null, false);

              summary.increment("Hierarchy relationships");
            }

            reader.endArray();
          }
        }
      }
    }
  }

  protected void restoreDagData(File directory, RestoreSummary summary) throws IOException
  {
    restoreGraphEdges(new File(directory, "dags"), "Directed acyclic graph relationships", summary);
  }

  protected void restoreUndirectedGraphData(File directory, RestoreSummary summary) throws IOException
  {
    restoreGraphEdges(new File(directory, "undirected-graphs"), "Undirected graph relationships", summary);
  }

  protected void restoreGraphEdges(File data, String label, RestoreSummary summary) throws IOException
  {
    File[] files = data.listFiles();

    if (files != null)
    {

      for (File file : files)
      {
        try (FileResource resource = new FileResource(file))
        {
          EdgeJsonImporter importer = new EdgeJsonImporter(resource, null, null, null, false);
          importer.importData();

          summary.add(label, importer.getCount());
        }
      }
    }
  }

  protected void restoreBusinessObjectData(File directory, RestoreSummary summary) throws IOException
  {
    File subdir = new File(directory, "business-objects");

    File[] files = subdir.listFiles();

    if (files != null)
    {

      for (File file : files)
      {
        String name = file.getName();
        String baseName = FilenameUtils.getBaseName(name);

        BusinessType type = this.bTypeService.getByCodeOrThrow(baseName);

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

              summary.increment("Business objects");
            }

            reader.endArray();
          }
        }
      }
    }
  }

  protected void restoreBusinessEdges(File directory, RestoreSummary summary) throws IOException
  {
    File subdir = new File(directory, "business-edges");
    File[] files = subdir.listFiles();

    if (files != null)
    {
      for (File file : files)
      {
        try (FileResource resource = new FileResource(file))
        {
          BusinessEdgeJsonImporter importer = new BusinessEdgeJsonImporter(resource, null, false);
          importer.importData();

          summary.add("Business relationships", importer.getCount());
        }
      }
    }
  }

  protected void restoreTransitionEvents(File directory, RestoreSummary summary) throws IOException
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

            summary.increment("Transition events");
          }

          reader.endArray();
        }
      }
    }
  }

  private void restoreListTypeData(File directory, RestoreSummary summary) throws IOException
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

            summary.increment("List types");
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
