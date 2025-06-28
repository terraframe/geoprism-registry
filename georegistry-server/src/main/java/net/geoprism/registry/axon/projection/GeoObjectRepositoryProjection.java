package net.geoprism.registry.axon.projection;

import java.util.Date;

import org.axonframework.eventhandling.EventHandler;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.dataaccess.MdEdgeDAOIF;
import com.runwaysdk.dataaccess.transaction.Transaction;

import net.geoprism.registry.ListType;
import net.geoprism.registry.action.ExecuteOutOfDateChangeRequestException;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectEvent;
import net.geoprism.registry.axon.event.remote.RemoteGeoObjectSetParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectApplyEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectCreateParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectRemoveParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectSetExternalIdEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectUpdateParentEvent;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.graph.VertexComponent;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.business.GPRGeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.HierarchyTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;

@Service
public class GeoObjectRepositoryProjection
{
  @Autowired
  private HierarchyTypeBusinessServiceIF hService;

  @Autowired
  private GPRGeoObjectBusinessServiceIF  service;

  @EventHandler
  @Transaction
  public void applyGeoObject(GeoObjectApplyEvent event) throws Exception
  {
    GeoObjectOverTime dto = GeoObjectOverTime.fromJSON(ServiceFactory.getAdapter(), event.getObject());

    ServerGeoObjectIF object = this.service.apply(dto, event.getIsNew(), event.getIsImport(), true);

    final ServerGeoObjectType type = object.getType();

    // if (!StringUtils.isBlank(event.getParents()))
    // {
    // ServerParentTreeNodeOverTime ptnOt =
    // ServerParentTreeNodeOverTime.fromJSON(type, event.getParents());
    //
    // this.service.setParents(object, ptnOt);
    // }

    if (event.getRefreshWorking())
    {
      updateWorkingLists(object, type);
    }
  }

  @EventHandler
  @Transaction
  public void removeParent(GeoObjectRemoveParentEvent event) throws Exception
  {
    ServerHierarchyType hierarchyType = this.hService.get(event.getEdgeType());

    ServerGeoObjectIF object = this.service.getGeoObject(event.getUid(), event.getType());
    EdgeObject edge = object.getEdge(hierarchyType, event.getEdgeUid());

    if (edge == null)
    {
      throw new ExecuteOutOfDateChangeRequestException();
    }

    edge.delete();

    if (event.getRefreshWorking())
    {
      updateWorkingLists(object);
    }
  }

  @EventHandler
  @Transaction
  public void updateParent(GeoObjectUpdateParentEvent event) throws Exception
  {
    ServerGeoObjectIF object = this.service.getGeoObject(event.getUid(), event.getType());
    ServerHierarchyType hierarchy = this.hService.get(event.getEdgeType());

    EdgeObject edge = object.getEdge(hierarchy, event.getEdgeUid());

    if (edge == null)
    {
      throw new ExecuteOutOfDateChangeRequestException();
    }

    if (event.getParentType() != null && event.getParentCode() != null)
    {
      VertexServerGeoObject newParent = (VertexServerGeoObject) this.service.getGeoObjectByCode(event.getParentCode(), event.getParentType());

      // Parent values can only be changed by deleting the current edge and
      // creating a new one unfortunately
      if (!edge.getParent().getOid().equals(newParent.getRunwayId()))
      {
        Date _newStartDate = event.getStartDate();
        Date _newEndDate = event.getEndDate();

        if (_newStartDate == null)
        {
          _newStartDate = edge.getObjectValue(GeoVertex.START_DATE);
        }

        if (_newEndDate == null)
        {
          _newEndDate = edge.getObjectValue(GeoVertex.END_DATE);
        }

        edge.delete();

        // We unfortunately can't use this method because we have to bypass
        // the votc reordering and validation
        // go.addParent(newParent, hierarchyType, _newStartDate,
        // _newEndDate);

        EdgeObject newEdge = object.getVertex().addParent( ( (VertexComponent) newParent ).getVertex(), hierarchy.getObjectEdge());
        newEdge.setValue(GeoVertex.START_DATE, _newStartDate);
        newEdge.setValue(GeoVertex.END_DATE, _newEndDate);
        newEdge.setValue(DefaultAttribute.UID.getName(), event.getEdgeUid());
        newEdge.apply();
      }
    }
    else
    {
      if (event.getStartDate() != null)
      {
        edge.setValue(GeoVertex.START_DATE, event.getStartDate());
      }

      if (event.getEndDate() != null)
      {
        edge.setValue(GeoVertex.END_DATE, event.getEndDate());
      }

      edge.apply();
    }

    if (event.getRefreshWorking())
    {
      updateWorkingLists(object);
    }
  }

  @EventHandler
  @Transaction
  public void createParent(GeoObjectCreateParentEvent event) throws Exception
  {
    ServerGeoObjectIF object = this.service.getGeoObject(event.getUid(), event.getType());
    ServerHierarchyType hierarchy = this.hService.get(event.getEdgeType());
    VertexServerGeoObject newParent = (VertexServerGeoObject) this.service.getGeoObjectByCode(event.getParentCode(), event.getParentType());

    if (event.getValidate())
    {
      this.service.addParent(object, newParent, hierarchy, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), true);
    }
    else
    {
      MdEdgeDAOIF mdEdge = hierarchy.getMdEdgeDAO();

      this.service.addParentRaw(object, newParent.getVertex(), mdEdge, event.getStartDate(), event.getEndDate(), event.getEdgeUid(), true);
    }

    if (event.getRefreshWorking())
    {
      updateWorkingLists(object);
    }
  }

  @EventHandler
  @Transaction
  public void setExternalId(GeoObjectSetExternalIdEvent event) throws Exception
  {
    ServerGeoObjectIF object = this.service.getGeoObject(event.getUid(), event.getType());
    String systemId = event.getSystemId();

    ExternalSystem system = ExternalSystem.getByExternalSystemId(systemId);

    this.service.createExternalId(object, system, event.getExternalId(), event.getStrategy());

    if (event.getRefreshWorking())
    {
      updateWorkingLists(object);
    }
  }

  protected void updateWorkingLists(ServerGeoObjectIF object)
  {
    this.updateWorkingLists(object, object.getType());
  }

  protected void updateWorkingLists(ServerGeoObjectIF object, final ServerGeoObjectType type)
  {
    // Update all of the working lists which have this record
    ListType.getForType(type).forEach(listType -> {
      listType.getWorkingVersions().forEach(version -> version.publishOrUpdateRecord(object));
    });
  }

  @EventHandler
  @Transaction
  public void handleRemoteGeoObject(RemoteGeoObjectEvent event) throws Exception
  {
    System.out.println("Handling remote geo object: [" + event.getType() + "][" + event.getUid() + "] - [" + event.getIsNew() + "]");
  }

  @EventHandler
  @Transaction
  public void handleRemoteParent(RemoteGeoObjectSetParentEvent event) throws Exception
  {
    System.out.println("Handling remote set parent: [" + event.getType() + "][" + event.getUid() + "] - [" + event.getParentCode() + "]");
  }

}
