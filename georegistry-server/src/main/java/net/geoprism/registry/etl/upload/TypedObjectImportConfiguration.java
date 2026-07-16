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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import com.runwaysdk.session.Request;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.registry.graph.ObjectClass;
import net.geoprism.registry.io.LocalizedValueFunction;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.view.BusinessObjectImportConfigurationDTO;
import net.geoprism.registry.view.ExclusionDTO;
import net.geoprism.registry.view.ImportTypeDTO;
import net.geoprism.registry.view.TypeInfo;
import net.geoprism.registry.view.TypedObjectImportConfigurationDTO;

public abstract class TypedObjectImportConfiguration<T extends ObjectClass> extends ImportConfiguration
{
  private T                                        type;

  private Map<String, Set<String>>                 exclusions;

  private LinkedList<ObjectRecordedErrorException> errors = new LinkedList<ObjectRecordedErrorException>();

  public TypedObjectImportConfiguration()
  {
    super();

    this.exclusions = new HashMap<String, Set<String>>();
  }

  protected abstract void setType(TypedObjectImportConfigurationDTO dto);

  public T getType()
  {
    return type;
  }

  public void setType(T type)
  {
    this.type = type;
  }

  @Override
  public List<TypeInfo> getTypes()
  {
    return Arrays.asList(this.type.getTypeInfo());
  }

  public Map<String, Set<String>> getExclusions()
  {
    return exclusions;
  }

  public Set<String> getExclusions(String attributeName)
  {
    return exclusions.get(attributeName);
  }

  public void setExclusions(Map<String, Set<String>> exclusions)
  {
    this.exclusions = exclusions;
  }

  public void addExclusion(String attributeName, String value)
  {
    if (!this.exclusions.containsKey(attributeName))
    {
      this.exclusions.put(attributeName, new TreeSet<String>());
    }

    this.exclusions.get(attributeName).add(value);
  }

  public void addExclusion(String attributeName, Set<String> value)
  {
    this.exclusions.put(attributeName, value);
  }

  public boolean isExclusion(String attributeName, String value)
  {
    return ( this.exclusions.get(attributeName) != null && this.exclusions.get(attributeName).contains(value) );
  }

  /**
   * Be careful when using this method because if an import was resumed half-way
   * through then this won't include errors which were created last time the
   * import ran. You probably want to query the database instead.
   * 
   * @return
   */
  public LinkedList<ObjectRecordedErrorException> getExceptions()
  {
    return this.errors;
  }

  public void addException(ObjectRecordedErrorException e)
  {
    this.errors.add(e);
  }

  @Override
  public boolean hasExceptions()
  {
    return this.errors.size() > 0;
  }

  @Request
  public BusinessObjectImportConfigurationDTO toDTO()
  {

    ImportTypeDTO type = toTypeDTO(this.type, this.functions);

    List<ExclusionDTO> exclusions = this.exclusions.entrySet().stream().map(e -> new ExclusionDTO(e.getKey(), e.getValue())).toList();

    BusinessObjectImportConfigurationDTO config = new BusinessObjectImportConfigurationDTO();
    config.setType(type);
    config.setExclusions(exclusions);
    super.toDTO(config);

    return config;
  }

  @Request
  public TypedObjectImportConfiguration<T> fromDTO(TypedObjectImportConfigurationDTO dto, boolean includeCoordinates)
  {
    super.fromDTO(dto);

    setType(dto);

    dto.getExclusions().stream().forEach(exclusion -> {
      this.addExclusion(exclusion.getCode(), exclusion.getValue());
    });

    dto.getType().getAttributes().forEach(attribute -> {
      if (attribute.getFunction() != null)
      {
        this.setFunction(attribute.getCode(), this.fromDTO(attribute.getFunction()));
      }
      else if (!StringUtils.isBlank(attribute.getTarget()))
      {
        if (!StringUtils.isBlank(attribute.getLocale()))
        {
          if (!this.functions.containsKey(attribute.getCode()))
          {
            this.functions.put(attribute.getCode(), new LocalizedValueFunction());
          }

          LocalizedValueFunction function = (LocalizedValueFunction) this.getFunction(attribute.getCode());

          function.add(attribute.getLocale(), new BasicColumnFunction(attribute.getTarget()));
        }
        else
        {
          this.setFunction(attribute.getCode(), new BasicColumnFunction(attribute.getTarget()));
        }
      }
    });

    return this;
  }

  @Override
  public void validate()
  {
    super.validate();
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
    history.setOrganization(type.getServerOrganization().getOrganization());
    history.setGeoObjectTypeCode(this.getType().getCode());
  }
}
