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
package net.geoprism.registry.view;

import net.geoprism.registry.etl.ObjectImporterFactory.JobHistoryType;
import net.geoprism.registry.etl.upload.EdgeObjectImporter.ReferenceStrategy;

public class EdgeObjectImportConfigurationDTO extends ImportConfigurationDTO
{
  private String            graphTypeClass;

  private String            graphTypeCode;

  private String            edgeSource;

  private ReferenceStrategy edgeSourceStrategy;

  private String            edgeSourceType;

  private ReferenceStrategy edgeSourceTypeStrategy;

  private String            edgeTarget;

  private ReferenceStrategy edgeTargetStrategy;

  private String            edgeTargetType;

  private ReferenceStrategy edgeTargetTypeStrategy;

  private Boolean           validate;

  public EdgeObjectImportConfigurationDTO()
  {
    super();

    this.setObjectType(JobHistoryType.EDGE_OBJECT);
    this.setValidate(false);
  }

  public String getGraphTypeClass()
  {
    return graphTypeClass;
  }

  public void setGraphTypeClass(String graphTypeClass)
  {
    this.graphTypeClass = graphTypeClass;
  }

  public String getGraphTypeCode()
  {
    return graphTypeCode;
  }

  public void setGraphTypeCode(String graphTypeCode)
  {
    this.graphTypeCode = graphTypeCode;
  }

  public String getEdgeSource()
  {
    return edgeSource;
  }

  public void setEdgeSource(String edgeSource)
  {
    this.edgeSource = edgeSource;
  }

  public ReferenceStrategy getEdgeSourceStrategy()
  {
    return edgeSourceStrategy;
  }

  public void setEdgeSourceStrategy(ReferenceStrategy edgeSourceStrategy)
  {
    this.edgeSourceStrategy = edgeSourceStrategy;
  }

  public String getEdgeSourceType()
  {
    return edgeSourceType;
  }

  public void setEdgeSourceType(String edgeSourceType)
  {
    this.edgeSourceType = edgeSourceType;
  }

  public ReferenceStrategy getEdgeSourceTypeStrategy()
  {
    return edgeSourceTypeStrategy;
  }

  public void setEdgeSourceTypeStrategy(ReferenceStrategy edgeSourceTypeStrategy)
  {
    this.edgeSourceTypeStrategy = edgeSourceTypeStrategy;
  }

  public String getEdgeTarget()
  {
    return edgeTarget;
  }

  public void setEdgeTarget(String edgeTarget)
  {
    this.edgeTarget = edgeTarget;
  }

  public ReferenceStrategy getEdgeTargetStrategy()
  {
    return edgeTargetStrategy;
  }

  public void setEdgeTargetStrategy(ReferenceStrategy edgeTargetStrategy)
  {
    this.edgeTargetStrategy = edgeTargetStrategy;
  }

  public String getEdgeTargetType()
  {
    return edgeTargetType;
  }

  public void setEdgeTargetType(String edgeTargetType)
  {
    this.edgeTargetType = edgeTargetType;
  }

  public ReferenceStrategy getEdgeTargetTypeStrategy()
  {
    return edgeTargetTypeStrategy;
  }

  public void setEdgeTargetTypeStrategy(ReferenceStrategy edgeTargetTypeStrategy)
  {
    this.edgeTargetTypeStrategy = edgeTargetTypeStrategy;
  }

  public Boolean getValidate()
  {
    return validate;
  }

  public void setValidate(Boolean validate)
  {
    this.validate = validate;
  }

}
