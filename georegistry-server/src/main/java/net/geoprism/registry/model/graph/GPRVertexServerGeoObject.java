package net.geoprism.registry.model.graph;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.AlternateId;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeListType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.locationtech.jts.geom.Geometry;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.metadata.graph.MdEdgeDAO;
import com.runwaysdk.session.CreatePermissionException;
import com.runwaysdk.session.ReadPermissionException;
import com.runwaysdk.session.WritePermissionException;

import net.geoprism.registry.GeometrySizeException;
import net.geoprism.registry.GeoregistryProperties;
import net.geoprism.registry.conversion.RegistryLocalizedValueConverter;
import net.geoprism.registry.etl.upload.ClassifierCache;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.DHIS2ExternalSystem;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.roles.CreateGeoObjectPermissionException;
import net.geoprism.registry.roles.ReadGeoObjectPermissionException;
import net.geoprism.registry.roles.WriteGeoObjectPermissionException;
import net.geoprism.registry.service.SearchService;

public class GPRVertexServerGeoObject extends VertexServerGeoObject
{

  public GPRVertexServerGeoObject(ServerGeoObjectType type, VertexObject vertex)
  {
    super(type, vertex);
  }

  public GPRVertexServerGeoObject(ServerGeoObjectType type, VertexObject vertex, Date date)
  {
    super(type, vertex, date);
  }
  
  @Override
  public void apply(boolean isImport)
  {
    final boolean isNew = this.vertex.isNew() || this.vertex.getObjectValue(GeoVertex.CREATEDATE) == null;
    
    try
    {
      super.apply(isImport);
    }
    catch (CreatePermissionException ex)
    {
      CreateGeoObjectPermissionException goex = new CreateGeoObjectPermissionException();
      goex.setGeoObjectType(this.getType().getLabel().getValue());
      goex.setOrganization(this.getType().getOrganization().getDisplayLabel().getValue());
      throw goex;
    }
    catch (WritePermissionException ex)
    {
      WriteGeoObjectPermissionException goex = new WriteGeoObjectPermissionException();
      goex.setGeoObjectType(this.getType().getLabel().getValue());
      goex.setOrganization(this.getType().getOrganization().getDisplayLabel().getValue());
      throw goex;
    }
    catch (ReadPermissionException ex)
    {
      ReadGeoObjectPermissionException goex = new ReadGeoObjectPermissionException();
      goex.setGeoObjectType(this.getType().getLabel().getValue());
      goex.setOrganization(this.getType().getOrganization().getDisplayLabel().getValue());
      throw goex;
    }

    if (!this.getInvalid())
    {
      new SearchService().insert(this, isNew);
    }
    else if (!isNew)
    {
      new SearchService().remove(this.getCode());
    }
  }
  
  public void setAlternateIds(List<AlternateId> alternateIds)
  {
    if (alternateIds == null)
    {
      alternateIds = new ArrayList<>();
    }

    final List<ExternalId> olds = this.getAllExternalIds();
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
        this.createExternalId(esMap.get(newId.getExternalSystemId()), newId.getId(), ImportStrategy.NEW_ONLY);
      }
    }
  }
  
  public static VertexServerGeoObject getByExternalId(String externalId, DHIS2ExternalSystem system, ServerGeoObjectType type)
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

      return new VertexServerGeoObject(type, vo);
    }
    else
    {
      return null;
    }
  }

  private ExternalId getExternalIdEdge(ExternalSystem system)
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(GeoVertex.EXTERNAL_ID);

    String statement = "SELECT expand(inE('" + mdEdge.getDBClassName() + "')[out = :parent])";
    statement += " FROM :child";

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement);
    query.setParameter("parent", system.getRID());
    query.setParameter("child", this.getVertex().getRID());

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

  public List<ExternalId> getAllExternalIds()
  {
    MdEdgeDAOIF mdEdge = MdEdgeDAO.getMdEdgeDAO(GeoVertex.EXTERNAL_ID);

    String statement = "SELECT expand(inE('" + mdEdge.getDBClassName() + "'))";
    statement += " FROM :child";

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement);
    query.setParameter("child", this.getVertex().getRID());

    return query.getResults().stream().map(edge -> new ExternalId(edge)).collect(Collectors.toList());
  }

  public void createExternalId(ExternalSystem system, String id, ImportStrategy importStrategy)
  {
    if (importStrategy.equals(ImportStrategy.NEW_ONLY))
    {
      ExternalId externalId = new ExternalId(this.getVertex().addParent(system, GeoVertex.EXTERNAL_ID));
      externalId.setExternalId(id);
      externalId.apply();
    }
    else
    {
      ExternalId externalId = this.getExternalIdEdge(system);

      if (externalId == null)
      {
        externalId = new ExternalId(this.getVertex().addParent(system, GeoVertex.EXTERNAL_ID));
      }

      externalId.setExternalId(id);
      externalId.apply();
    }
  }

  public String getExternalId(ExternalSystem system)
  {
    ExternalId edge = this.getExternalIdEdge(system);

    if (edge != null)
    {
      return edge.getExternalId();
    }

    return null;
  }
  
  @Override
  public GeoObjectOverTime toGeoObjectOverTime(boolean generateUid, ClassifierCache classifierCache)
  {
    GeoObjectOverTime geoObj = super.toGeoObjectOverTime(generateUid, classifierCache);
    
    for (ExternalId id : this.getAllExternalIds())
    {
      geoObj.addAlternateId(id.toDTO());
    }
    
    return geoObj;
  }
  
  @Override
  public GeoObject toGeoObject(Date date, boolean includeExternalIds)
  {
    GeoObject geoObj = super.toGeoObject(date, includeExternalIds);
    
    if (includeExternalIds)
    {
      for (ExternalId id : this.getAllExternalIds())
      {
        geoObj.addAlternateId(id.toDTO());
      }
    }
    
    return geoObj;
  }
  
  @Override
  public void setGeometry(Geometry geometry, Date startDate, Date endDate)
  {
    if (geometry != null && geometry.getNumPoints() > GeoregistryProperties.getMaxNumberOfPoints())
    {
      throw new GeometrySizeException();
    }
    
    super.setGeometry(geometry, startDate, endDate);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void setValue(String attributeName, Object value)
  {
    AttributeType at = this.type.getAttribute(attributeName).orElse(null);

    if (at instanceof AttributeListType && at.getName().equals(DefaultAttribute.ALT_IDS.getName()))
    {
      this.setAlternateIds((List<AlternateId>) value);
    }
    else
    {
      super.setValue(attributeName, value);
    }
  }
  
}
