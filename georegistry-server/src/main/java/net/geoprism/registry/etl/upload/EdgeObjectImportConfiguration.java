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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.runwaysdk.session.Request;

import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.etl.upload.EdgeObjectImporter.ReferenceStrategy;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.model.EdgeType;
import net.geoprism.registry.service.business.EdgeTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.view.EdgeObjectImportConfigurationDTO;
import net.geoprism.registry.view.TypeClass;
import net.geoprism.registry.view.TypeInfo;

public class EdgeObjectImportConfiguration extends ImportConfiguration
{
  public static final String                           PARENT_EXCLUSION          = "##PARENT##";

  public static final String                           TARGET                    = "target";

  public static final String                           BASE_TYPE                 = "baseType";

  public static final String                           TEXT                      = "text";

  public static final String                           NUMERIC                   = "numeric";

  public static final String                           GRAPH_TYPE_CODE           = "graphTypeCode";

  public static final String                           GRAPH_TYPE_CLASS          = "graphTypeClass";

  public static final String                           DIRECTION                 = "direction";

  public static final String                           LOCATIONS                 = "locations";

  public static final String                           TYPE                      = "type";

  public static final String                           SHEET                     = "sheet";

  public static final String                           EXCLUSIONS                = "exclusions";

  public static final String                           VALUE                     = "value";

  public static final String                           DATE_FORMAT               = "yyyy-MM-dd";

  public static final String                           MATCH_STRATEGY            = "matchStrategy";

  public static final String                           VALIDATE                  = "validate";

  public static final String                           EDGE_SOURCE               = "edgeSource";

  public static final String                           EDGE_SOURCE_STRATEGY      = "edgeSourceStrategy";

  public static final String                           EDGE_SOURCE_TYPE          = "edgeSourceType";

  public static final String                           EDGE_SOURCE_TYPE_STRATEGY = "edgeSourceTypeStrategy";

  public static final String                           EDGE_TARGET               = "edgeTarget";

  public static final String                           EDGE_TARGET_STRATEGY      = "edgeTargetStrategy";

  public static final String                           EDGE_TARGET_TYPE          = "edgeTargetType";

  public static final String                           EDGE_TARGET_TYPE_STRATEGY = "edgeTargetTypeStrategy";

  private String                                       edgeSource;

  private ReferenceStrategy                            edgeSourceStrategy;

  private String                                       edgeSourceType;

  private ReferenceStrategy                            edgeSourceTypeStrategy;

  private String                                       edgeTarget;

  private ReferenceStrategy                            edgeTargetStrategy;

  private String                                       edgeTargetType;

  private ReferenceStrategy                            edgeTargetTypeStrategy;

  private EdgeType                                     graphType;

  private boolean                                      validate;

  private LinkedList<EdgeObjectRecordedErrorException> errors                    = new LinkedList<EdgeObjectRecordedErrorException>();

  private EdgeTypeBusinessServiceIF                    service;

  public EdgeObjectImportConfiguration()
  {
    this.service = ServiceFactory.getBean(EdgeTypeBusinessServiceIF.class);

    this.functions = new HashMap<String, ShapefileFunction>();
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

  public boolean isValidate()
  {
    return validate;
  }

  public void setValidate(boolean validate)
  {
    this.validate = validate;
  }

  public EdgeType getGraphType()
  {
    return graphType;
  }

  public void setGraphType(EdgeType graphType)
  {
    this.graphType = graphType;
  }

  /**
   * Be careful when using this method because if an import was resumed half-way
   * through then this won't include errors which were created last time the
   * import ran. You probably want to query the database instead.
   * 
   * @return
   */
  public LinkedList<EdgeObjectRecordedErrorException> getExceptions()
  {
    return this.errors;
  }

  public void addException(EdgeObjectRecordedErrorException e)
  {
    this.errors.add(e);
  }

  @Override
  public boolean hasExceptions()
  {
    return this.errors.size() > 0;
  }

  @Override
  public List<TypeInfo> getTypes()
  {
    List<TypeInfo> types = new LinkedList<>();

    if (this.edgeSourceTypeStrategy.equals(ReferenceStrategy.FIXED_TYPE))
    {
      TypeClass typeClass = this.graphType.getSourceType();

      types.add(new TypeInfo(typeClass, this.edgeSourceType));
    }

    if (this.edgeTargetTypeStrategy.equals(ReferenceStrategy.FIXED_TYPE))
    {
      TypeClass typeClass = this.graphType.getTargetType();

      types.add(new TypeInfo(typeClass, this.edgeTargetType));
    }

    return types;
  }

  @Request
  @Override
  public EdgeObjectImportConfigurationDTO toDTO()
  {
    EdgeObjectImportConfigurationDTO dto = new EdgeObjectImportConfigurationDTO();

    super.toDTO(dto);

    if (this.getGraphType() != null)
    {
      dto.setGraphTypeCode(this.getGraphType().getCode());
      dto.setGraphTypeClass(EdgeType.getTypeCode(this.getGraphType()));
    }

    dto.setValidate(this.isValidate());

    dto.setEdgeSource(edgeSource);
    dto.setEdgeSourceStrategy(edgeSourceStrategy);
    dto.setEdgeSourceType(edgeSourceType);
    dto.setEdgeSourceTypeStrategy(edgeSourceTypeStrategy);

    dto.setEdgeTarget(edgeTarget);
    dto.setEdgeTargetStrategy(edgeTargetStrategy);
    dto.setEdgeTargetType(edgeTargetType);
    dto.setEdgeTargetTypeStrategy(edgeTargetTypeStrategy);

    return dto;
  }

  @Request
  public EdgeObjectImportConfiguration fromDTO(EdgeObjectImportConfigurationDTO dto, boolean includeCoordinates)
  {
    super.fromDTO(dto);

    edgeSource = dto.getEdgeSource();
    edgeSourceStrategy = dto.getEdgeSourceTypeStrategy();
    edgeSourceType = dto.getEdgeSourceType();
    edgeSourceTypeStrategy = dto.getEdgeSourceTypeStrategy();

    edgeTarget = dto.getEdgeTarget();
    edgeTargetStrategy = dto.getEdgeTargetTypeStrategy();
    edgeTargetType = dto.getEdgeTargetType();
    edgeTargetTypeStrategy = dto.getEdgeTargetTypeStrategy();

    this.setValidate(dto.getValidate());

    this.setGraphType(this.service.getByCode(dto.getGraphTypeClass(), dto.getGraphTypeCode()));

    return this;
  }

  @Override
  public void enforceCreatePermissions()
  {
    // TODO determine permissions
  }

  @Override
  public void enforceExecutePermissions()
  {
    // TODO determine permissions
  }

  @Override
  public void populate(ImportHistory history)
  {
    // Organization org = graphType.getOrganization().getOrganization();
    //
    // history.setOrganization(org);
    // history.setGeoObjectTypeCode(type.getCode());
  }
}
