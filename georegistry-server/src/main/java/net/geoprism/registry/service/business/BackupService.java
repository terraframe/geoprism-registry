package net.geoprism.registry.service.business;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.cache.ObjectCache;
import com.runwaysdk.system.metadata.MdEdge;

import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.graph.transition.Transition;
import net.geoprism.registry.graph.transition.TransitionEvent;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.query.graph.BasicVertexQuery;
import net.geoprism.registry.service.request.ServiceFactory;
import net.geoprism.registry.view.Page;
import net.geoprism.registry.xml.XMLExporter;

@Service
public class BackupService implements BackupServiceIF
{
  @Autowired
  private BusinessTypeBusinessServiceIF     bTypeService;

  @Autowired
  private BusinessEdgeTypeBusinessServiceIF bEdgeTypeService;

  @Autowired
  private GPRBusinessObjectBusinessService  bService;

  @Autowired
  private GeoObjectBusinessServiceIF        gService;

  @Autowired
  private GPRTransitionEventBusinessService teService;

  @Autowired
  private GPRTransitionBusinessService      tService;

  @Override
  public void createBackup(File zipfile)
  {
    try
    {
      File directory = Files.createTempDirectory("gpr-dump").toFile();

      try
      {
        exportMetadata(directory);

        exportGeoObjectData(directory);

        exportHierarchyData(directory);

        exportDagData(directory);

        exportUndirectedGraphData(directory);

        exportBusinessObjectData(directory);

        exportBusinessEdgeData(directory);

        exportHistoricalEventData(directory);

        GeoRegistryUtil.zipDirectory(directory, zipfile);
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

  private void exportHistoricalEventData(File directory) throws IOException
  {
    File subdirectory = new File(directory, "events");
    subdirectory.mkdirs();

    exportTransitionEvents(subdirectory);

    exportTransitions(subdirectory);
  }

  protected void exportTransitions(File directory) throws IOException
  {
    try (JsonWriter writer = new JsonWriter(new FileWriter(new File(directory, "transitions.json"))))
    {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();

      writer.beginArray();

      Long count = this.tService.getCount();

      if (count == null)
      {
        count = 0L;
      }

      int pageSize = 1000;

      long skip = 0;

      while (skip < count)
      {
        List<Transition> results = this.tService.getAll(pageSize, skip);

        for (Transition result : results)
        {
          JsonObject json = result.toJSON();
          json.addProperty("event", (String) result.getObjectValue(Transition.EVENT));

          gson.toJson(json, writer);
        }

        skip += pageSize;
      }

      writer.endArray();
    }
  }

  protected void exportTransitionEvents(File directory) throws IOException
  {
    Page<TransitionEvent> page = this.teService.getAll(1000, 1);

    try (JsonWriter writer = new JsonWriter(new FileWriter(new File(directory, "transition-events.json"))))
    {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();

      writer.beginArray();

      while (page.getResults().size() > 0)
      {
        List<TransitionEvent> results = page.getResults();

        for (TransitionEvent result : results)
        {
          gson.toJson(result.toJSON(), writer);
        }

        page = this.teService.getAll(page.getPageSize(), page.getPageNumber() + 1);
      }

      writer.endArray();
    }
  }

  protected void exportMetadata(File directory)
  {
    File metadata = new File(directory, "metadata");
    metadata.mkdirs();

    List<ServerOrganization> organizations = ServerOrganization.getOrganizations();

    int i = 0;

    // Export the metadata definitions for each organization
    for (ServerOrganization organization : organizations)
    {
      XMLExporter exporter = new XMLExporter(organization, ( i == 0 ));
      exporter.build();

      exporter.write(new File(metadata, organization.getCode() + ".xml"));

      i++;
    }
  }

  protected void exportGeoObjectData(File directory) throws IOException
  {
    File data = new File(directory, "geo-objects");
    data.mkdirs();

    List<ServerGeoObjectType> types = ServiceFactory.getMetadataCache().getAllGeoObjectTypes();

    for (ServerGeoObjectType type : types)
    {
      if (!type.getIsAbstract())
      {
        exportGeoObjectData(data, type);
      }
    }
  }

  protected void exportGeoObjectData(File directory, ServerGeoObjectType type) throws IOException
  {
    try (JsonWriter writer = new JsonWriter(new FileWriter(new File(directory, type.getCode() + ".json"))))
    {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();

      writer.beginArray();

      BasicVertexQuery query = new BasicVertexQuery(type, null);
      Long count = query.getCount();

      if (count == null)
      {
        count = 0L;
      }

      int pageSize = 1000;

      long skip = 0;

      while (skip < count)
      {
        query = new BasicVertexQuery(type, null);
        query.setLimit(pageSize);
        query.setSkip(skip);

        List<ServerGeoObjectIF> results = query.getResults();

        for (ServerGeoObjectIF result : results)
        {
          GeoObjectOverTime object = this.gService.toGeoObjectOverTime(result);

          gson.toJson(object.toJSON(), writer);
        }

        skip += pageSize;
      }

      writer.endArray();
    }
  }

  protected void exportHierarchyData(File directory) throws IOException
  {
    File data = new File(directory, "hierarchies");
    data.mkdirs();

    List<ServerHierarchyType> types = ServiceFactory.getMetadataCache().getAllHierarchyTypes();

    for (ServerHierarchyType type : types)
    {
      exportHierarchyData(data, type);
    }
  }

  protected void exportHierarchyData(File directory, ServerHierarchyType type) throws IOException
  {
    try (JsonWriter writer = new JsonWriter(new FileWriter(new File(directory, type.getCode() + ".json"))))
    {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();

      writer.beginArray();

      Long count = new GraphQuery<Long>("SELECT COUNT(*) FROM " + type.getMdEdge().getDBClassName()).getSingleResult();

      if (count == null)
      {
        count = 0L;
      }

      int pageSize = 1000;

      long skip = 0;

      while (skip < count)
      {
        StringBuilder statement = new StringBuilder();
        statement.append("SELECT out.uuid AS parentUid, out.@class AS parentClass, in.uuid AS childUid, in.@class AS childClass");
        statement.append(" FROM " + type.getMdEdge().getDBClassName());
        statement.append(" SKIP " + skip);
        statement.append(" LIMIT " + pageSize);

        GraphQuery<Map<String, Object>> query = new GraphQuery<Map<String, Object>>(statement.toString());
        List<Map<String, Object>> results = query.getResults();

        for (Map<String, Object> result : results)
        {
          String parentUid = (String) result.get("parentUid");
          String parentClass = (String) result.get("parentClass");
          String childUid = (String) result.get("childUid");
          String childClass = (String) result.get("childClass");

          MdVertexDAOIF parentVertex = (MdVertexDAOIF) ObjectCache.getMdClassByTableName(parentClass);
          MdVertexDAOIF childVertex = (MdVertexDAOIF) ObjectCache.getMdClassByTableName(childClass);

          ServerGeoObjectType parentType = ServerGeoObjectType.get(parentVertex);
          ServerGeoObjectType childType = ServerGeoObjectType.get(childVertex);

          JsonObject object = new JsonObject();
          object.addProperty("parentUid", parentUid);
          object.addProperty("parentType", parentType.getCode());
          object.addProperty("childUid", childUid);
          object.addProperty("childType", childType.getCode());

          gson.toJson(object, writer);
        }

        skip += pageSize;
      }

      writer.endArray();
    }
  }

  protected void exportDagData(File directory) throws IOException
  {
    File data = new File(directory, "dags");
    data.mkdirs();

    List<DirectedAcyclicGraphType> types = DirectedAcyclicGraphType.getAll();

    for (DirectedAcyclicGraphType type : types)
    {
      exportGraphEdges(data, type.getCode(), DirectedAcyclicGraphType.CLASS, type.getMdEdge());
    }
  }

  protected void exportUndirectedGraphData(File directory) throws IOException
  {
    File data = new File(directory, "undirected-graphs");
    data.mkdirs();

    List<UndirectedGraphType> types = UndirectedGraphType.getAll();

    for (UndirectedGraphType type : types)
    {
      exportGraphEdges(data, type.getCode(), UndirectedGraphType.CLASS, type.getMdEdge());
    }
  }

  protected void exportGraphEdges(File directory, String code, String graphTypeClass, MdEdge mdEdge) throws IOException
  {
    Set<String> keys = new TreeSet<String>();

    try (JsonWriter writer = new JsonWriter(new FileWriter(new File(directory, code + ".json"))))
    {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();

      writer.beginObject();
      writer.name("graphTypes");

      writer.beginArray();

      writer.beginObject();
      writer.name("graphTypeClass");
      writer.value(graphTypeClass);
      writer.name("code");
      writer.value(code);

      writer.name("edges");
      writer.beginArray();

      Long count = new GraphQuery<Long>("SELECT COUNT(*) FROM " + mdEdge.getDbClassName()).getSingleResult();

      if (count == null)
      {
        count = 0L;
      }

      int pageSize = 1000;

      long skip = 0;

      while (skip < count)
      {
        StringBuilder statement = new StringBuilder();
        statement.append("SELECT out.code AS sourceCode, out.@class AS sourceClass, in.code AS targetCode, in.@class AS targetClass");
        statement.append(" FROM " + mdEdge.getDbClassName());
        statement.append(" SKIP " + skip);
        statement.append(" LIMIT " + pageSize);

        GraphQuery<Map<String, Object>> query = new GraphQuery<Map<String, Object>>(statement.toString());
        List<Map<String, Object>> results = query.getResults();

        for (Map<String, Object> result : results)
        {
          String sourceCode = (String) result.get("sourceCode");
          String sourceClass = (String) result.get("sourceClass");
          String targetCode = (String) result.get("targetCode");
          String targetClass = (String) result.get("targetClass");

          MdVertexDAOIF sourceVertex = (MdVertexDAOIF) ObjectCache.getMdClassByTableName(sourceClass);
          MdVertexDAOIF targetVertex = (MdVertexDAOIF) ObjectCache.getMdClassByTableName(targetClass);

          ServerGeoObjectType sourceType = ServerGeoObjectType.get(sourceVertex);
          ServerGeoObjectType targetType = ServerGeoObjectType.get(targetVertex);

          String key = sourceCode.compareTo(targetCode) > 0 ? sourceCode + "-" + targetCode : targetCode + "-" + sourceCode;

          if (!keys.contains(key))
          {
            JsonObject object = new JsonObject();
            object.addProperty("source", sourceCode);
            object.addProperty("sourceType", sourceType.getCode());
            object.addProperty("target", targetCode);
            object.addProperty("targetType", targetType.getCode());

            gson.toJson(object, writer);

            keys.add(key);
          }
        }

        skip += pageSize;
      }

      // End edges array
      writer.endArray();

      // End Graph Type Object
      writer.endObject();

      // End Graph Type array
      writer.endArray();

      // End Main object
      writer.endObject();
    }
  }

  protected void exportBusinessObjectData(File directory) throws IOException
  {
    File data = new File(directory, "business-objects");
    data.mkdirs();

    List<BusinessType> types = this.bTypeService.getAll();

    for (BusinessType type : types)
    {
      exportBusinessObjectData(data, type);
    }
  }

  protected void exportBusinessObjectData(File directory, BusinessType type) throws IOException
  {
    try (JsonWriter writer = new JsonWriter(new FileWriter(new File(directory, type.getCode() + ".json"))))
    {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();

      writer.beginArray();

      Long count = this.bService.getCount(type);

      if (count == null)
      {
        count = 0L;
      }

      int pageSize = 1000;

      long skip = 0;

      while (skip < count)
      {
        List<BusinessObject> results = this.bService.getAll(type, skip, pageSize);

        for (BusinessObject result : results)
        {
          JsonObject json = this.bService.toJSON(result);

          gson.toJson(json, writer);
        }

        skip += pageSize;
      }

      writer.endArray();
    }
  }

  protected void exportBusinessEdgeData(File directory) throws IOException
  {
    File data = new File(directory, "business-edges");
    data.mkdirs();

    List<BusinessEdgeType> types = this.bEdgeTypeService.getAll();

    for (BusinessEdgeType type : types)
    {
      exportBusinessEdgeData(data, type);
    }
  }

  protected void exportBusinessEdgeData(File directory, BusinessEdgeType type) throws IOException
  {
    try (JsonWriter writer = new JsonWriter(new FileWriter(new File(directory, type.getCode() + ".json"))))
    {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();

      writer.beginObject();
      writer.name("edgeTypes");

      writer.beginArray();

      writer.beginObject();
      writer.name("code");
      writer.value(type.getCode());

      writer.name("edges");
      writer.beginArray();

      MdEdge mdEdge = type.getMdEdge();
      Long count = new GraphQuery<Long>("SELECT COUNT(*) FROM " + mdEdge.getDbClassName()).getSingleResult();

      if (count == null)
      {
        count = 0L;
      }

      int pageSize = 1000;

      long skip = 0;

      while (skip < count)
      {
        StringBuilder statement = new StringBuilder();
        statement.append("SELECT out.code AS sourceCode, in.code AS targetCode");
        statement.append(" FROM " + mdEdge.getDbClassName());
        statement.append(" SKIP " + skip);
        statement.append(" LIMIT " + pageSize);

        GraphQuery<Map<String, Object>> query = new GraphQuery<Map<String, Object>>(statement.toString());
        List<Map<String, Object>> results = query.getResults();

        for (Map<String, Object> result : results)
        {
          String sourceCode = (String) result.get("sourceCode");
          String targetCode = (String) result.get("targetCode");

          JsonObject object = new JsonObject();
          object.addProperty("source", sourceCode);
          object.addProperty("target", targetCode);

          gson.toJson(object, writer);
        }

        skip += pageSize;
      }

      // End edges array
      writer.endArray();

      // End Edge Type Object
      writer.endObject();

      // End Edge Type array
      writer.endArray();

      // End Main object
      writer.endObject();
    }
  }

}
