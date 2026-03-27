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
package net.geoprism.registry.service.request;

import java.util.List;

import org.commongeoregistry.adapter.metadata.GraphTypeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.DataNotFoundException;
import net.geoprism.registry.DirectedAcyclicGraphType;
import net.geoprism.registry.UndirectedGraphType;
import net.geoprism.registry.etl.ObjectImporterFactory;
import net.geoprism.registry.service.business.ETLBusinessService;
import net.geoprism.registry.service.business.UndirectedGraphTypeBusinessServiceIF;
import net.geoprism.registry.view.ImportHistoryView;

@Service
public class UndirectedGraphTypeService
{
  @Autowired
  private UndirectedGraphTypeBusinessServiceIF service;

  @Autowired
  private ETLBusinessService                   etlBusinessService;

  @Request(RequestType.SESSION)
  public List<GraphTypeDTO> getAll(String sessionId)
  {
    List<UndirectedGraphType> types = this.service.getAll();

    return types.stream().map(child -> child.toDTO()).toList();
  }

  @Request(RequestType.SESSION)
  public GraphTypeDTO apply(String sessionId, GraphTypeDTO object)
  {
    String code = object.getCode();

    return service.getByCode(code).map(type -> {

      this.service.update(type, object);

      return type.toDTO();

    }).orElseGet(() -> {

      UndirectedGraphType type = this.service.create(object);

      return type.toDTO();
    });
  }

  @Request(RequestType.SESSION)
  public GraphTypeDTO get(String sessionId, String code)
  {
    UndirectedGraphType type = service.getByCode(code).orElseThrow(() -> {
      DataNotFoundException ex = new DataNotFoundException();
      ex.setTypeLabel("DAG");
      ex.setAttributeLabel(DirectedAcyclicGraphType.CODE);
      ex.setDataIdentifier(code);

      return ex;
    });

    return type.toDTO();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String code)
  {
    service.getByCode(code).ifPresent(type -> {
      this.service.delete(type);
    });
  }

  @Request(RequestType.SESSION)
  public List<ImportHistoryView> getHistory(String sessionId, String code)
  {
    return this.etlBusinessService.getHistory(ObjectImporterFactory.ObjectImportType.EDGE_OBJECT.name(), code, GraphTypeDTO.UNDIRECTED_GRAPH_TYPE);
  }

}
