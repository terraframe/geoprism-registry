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
package net.geoprism.registry.service.request;

import java.io.InputStream;
import java.util.Set;

import org.springframework.stereotype.Component;

import net.geoprism.registry.service.business.BulkExportSection;

@Component
public interface BulkImportExportServiceIF
{

  /**
   * Creates a bulk export of the given sections of the data in the system
   * 
   * @param sessionId
   * @param sections
   */
  public InputStream exportData(String sessionId, Set<BulkExportSection> sections);

  /**
   * Deletes the data in the system
   */
  public void deleteData(String sessionId);

  public String importData(String sessionId, InputStream istream);

}
