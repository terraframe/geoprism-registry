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
package net.geoprism.registry.etl.upload;

import java.util.List;

import net.geoprism.registry.axon.event.repository.ConceptObjectEventBuilder;
import net.geoprism.registry.axon.event.repository.RepositoryEvent;
import net.geoprism.registry.graph.ConceptClass;
import net.geoprism.registry.model.ConceptObject;
import net.geoprism.registry.service.business.ConceptObjectBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.view.ConceptClassDTO;

public class ConceptObjectImporter extends ObjectImporter<ConceptObject, ConceptClass, ConceptClassDTO> implements ObjectImporterIF
{

  private ConceptObjectBusinessServiceIF service;

  public ConceptObjectImporter(ConceptObjectImportConfiguration configuration, ImportProgressListenerIF progressListener)
  {
    super(configuration, progressListener);

    this.service = ServiceFactory.getBean(ConceptObjectBusinessServiceIF.class);
  }

  @Override
  protected ConceptObjectBusinessServiceIF getService()
  {
    return this.service;
  }

  @Override
  protected List<RepositoryEvent> buildEvents(ConceptObject object, boolean isNew)
  {
    ConceptObjectEventBuilder eventBuilder = new ConceptObjectEventBuilder(this.service);
    eventBuilder.setObject(object, isNew);
    eventBuilder.setAttributeUpdate(true);

    return eventBuilder.build();
  }
}
