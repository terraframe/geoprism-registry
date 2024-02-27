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
package net.geoprism.registry;

import org.commongeoregistry.adapter.metadata.RegistryRole;

public interface RegistryConstants
{
  public static final String REGISTRY_PACKAGE                 = "net.geoprism.registry";

  public static final String BUSINESS_PACKAGE                 = REGISTRY_PACKAGE + ".business";

  public static final String BUSINESS_GRAPH_PACKAGE           = REGISTRY_PACKAGE + ".business.graph";

  public static final String UNIVERSAL_GRAPH_PACKAGE          = REGISTRY_PACKAGE + ".graph";

  public static final String DAG_PACKAGE                      = REGISTRY_PACKAGE + ".dag";

  public static final String UNDIRECTED_GRAPH_PACKAGE         = REGISTRY_PACKAGE + ".undirected";

  public static final String UNIVERSAL_MDBUSINESS_PACKAGE     = REGISTRY_PACKAGE + ".universal";

  public static final String TABLE_PACKAGE                    = REGISTRY_PACKAGE + ".table";

  public static final String CLASSIFICATION_PACKAGE           = REGISTRY_PACKAGE + ".classification";

  public static final String UNIVERSAL_RELATIONSHIP_POST      = "Metadata";

  public static final String GEO_ENTITY_ATTRIBUTE_NAME        = "geoEntity";

  public static final String GEO_ENTITY_ATTRIBUTE_LABEL       = "GeoEntity";

  public static final String GEOMETRY_ATTRIBUTE_NAME          = "geom";

  public static final String GEO_POINT_ATTRIBUTE_LABEL        = "Geo Point";

  public static final String GEO_LINE_ATTRIBUTE_LABEL         = "Geo Line";

  public static final String GEO_POLYGON_ATTRIBUTE_LABEL      = "Geo Polygon";

  public static final String GEO_MULTIPOINT_ATTRIBUTE_LABEL   = "Geo MultiPoint";

  public static final String GEO_MULTILINE_ATTRIBUTE_LABEL    = "Geo MultiLine";

  public static final String GEO_MULTIPOLYGON_ATTRIBUTE_LABEL = "Geo MultiPolygon";

  public static final String REGISTRY_ROLE_PREFIX             = RegistryRole.Type.REGISTRY_ROLE_PREFIX + ".";

  public static final String REGISTRY_SUPER_ADMIN_ROLE        = REGISTRY_ROLE_PREFIX + "SRA";

  public static final String REGISTRY_MAINTAINER_PREFIX       = REGISTRY_ROLE_PREFIX + "registry.maintainer.";

  public static final String REGISTRY_ADMIN_ROLE              = REGISTRY_ROLE_PREFIX + "RegistryAdministrator";

  public static final String REGISTRY_MAINTAINER_ROLE         = REGISTRY_ROLE_PREFIX + "RegistryMaintainer";

  public static final String REGISTRY_CONTRIBUTOR_ROLE        = REGISTRY_ROLE_PREFIX + "RegistryContributor";

  public static final String API_CONSUMER_ROLE                = REGISTRY_ROLE_PREFIX + "APIConsumer";

  public final static String TERM_CLASS                       = "CLASS";

  public static final String ADMIN_USER_NAME                  = "admin";
  
  public static final String CONTROLLER_ROOT                  = "";
}
