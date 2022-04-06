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
package net.geoprism.registry.etl;

import net.geoprism.registry.etl.upload.GeoObjectImporter;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.etl.upload.ImportProgressListenerIF;
import net.geoprism.registry.etl.upload.ObjectImporterIF;
import net.geoprism.registry.etl.upload.BusinessObjectImportConfiguration;
import net.geoprism.registry.etl.upload.BusinessObjectImporter;
import net.geoprism.registry.io.GeoObjectImportConfiguration;

public class ObjectImporterFactory
{
  public static enum ObjectImportType {
    GEO_OBJECT, BUSINESS_OBJECT
  }

  public static ObjectImporterIF getImporter(String type, ImportConfiguration config, ImportProgressListenerIF progress)
  {
    if (type.equals(ObjectImportType.GEO_OBJECT.name()))
    {
      return new GeoObjectImporter((GeoObjectImportConfiguration) config, progress);
    }
    else if (type.equals(ObjectImportType.BUSINESS_OBJECT.name()))
    {
      return new BusinessObjectImporter((BusinessObjectImportConfiguration) config, progress);
    }
    else
    {
      throw new UnsupportedOperationException();
    }
  }
}
