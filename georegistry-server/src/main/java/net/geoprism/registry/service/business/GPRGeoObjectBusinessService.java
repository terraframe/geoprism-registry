package net.geoprism.registry.service.business;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.dataaccess.AlternateId;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.session.CreatePermissionException;
import com.runwaysdk.session.ReadPermissionException;
import com.runwaysdk.session.WritePermissionException;

import net.geoprism.registry.etl.export.GeoObjectExportFormat;
import net.geoprism.registry.etl.export.GeoObjectJsonExporter;
import net.geoprism.registry.etl.export.RevealGeoObjectJsonAdapters;
import net.geoprism.registry.etl.upload.ClassifierCache;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.graph.ExternalId;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.roles.CreateGeoObjectPermissionException;
import net.geoprism.registry.roles.ReadGeoObjectPermissionException;
import net.geoprism.registry.roles.WriteGeoObjectPermissionException;

@Service
@Primary
public class GPRGeoObjectBusinessService extends GeoObjectBusinessService implements GPRGeoObjectBusinessServiceIF
{
  @Autowired
  private SearchService searchService;

  @Override
  public JsonObject getAll(String gotCode, String hierarchyCode, Date since, Boolean includeLevel, String format, String externalSystemId, Integer pageNumber, Integer pageSize)
  {
    GeoObjectExportFormat goef = null;
    if (format != null && format.length() > 0)
    {
      goef = GeoObjectExportFormat.valueOf(format);
    }

    Map<Type, Object> typeAdapters = null;

    if (format.equals(GeoObjectExportFormat.JSON_REVEAL.name()) && externalSystemId != null)
    {
      ExternalSystem es = ExternalSystem.getByExternalSystemId(externalSystemId);
      ServerHierarchyType ht = ServerHierarchyType.get(hierarchyCode);

      typeAdapters = new HashMap<Type, Object>();
      typeAdapters.put(VertexServerGeoObject.class, new RevealGeoObjectJsonAdapters.RevealSerializer(ServerGeoObjectType.get(gotCode), ht, includeLevel, es));
    }

    GeoObjectJsonExporter exporter = new GeoObjectJsonExporter(gotCode, hierarchyCode, since, includeLevel, goef, typeAdapters, pageSize, pageNumber);

    try
    {
      return exporter.export();
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void apply(ServerGeoObjectIF sgo, boolean isImport)
  {
    final boolean isNew = sgo.getVertex().isNew() || sgo.getVertex().getObjectValue(GeoVertex.CREATEDATE) == null;

    try
    {
      super.apply(sgo, isImport);
    }
    catch (CreatePermissionException ex)
    {
      CreateGeoObjectPermissionException goex = new CreateGeoObjectPermissionException();
      goex.setGeoObjectType(sgo.getType().getLabel().getValue());
      goex.setOrganization(sgo.getType().getOrganization().getDisplayLabel().getValue());
      throw goex;
    }
    catch (WritePermissionException ex)
    {
      WriteGeoObjectPermissionException goex = new WriteGeoObjectPermissionException();
      goex.setGeoObjectType(sgo.getType().getLabel().getValue());
      goex.setOrganization(sgo.getType().getOrganization().getDisplayLabel().getValue());
      throw goex;
    }
    catch (ReadPermissionException ex)
    {
      ReadGeoObjectPermissionException goex = new ReadGeoObjectPermissionException();
      goex.setGeoObjectType(sgo.getType().getLabel().getValue());
      goex.setOrganization(sgo.getType().getOrganization().getDisplayLabel().getValue());
      throw goex;
    }

    if (!sgo.getInvalid())
    {
      this.searchService.insert((VertexServerGeoObject) sgo, isNew);
    }
    else if (!isNew)
    {
      this.searchService.remove(sgo.getCode());
    }
  }

  public void setAlternateIds(ServerGeoObjectIF sgo, List<AlternateId> alternateIds)
  {
    if (alternateIds == null)
    {
      alternateIds = new ArrayList<>();
    }

    final List<ExternalId> olds = getAllExternalIds(sgo);
    final Map<String, ExternalSystem> esMap = ExternalSystem.getAll().stream().collect(Collectors.toMap(es -> es.getOid(), es -> es));
    final Set<Integer> newMatched = new HashSet<Integer>();

    for (ExternalId oldId : olds)
    {
      boolean matched = false;

      for (int i = 0; i < alternateIds.size(); ++i)
      {
        org.commongeoregistry.adapter.dataaccess.ExternalId newId = (org.commongeoregistry.adapter.dataaccess.ExternalId) alternateIds.get(i);

        if (!newMatched.contains(i) && oldId.getExternalId().equals(newId.getId()))
        {
          oldId.setExternalId(newId.getId());
          oldId.apply();

          matched = true;
          newMatched.add(i);
          break;
        }
      }

      if (!matched)
      {
        oldId.delete();
      }
    }
    for (int i = 0; i < alternateIds.size(); ++i)
    {
      org.commongeoregistry.adapter.dataaccess.ExternalId newId = (org.commongeoregistry.adapter.dataaccess.ExternalId) alternateIds.get(i);

      if (!newMatched.contains(i))
      {
        createExternalId(sgo, esMap.get(newId.getExternalSystemId()), newId.getId(), ImportStrategy.NEW_ONLY);
      }
    }
  }

  @Override
  public VertexServerGeoObject getByExternalId(String externalId, ExternalSystem system, ServerGeoObjectType type)
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(GeoVertex.EXTERNAL_ID);

    StringBuilder statement = new StringBuilder();

    if (type != null)
    {
      statement.append("SELECT FROM (");
    }

    statement.append("SELECT expand(in) FROM (");
    statement.append("SELECT expand(outE('" + mdEdge.getDBClassName() + "')[id = '" + externalId + "']) FROM :system)");

    if (type != null)
    {
      statement.append(") WHERE @class='" + type.getMdVertex().getDBClassName() + "'");
    }

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());
    query.setParameter("system", system.getRID());

    VertexObject vo = query.getSingleResult();

    if (vo != null)
    {
      if (type == null)
      {
        type = ServerGeoObjectType.get((MdVertexDAOIF) vo.getMdClass());
      }

      return new VertexServerGeoObject(type, vo, new TreeMap<>());
    }
    else
    {
      return null;
    }
  }

  private ExternalId getExternalIdEdge(ServerGeoObjectIF sgo, ExternalSystem system)
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(GeoVertex.EXTERNAL_ID);

    String statement = "SELECT expand(inE('" + mdEdge.getDBClassName() + "')[out = :parent])";
    statement += " FROM :child";

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement);
    query.setParameter("parent", system.getRID());
    query.setParameter("child", sgo.getVertex().getRID());

    EdgeObject edge = query.getSingleResult();

    if (edge == null)
    {
      return null;
    }
    else
    {
      return new ExternalId(edge);
    }
  }

  public List<ExternalId> getAllExternalIds(ServerGeoObjectIF sgo)
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(GeoVertex.EXTERNAL_ID);

    String statement = "SELECT expand(inE('" + mdEdge.getDBClassName() + "'))";
    statement += " FROM :child";

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement);
    query.setParameter("child", sgo.getVertex().getRID());

    return query.getResults().stream().map(edge -> new ExternalId(edge)).collect(Collectors.toList());
  }

  @Override
  public void createExternalId(ServerGeoObjectIF sgo, ExternalSystem system, String id, ImportStrategy importStrategy)
  {
    if (importStrategy.equals(ImportStrategy.NEW_ONLY))
    {
      ExternalId externalId = new ExternalId(sgo.getVertex().addParent(system, GeoVertex.EXTERNAL_ID));
      externalId.setExternalId(id);
      externalId.apply();
    }
    else
    {
      ExternalId externalId = getExternalIdEdge(sgo, system);

      if (externalId == null)
      {
        externalId = new ExternalId(sgo.getVertex().addParent(system, GeoVertex.EXTERNAL_ID));
      }

      externalId.setExternalId(id);
      externalId.apply();
    }
  }

  @Override
  public String getExternalId(ServerGeoObjectIF sgo, ExternalSystem system)
  {
    ExternalId edge = getExternalIdEdge(sgo, system);

    if (edge != null)
    {
      return edge.getExternalId();
    }

    return null;
  }

  @Override
  public GeoObjectOverTime toGeoObjectOverTime(ServerGeoObjectIF sgo, boolean generateUid, ClassifierCache classifierCache)
  {
    GeoObjectOverTime geoObj = super.toGeoObjectOverTime(sgo, generateUid, classifierCache);

    for (ExternalId id : getAllExternalIds(sgo))
    {
      geoObj.addAlternateId(id.toDTO());
    }

    return geoObj;
  }

  @Override
  public GeoObject toGeoObject(ServerGeoObjectIF sgo, Date date, boolean includeExternalIds)
  {
    GeoObject geoObj = super.toGeoObject(sgo, date, includeExternalIds);

    if (includeExternalIds)
    {
      for (ExternalId id : getAllExternalIds(sgo))
      {
        geoObj.addAlternateId(id.toDTO());
      }
    }

    return geoObj;
  }
}
