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

import java.util.List;
import java.util.Optional;

import com.google.gson.JsonArray;

import net.geoprism.registry.axon.event.remote.RemoteEvent;
import net.geoprism.registry.model.DataSourceDTO;
import net.geoprism.registry.model.SourceAuthorityDTO;
import net.geoprism.registry.view.BusinessEdgeTypeDTO;
import net.geoprism.registry.view.BusinessTypeDTO;
import net.geoprism.registry.view.CommitDTO;
import net.geoprism.registry.view.ConceptClassDTO;
import net.geoprism.registry.view.ConceptEdgeTypeDTO;
import net.geoprism.registry.view.ConceptSetDTO;
import net.geoprism.registry.view.PublishDTO;

public interface RemoteClientIF extends AutoCloseable
{
  public void close();

  public List<PublishDTO> getAll();

  public Optional<PublishDTO> getPublish(String publishId);

  public Optional<CommitDTO> getLatest(String publishId);

  public List<CommitDTO> getDependencies(String commitId);

  public List<BusinessTypeDTO> getBusinessTypes(String commitId);

  public List<ConceptClassDTO> getConceptClasses(String commitId);

  public List<ConceptEdgeTypeDTO> getConceptEdgeTypes(String commitId);

  public List<ConceptSetDTO> getConceptSets(String commitId);

  public JsonArray getGeoObjectTypes(String commitId);

  public List<BusinessEdgeTypeDTO> getBusinessEdgeTypes(String uid);

  public JsonArray getHierarchyTypes(String uid);

  public JsonArray getDirectedAcyclicGraphTypes(String uid);

  public JsonArray getUndirectedGraphTypes(String uid);

  public List<RemoteEvent> getRemoteEvents(String uid, Integer chunk);

  public List<DataSourceDTO> getDataSources(String uid);

  public List<SourceAuthorityDTO> getSourceAuthorities(String uid);

}
