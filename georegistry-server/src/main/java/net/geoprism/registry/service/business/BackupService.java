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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.cache.ObjectCache;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.system.metadata.MdEdge;

import net.geoprism.registry.BusinessEdgeType;
import net.geoprism.registry.BusinessType;
import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.ListType;
import net.geoprism.registry.ListTypeQuery;
import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.graph.transition.Transition;
import net.geoprism.registry.graph.transition.TransitionEvent;
import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.query.graph.BasicVertexQuery;
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

        exportBusinessGeoObjectEdgeData(directory);

        exportBusinessEdgeData(directory);

        exportHistoricalEventData(directory);

        exportListTypeData(directory);

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

  private void exportListTypeData(File directory) throws IOException
  {
    File subdirectory = new File(directory, "list-type");
    subdirectory.mkdirs();

    ListTypeQuery query = new ListTypeQuery(new QueryFactory());

    try (OIterator<? extends ListType> it = query.getIterator())
    {
      List<? extends ListType> listTypes = it.getAll();

      for (ListType listType : listTypes)
      {
        try (JsonWriter writer = new JsonWriter(new FileWriter(new File(subdirectory, listType.getCode() + ".json"))))
        {
          Gson gson = new GsonBuilder().setPrettyPrinting().create();

          gson.toJson(listType.toJSON(false), writer);
        }
      }
    }
  }

  private void exportHistoricalEventData(File directory) throws IOException
  {
    File subdirectory = new File(directory, "events");
    subdirectory.mkdirs();

    exportTransitionEvents(subdirectory);
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
          JsonObject event = this.teService.toJSON(result, true);
          JsonArray transitions = event.get("transitions").getAsJsonArray();

          for (int i = 0; i < transitions.size(); i++)
          {
            JsonObject object = transitions.get(i).getAsJsonObject();
            object.remove(Transition.OID);
            object.addProperty("isNew", true);
          }

          gson.toJson(event, writer);
        }

        page = this.teService.getAll(page.getPageSize(), page.getPageNumber() + 1);
      }

      writer.endArray();
    }
  }

  protected void exportMetadata(File directory)
  {
    File subdir = new File(directory, "metadata");
    subdir.mkdirs();

    List<ServerOrganization> organizations = ServerOrganization.getOrganizations();

    XMLExporter exporter = new XMLExporter(organizations);
    exporter.build();
    exporter.write(new File(subdir, "metadata.xml"));
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

      Long count = new GraphQuery<Long>("SELECT COUNT(*) FROM " + type.getMdEdgeDAO().getDBClassName()).getSingleResult();

      if (count == null)
      {
        count = 0L;
      }

      int pageSize = 1000;

      long skip = 0;

      while (skip < count)
      {
        StringBuilder statement = new StringBuilder();
        statement.append("SELECT out.uid AS parentUid, out.@class AS parentClass, in.uid AS childUid, in.@class AS childClass, startDate AS startDate, endDate AS endDate");
        statement.append(" FROM " + type.getMdEdgeDAO().getDBClassName());
        statement.append(" ORDER BY out.@rid, in.@rid");
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
          Date startDate = (Date) result.get("startDate");
          Date endDate = (Date) result.get("endDate");

          MdVertexDAOIF parentVertex = (MdVertexDAOIF) ObjectCache.getMdClassByTableName(parentClass);
          MdVertexDAOIF childVertex = (MdVertexDAOIF) ObjectCache.getMdClassByTableName(childClass);

          ServerGeoObjectType parentType = ServerGeoObjectType.get(parentVertex);
          ServerGeoObjectType childType = ServerGeoObjectType.get(childVertex);

          JsonObject object = new JsonObject();
          object.addProperty("parentUid", parentUid);
          object.addProperty("parentType", parentType.getCode());
          object.addProperty("childUid", childUid);
          object.addProperty("childType", childType.getCode());
          object.addProperty("startDate", GeoRegistryUtil.formatDate(startDate, false));
          object.addProperty("endDate", GeoRegistryUtil.formatDate(endDate, false));

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
        statement.append("SELECT out.code AS sourceCode, out.@class AS sourceClass, in.code AS targetCode, in.@class AS targetClass, startDate AS startDate, endDate AS endDate");
        statement.append(" FROM " + mdEdge.getDbClassName());
        statement.append(" ORDER BY out.@rid, in.@rid");
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
          Date startDate = (Date) result.get("startDate");
          Date endDate = (Date) result.get("endDate");

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
            object.addProperty("startDate", GeoRegistryUtil.formatDate(startDate, false));
            object.addProperty("endDate", GeoRegistryUtil.formatDate(endDate, false));

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

  protected void exportBusinessGeoObjectEdgeData(File directory) throws IOException
  {
    File data = new File(directory, "business-geoobject-edges");
    data.mkdirs();

    List<BusinessType> types = this.bTypeService.getAll();

    for (BusinessType type : types)
    {
      exportBusinessGeoObjectEdgeData(data, type);
    }
  }

  private void exportBusinessGeoObjectEdgeData(File directory, BusinessType type) throws IOException
  {
    try (JsonWriter writer = new JsonWriter(new FileWriter(new File(directory, type.getCode() + ".json"))))
    {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
        statement.append("SELECT out.code AS geoObjectCode, out.@class AS geoObjectClass, in.code AS businessCode");
        statement.append(" FROM " + mdEdge.getDbClassName());
        statement.append(" ORDER BY out.code, in.code");
        statement.append(" SKIP " + skip);
        statement.append(" LIMIT " + pageSize);

        GraphQuery<Map<String, Object>> query = new GraphQuery<Map<String, Object>>(statement.toString());
        List<Map<String, Object>> results = query.getResults();

        for (Map<String, Object> result : results)
        {
          String geoObjectCode = (String) result.get("geoObjectCode");
          String geoObjectClass = (String) result.get("geoObjectClass");
          String businessCode = (String) result.get("businessCode");

          MdVertexDAOIF geoObjectVertex = (MdVertexDAOIF) ObjectCache.getMdClassByTableName(geoObjectClass);

          ServerGeoObjectType geoObjectType = ServerGeoObjectType.get(geoObjectVertex);

          JsonObject object = new JsonObject();
          object.addProperty("geoObjectCode", geoObjectCode);
          object.addProperty("geoObjectType", geoObjectType.getCode());
          object.addProperty("businessCode", businessCode);
          object.addProperty("businessType", type.getCode());

          gson.toJson(object, writer);
        }

        skip += pageSize;
      }

      writer.endArray();
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
