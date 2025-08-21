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
package net.geoprism.registry.view.action;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.runwaysdk.business.graph.EdgeObject;

import net.geoprism.registry.action.ExecuteOutOfDateChangeRequestException;
import net.geoprism.registry.action.InvalidChangeRequestException;
import net.geoprism.registry.axon.event.repository.GeoObjectCreateParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectRemoveParentEvent;
import net.geoprism.registry.axon.event.repository.GeoObjectUpdateParentEvent;
import net.geoprism.registry.axon.event.repository.ServerGeoObjectEventBuilder;
import net.geoprism.registry.conversion.VertexGeoObjectStrategy;
import net.geoprism.registry.graph.GeoVertex;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class UpdateParentValueOverTimeView extends UpdateValueOverTimeView
{
  public static final String VALUE_SPLIT_TOKEN = "_~VST~_";

  // @Override
  // public void execute(UpdateChangeOverTimeAttributeView cotView,
  // VertexServerGeoObject go, List<ValueOverTime> looseVotc)
  // {
  // throw new UnsupportedOperationException();
  // }

  // public void executeParent(UpdateChangeOverTimeAttributeView cotView,
  // VertexServerGeoObject go, SortedSet<EdgeObject> looseVotc)
  // {
  // if (cotView instanceof UpdateParentView)
  // {
  // UpdateParentView parentView = (UpdateParentView) cotView;
  // final ServerHierarchyType hierarchyType =
  // ServerHierarchyType.get(parentView.getHierarchyCode());
  //
  // if (this.action.equals(UpdateActionType.DELETE))
  // {
  // EdgeObject edge = this.getEdgeByOid(looseVotc, this.oid).orElseThrow(() ->
  // {
  // throw new ExecuteOutOfDateChangeRequestException();
  // });
  //
  // edge.delete();
  // looseVotc.remove(edge);
  // }
  // else if (this.action.equals(UpdateActionType.UPDATE))
  // {
  // EdgeObject edge = this.getEdgeByOid(looseVotc, this.oid).orElseThrow(() ->
  // {
  // throw new ExecuteOutOfDateChangeRequestException();
  // });
  //
  // final VertexServerGeoObject newParent = this.getNewValueAsGO();
  // final String parentCode = newParent == null ? null : newParent.getCode();
  //
  // String currentCode =
  // edge.getParent().getObjectValue(DefaultAttribute.CODE.getName());
  //
  // // Parent values can only be changed by deleting the current edge and
  // // creating a new one unfortunately
  // if (this.newValue != null && ( !currentCode.equals(parentCode) ))
  // {
  // Date _newStartDate = this.newStartDate;
  // Date _newEndDate = this.newEndDate;
  //
  // if (_newStartDate == null)
  // {
  // _newStartDate = edge.getObjectValue(GeoVertex.START_DATE);
  // }
  //
  // if (_newEndDate == null)
  // {
  // _newEndDate = edge.getObjectValue(GeoVertex.END_DATE);
  // }
  //
  // edge.delete();
  // looseVotc.remove(edge);
  //
  // if (newParent != null)
  // {
  // // We unfortunately can't use this method because we have to bypass
  // // the votc reordering and validation
  // // go.addParent(newParent, hierarchyType, _newStartDate,
  // // _newEndDate);
  //
  // EdgeObject newEdge = go.getVertex().addParent( ( (VertexComponent)
  // newParent ).getVertex(), hierarchyType.getObjectEdge());
  // newEdge.setValue(GeoVertex.START_DATE, _newStartDate);
  // newEdge.setValue(GeoVertex.END_DATE, _newEndDate);
  // newEdge.apply();
  //
  // looseVotc.add(newEdge);
  // }
  //
  // return;
  // }
  //
  // if (newStartDate != null)
  // {
  // edge.setValue(GeoVertex.START_DATE, newStartDate);
  // }
  //
  // if (newEndDate != null)
  // {
  // edge.setValue(GeoVertex.END_DATE, newEndDate);
  // }
  //
  // edge.apply();
  // }
  // else if (this.action.equals(UpdateActionType.CREATE))
  // {
  // final VertexServerGeoObject newParent = this.getNewValueAsGO();
  //
  // if (newParent == null || this.newStartDate == null || this.newEndDate ==
  // null)
  // {
  // throw new InvalidChangeRequestException();
  // }
  //
  // EdgeObject edge = go.getEdge(newParent, hierarchyType, this.newStartDate,
  // this.newEndDate);
  //
  // if (edge != null)
  // {
  // ExecuteOutOfDateChangeRequestException ex = new
  // ExecuteOutOfDateChangeRequestException();
  // throw ex;
  // }
  //
  // // We unfortunately can't use this method because we have to bypass the
  // // votc reordering and validation
  // // go.addParent(newParent, hierarchyType, this.newStartDate,
  // // this.newEndDate);
  //
  // EdgeObject newEdge = go.getVertex().addParent( ( (VertexComponent)
  // newParent ).getVertex(), hierarchyType.getObjectEdge());
  // newEdge.setValue(GeoVertex.START_DATE, this.newStartDate);
  // newEdge.setValue(GeoVertex.END_DATE, this.newEndDate);
  // newEdge.apply();
  //
  // looseVotc.add(newEdge);
  // }
  // else
  // {
  // throw new UnsupportedOperationException("Unsupported action type [" +
  // this.action + "].");
  // }
  // }
  // }

  protected String           oldDataSource;

  protected String           newDataSource;

  protected Optional<EdgeObject> getEdgeByOid(Collection<EdgeObject> edges, String oid)
  {
    return edges.stream().filter(edge -> edge.getOid().equals(oid)).findFirst();
  }

  protected Optional<EdgeObject> getEdgeByDate(Collection<EdgeObject> edges, Date startDate, Date endDate)
  {
    return edges.stream().filter(edge -> {
      Date edgeStartDate = edge.getObjectValue(GeoVertex.START_DATE);
      Date edgeEndDate = edge.getObjectValue(GeoVertex.END_DATE);

      return edgeStartDate.equals(startDate) && edgeEndDate.equals(endDate);
    }).findFirst();
  }

  public VertexServerGeoObject getNewValueAsGO()
  {
    if (this.newValue != null && !this.newValue.isJsonNull())
    {
      String[] newValueSplit = ( this.getNewValue().getAsString() ).split(VALUE_SPLIT_TOKEN);
      String parentTypeCode = newValueSplit[0];
      String parentCode = newValueSplit[1];

      ServerGeoObjectType parentType = ServerGeoObjectType.get(parentTypeCode);
      final VertexServerGeoObject parent = new VertexGeoObjectStrategy(parentType).getGeoObjectByCode(parentCode);

      return parent;
    }

    return null;
  }

  @Override
  public boolean isEdge()
  {
    return true;
  }

  public void buildParent(ServerGeoObjectEventBuilder builder, UpdateChangeOverTimeAttributeView cotView, Collection<EdgeObject> collection)
  {
    if (cotView instanceof UpdateParentView)
    {
      VertexServerGeoObject go = builder.getOrThrow();
      UpdateParentView parentView = (UpdateParentView) cotView;
      final ServerHierarchyType hierarchyType = ServerHierarchyType.get(parentView.getHierarchyCode());

      if (this.action.equals(UpdateActionType.DELETE))
      {
        builder.addEvent(this.getEdgeByOid(collection, this.oid).map(edge -> {
          Date startDate = edge.getObjectValue(GeoVertex.START_DATE);
          Date endDate = edge.getObjectValue(GeoVertex.END_DATE);
          String uid = edge.getObjectValue(DefaultAttribute.UID.getName());

          return new GeoObjectRemoveParentEvent(go.getCode(), go.getType().getCode(), uid, hierarchyType.getCode(), startDate, endDate);
        }));
      }
      else if (this.action.equals(UpdateActionType.UPDATE))
      {
        builder.addEvent(this.getEdgeByOid(collection, this.oid).map(edge -> {
          String edgeUid = edge.getObjectValue(DefaultAttribute.UID.getName());

          String parentTypeCode = null;
          String parentCode = null;

          if (this.newValue != null && !this.newValue.isJsonNull())
          {
            String[] newValueSplit = ( this.getNewValue().getAsString() ).split(VALUE_SPLIT_TOKEN);
            parentTypeCode = newValueSplit[0];
            parentCode = newValueSplit[1];
          }

          String source = StringUtils.isBlank(this.newDataSource) ? this.oldDataSource : this.newDataSource;

          return new GeoObjectUpdateParentEvent(go.getCode(), go.getType().getCode(), edgeUid, hierarchyType.getCode(), this.newStartDate, this.newEndDate, parentCode, parentTypeCode, source);
        }));
      }
      else if (this.action.equals(UpdateActionType.CREATE))
      {
        final VertexServerGeoObject newParent = this.getNewValueAsGO();

        if (newParent == null || this.newStartDate == null || this.newEndDate == null)
        {
          throw new InvalidChangeRequestException();
        }

        EdgeObject edge = go.getEdge(newParent, hierarchyType, this.newStartDate, this.newEndDate);

        if (edge != null)
        {
          throw new ExecuteOutOfDateChangeRequestException();
        }
        
        builder.addEvent(new GeoObjectCreateParentEvent(go.getCode(), go.getType().getCode(), UUID.randomUUID().toString(), hierarchyType.getCode(), this.newStartDate, this.newEndDate, newParent.getCode(), newParent.getType().getCode(), this.newDataSource, true));
      }
      else
      {
        throw new UnsupportedOperationException("Unsupported action type [" + this.action + "].");
      }
    }
  }

}
