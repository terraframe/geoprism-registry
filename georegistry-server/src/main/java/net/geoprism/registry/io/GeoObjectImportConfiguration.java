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
package net.geoprism.registry.io;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeClassificationType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;

import com.runwaysdk.session.Request;

import net.geoprism.data.importer.BasicColumnFunction;
import net.geoprism.data.importer.ShapefileFunction;
import net.geoprism.registry.etl.upload.GeoObjectRecordedErrorException;
import net.geoprism.registry.etl.upload.ImportConfiguration;
import net.geoprism.registry.graph.SourceAuthority;
import net.geoprism.registry.io.view.ExternalIdMappingDTO;
import net.geoprism.registry.io.view.GeoObjectImportConfigurationDTO;
import net.geoprism.registry.io.view.ImportTypeDTO;
import net.geoprism.registry.io.view.LocationDTO;
import net.geoprism.registry.jobs.ImportHistory;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerOrganization;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;
import net.geoprism.registry.service.business.SourceAuthorityBusinessServiceIF;
import net.geoprism.registry.service.permission.RolePermissionService;
import net.geoprism.registry.view.ExclusionDTO;
import net.geoprism.registry.view.TypeClass;
import net.geoprism.registry.view.TypeInfo;

public class GeoObjectImportConfiguration extends ImportConfiguration
{
  public static final String                          PARENT_EXCLUSION = "##PARENT##";

  public static final String                          DATA_SOURCE      = "dataSource";

  public static final String                          TARGET           = "target";

  public static final String                          BASE_TYPE        = "baseType";

  public static final String                          TEXT             = "text";

  public static final String                          LATITUDE         = "latitude";

  public static final String                          LONGITUDE        = "longitude";

  public static final String                          NUMERIC          = "numeric";

  public static final String                          LONGITUDE_KEY    = "georegistry.longitude.label";

  public static final String                          LATITUDE_KEY     = "georegistry.latitude.label";

  private ServerGeoObjectType                         type;

  private GeoObject                                   root;

  private Map<String, Set<String>>                    exclusions;

  private boolean                                     includeCoordinates;

  private List<Location>                              locations;

  private ServerHierarchyType                         hierarchy;

  private Boolean                                     postalCode;

  private LinkedList<GeoObjectRecordedErrorException> errors           = new LinkedList<GeoObjectRecordedErrorException>();

  private GeoObjectTypeBusinessServiceIF              typeService;

  private SourceAuthorityBusinessServiceIF            authorityService;

  private RolePermissionService                       permissions;

  private Map<String, ShapefileFunction>              idFunctions;

  public GeoObjectImportConfiguration()
  {
    super();

    this.typeService = ServiceFactory.getBean(GeoObjectTypeBusinessServiceIF.class);
    this.permissions = ServiceFactory.getBean(RolePermissionService.class);
    this.authorityService = ServiceFactory.getBean(SourceAuthorityBusinessServiceIF.class);

    this.includeCoordinates = false;
    this.idFunctions = new HashMap<String, ShapefileFunction>();
    this.locations = new LinkedList<Location>();
    this.exclusions = new HashMap<String, Set<String>>();
    this.postalCode = false;
  }

  public boolean isIncludeCoordinates()
  {
    return includeCoordinates;
  }

  public void setIncludeCoordinates(boolean includeCoordinates)
  {
    this.includeCoordinates = includeCoordinates;
  }

  public ServerGeoObjectType getType()
  {
    return type;
  }

  public void setType(ServerGeoObjectType type)
  {
    this.type = type;
  }

  public GeoObject getRoot()
  {
    return root;
  }

  public void setRoot(GeoObject root)
  {
    this.root = root;
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

  public Map<String, ShapefileFunction> getIdFunctions()
  {
    return idFunctions;
  }

  public void setIdFunctions(Map<String, ShapefileFunction> idFunctions)
  {
    this.idFunctions = idFunctions;
  }

  public void addIdFunction(String authority, ShapefileFunction idFunction)
  {
    this.idFunctions.put(authority, idFunction);
  }

  public void addParent(Location location)
  {
    this.locations.add(location);
  }

  public List<Location> getLocations()
  {
    return this.locations;
  }

  public ServerHierarchyType getHierarchy()
  {
    return hierarchy;
  }

  public void setHierarchy(ServerHierarchyType hierarchy)
  {
    this.hierarchy = hierarchy;
  }

  public Boolean isPostalCode()
  {
    return postalCode;
  }

  public void setPostalCode(Boolean postalCode)
  {
    this.postalCode = postalCode;
  }

  /**
   * Be careful when using this method because if an import was resumed half-way
   * through then this won't include errors which were created last time the
   * import ran. You probably want to query the database instead.
   * 
   * @return
   */
  public LinkedList<GeoObjectRecordedErrorException> getExceptions()
  {
    return this.errors;
  }

  public void addException(GeoObjectRecordedErrorException e)
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
    return Arrays.asList(new TypeInfo(TypeClass.GEO_OBJECT_TYPE, this.type.getCode()));
  }

  @Request
  @Override
  public GeoObjectImportConfigurationDTO toDTO()
  {
    ImportTypeDTO type = toTypeDTO(this.type, this.functions);

    List<ExclusionDTO> exclusions = this.exclusions.entrySet().stream().map(e -> new ExclusionDTO(e.getKey(), e.getValue())).toList();

    GeoObjectImportConfigurationDTO config = new GeoObjectImportConfigurationDTO();
    super.toDTO(config);
    config.setPostalCode(postalCode);
    config.setType(type);
    config.setExclusions(exclusions);

    for (Location location : this.locations)
    {
      config.addLocation(location.toDTO());
    }

    this.idFunctions.forEach((key, function) -> {

      ExternalIdMappingDTO dto = new ExternalIdMappingDTO();
      dto.setAuthority(key);
      dto.setFunction(toDTO(function));

      config.addIdMapping(dto);
    });

    if (this.hierarchy != null)
    {
      config.setHierarchy(this.getHierarchy().getCode());
    }

    return config;
  }

  @Request
  public GeoObjectImportConfiguration fromDTO(GeoObjectImportConfigurationDTO dto, boolean includeCoordinates)
  {
    super.fromDTO(dto);

    ServerGeoObjectType type = ServerGeoObjectType.get(dto.getType().getCode());

    this.setType(type);
    this.setIncludeCoordinates(includeCoordinates);
    this.setPostalCode(dto.getPostalCode());

    dto.getExclusions().stream().forEach(exclusion -> {
      this.addExclusion(exclusion.getCode(), exclusion.getValue());
    });

    dto.getType().getAttributes().forEach(attribute -> {
      if (attribute.getFunction() != null)
      {
        this.setFunction(attribute.getCode(), this.fromDTO(attribute.getFunction()));
      }
      else if (StringUtils.isNotBlank(attribute.getTarget()))
      {
        if (StringUtils.isNotBlank(attribute.getLocale()))
        {
          this.functions.putIfAbsent(attribute.getCode(), new LocalizedValueFunction());

          LocalizedValueFunction function = (LocalizedValueFunction) this.functions.get(attribute.getCode());

          function.add(attribute.getLocale(), new BasicColumnFunction(attribute.getTarget()));
        }
        else
        {
          this.setFunction(attribute.getCode(), new BasicColumnFunction(attribute.getTarget()));
        }
      }
    });

    if (!StringUtils.isBlank(dto.getHierarchy()))
    {
      String hCode = dto.getHierarchy();

      if (hCode.length() > 0)
      {

        ServerHierarchyType hierarchyType = ServerHierarchyType.get(hCode);
        List<ServerGeoObjectType> ancestors = this.typeService.getTypeAncestors(type, hierarchyType, true);

        this.setHierarchy(hierarchyType);

        if (ancestors.size() > 0)
        {
          this.setRoot(null);
        }
      }
    }

    List<LocationDTO> locations = dto.getLocations();

    for (int i = 0; i < locations.size(); i++)
    {
      LocationDTO location = locations.get(i);

      if (!StringUtils.isBlank(location.getTarget()) && location.getMatchStrategy() != null)
      {
        String pCode = location.getCode();

        ServerGeoObjectType pType = ServerGeoObjectType.get(pCode);
        ServerHierarchyType pHierarchy = this.typeService.findHierarchy(type, this.hierarchy, pType);

        String target = location.getTarget();

        ShapefileFunction function = location.getFunction() != null ? fromDTO(location.getFunction()) : new BasicColumnFunction(target);
        SourceAuthority authority = StringUtils.isNotBlank(location.getAuthority()) ? this.authorityService.getByCodeOrThrow(location.getAuthority()) : null;

        this.addParent(new Location(pType, pHierarchy, function, location.getMatchStrategy(), authority));
      }
    }

    // If the hierarchy is inherited, we need to resolve the hierarchy
    // inheritance chain and set them properly on the Location objects
    // To do this, we must start from the bottom and resolve upwards
    ServerHierarchyType ht = this.hierarchy;
    for (int i = this.locations.size() - 1; i >= 0; --i)
    {
      Location loc = this.locations.get(i);

      ht = this.typeService.findHierarchy(type, ht, loc.getType());
      loc.setHierarchy(ht);
    }

    dto.getIds().forEach(id -> {
      this.addIdFunction(id.getAuthority(), fromDTO(id.getFunction()));
    });

    return this;
  }

  @Override
  public void validate()
  {
    super.validate();
  }

  public static String getBaseType(String attributeType)
  {
    if (attributeType.equals(AttributeBooleanType.TYPE))
    {
      return AttributeBooleanType.TYPE;
    }
    else if (attributeType.equals(AttributeClassificationType.TYPE) || attributeType.equals(AttributeCharacterType.TYPE) || attributeType.equals(AttributeLocalType.TYPE))
    {
      return GeoObjectImportConfiguration.TEXT;
    }
    else if (attributeType.equals(AttributeFloatType.TYPE) || attributeType.equals(AttributeIntegerType.TYPE))
    {
      return GeoObjectImportConfiguration.NUMERIC;
    }

    return AttributeDateType.TYPE;
  }

  public static String getBaseType(org.opengis.feature.type.AttributeType type)
  {
    Class<?> clazz = type.getBinding();

    if (Boolean.class.isAssignableFrom(clazz))
    {
      return AttributeBooleanType.TYPE;
    }
    else if (String.class.isAssignableFrom(clazz))
    {
      return GeoObjectImportConfiguration.TEXT;
    }
    else if (Number.class.isAssignableFrom(clazz))
    {
      return GeoObjectImportConfiguration.NUMERIC;
    }
    else if (Date.class.isAssignableFrom(clazz))
    {
      return AttributeDateType.TYPE;
    }

    throw new UnsupportedOperationException("Unsupported type [" + type.getBinding().getName() + "]");
  }

  @Override
  public void enforceCreatePermissions()
  {
    if (this.getImportStrategy() == ImportStrategy.NEW_ONLY)
    {
      ServiceFactory.getGeoObjectPermissionService().enforceCanCreate(type.getOrganization().getCode(), type);
    }
    else
    {
      ServiceFactory.getGeoObjectPermissionService().enforceCanWrite(type.getOrganization().getCode(), type);
    }
  }

  @Override
  public void enforceExecutePermissions()
  {
    ServerOrganization org = type.getOrganization();

    if (this.permissions.isRA())
    {
      this.permissions.enforceRA(org.getCode());
    }
    else if (this.permissions.isRM())
    {
      this.permissions.enforceRM(org.getCode(), type);
    }
    else
    {
      this.permissions.enforceRM();
    }
  }

  @Override
  public void populate(ImportHistory history)
  {
    ServerOrganization org = type.getOrganization();

    history.setOrganization(org.getOrganization());
    history.setGeoObjectTypeCode(type.getCode());
  }
}
