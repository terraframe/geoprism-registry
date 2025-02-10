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
package net.geoprism.registry.etl.export.dhis2;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.localization.LocalizationFacade;

import net.geoprism.dhis2.dhis2adapter.DHIS2Constants;
import net.geoprism.dhis2.dhis2adapter.exception.BadServerUriException;
import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.exception.UnexpectedResponseException;
import net.geoprism.dhis2.dhis2adapter.response.model.DHIS2Locale;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.cache.GeoObjectCache;
import net.geoprism.registry.etl.DHIS2AttributeMapping;
import net.geoprism.registry.etl.DHIS2SyncConfig;
import net.geoprism.registry.etl.DHIS2SyncLevel;
import net.geoprism.registry.etl.export.ExportRemoteException;
import net.geoprism.registry.etl.export.UnsupportedGeometryException;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.graph.InheritedHierarchyAnnotation;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.business.GPRGeoObjectBusinessServiceIF;
import net.geoprism.registry.service.business.GeoObjectTypeBusinessServiceIF;
import net.geoprism.registry.service.business.ServiceFactory;

public class DHIS2GeoObjectJsonAdapters
{
  public static class DHIS2Serializer implements JsonSerializer<VertexServerGeoObject>
  {
    private static final Logger            logger = LoggerFactory.getLogger(DHIS2Serializer.class);

    private ServerHierarchyType            hierarchyType;

    private Integer                        depth;

    private ExternalSystem                 ex;

    private DHIS2TransportServiceIF        dhis2;

    private DHIS2SyncLevel                 syncLevel;

    private SortedSet<DHIS2SyncLevel>      levels;

    private DHIS2SyncConfig                dhis2Config;

    private List<DHIS2Locale>              dhis2Locales;

    // Links Geo-Object reference (typeCode + SEPARATOR + goUid) -> externalId
    private BidiMap<String, String>        newExternalIds;

    private GeoObjectTypeBusinessServiceIF typeService;

    private GPRGeoObjectBusinessServiceIF  objectService;

    public DHIS2Serializer(DHIS2TransportServiceIF dhis2, DHIS2SyncConfig dhis2Config, DHIS2SyncLevel syncLevel, List<DHIS2Locale> dhis2Locales, BidiMap<String, String> newExternalIds)
    {
      this.typeService = ServiceFactory.getBean(GeoObjectTypeBusinessServiceIF.class);
      this.objectService = ServiceFactory.getBean(GPRGeoObjectBusinessServiceIF.class);

      this.hierarchyType = dhis2Config.getHierarchy();
      this.dhis2 = dhis2;
      this.ex = dhis2Config.getSystem();
      this.syncLevel = syncLevel;
      this.levels = dhis2Config.getLevels();
      this.dhis2Config = dhis2Config;
      this.dhis2Locales = dhis2Locales;
      this.newExternalIds = newExternalIds;

      this.calculateDepth();
    }

    private String getExternalId(ServerGeoObjectIF serverGo)
    {
      String goRef = serverGo.getType().getCode() + GeoObjectCache.SEPARATOR + serverGo.getUid();

      if (this.newExternalIds.containsKey(goRef))
      {
        return this.newExternalIds.get(goRef);
      }
      else
      {
        String externalId = this.objectService.getExternalId(serverGo, ex);

        if (externalId == null)
        {
          ParentExternalIdException ex = new ParentExternalIdException();
          ex.setParentLabel(serverGo.getCode());
          throw ex;
        }

        return externalId;
      }
    }

    private synchronized String getOrCreateExternalId(ServerGeoObjectIF serverGo)
    {
      String externalId = this.objectService.getExternalId(serverGo, this.ex);

      if (externalId == null || externalId.length() == 0)
      {
        try
        {
          externalId = this.dhis2.getDhis2Id();
        }
        catch (HTTPException | InvalidLoginException | UnexpectedResponseException | BadServerUriException e)
        {
          ExportRemoteException remoteEx = new ExportRemoteException();
          remoteEx.setRemoteError(e.getLocalizedMessage()); // TODO : Pull this
                                                            // error message
                                                            // from DHIS2
          throw remoteEx;
        }

        String goRef = serverGo.getType().getCode() + GeoObjectCache.SEPARATOR + serverGo.getUid();
        this.newExternalIds.put(goRef, externalId);
      }

      return externalId;
    }

    @Override
    public JsonElement serialize(VertexServerGeoObject serverGo, Type typeOfSrc, JsonSerializationContext context)
    {
      JsonObject jo = new JsonObject();

      writeAttributes(serverGo, jo);

      writeParents(serverGo, jo);

      return jo;
    }

    private JsonArray writeTranslations(VertexServerGeoObject serverGo)
    {
      JsonArray translations = new JsonArray();

      if (this.syncLevel.getMappings() != null)
      {
        for (DHIS2AttributeMapping mapping : this.syncLevel.getMappings())
        {
          mapping.writeTranslations(serverGo, this.dhis2Config.getDate(), translations, this.dhis2Config, this.syncLevel, this.dhis2Locales);
        }
      }

      return translations;
    }

    // private void writeParents(VertexServerGeoObject serverGo, JsonObject jo)
    // {
    // if (this.depth > 1)
    // {
    // ServerGeoObjectIF goParent = getParent(serverGo,
    // this.hierarchyType.getCode());
    //
    // if (goParent == null)
    // {
    //
    // }
    //
    // String parentId = this.getExternalId(goParent);
    //
    // if (parentId != null)
    // {
    //
    // }
    //
    // JsonObject parent = new JsonObject();
    // parent.addProperty("id", parentId);
    // jo.add("parent", parent);
    //
    // if (!jo.has("parent") && this.depth > 1)
    // {
    // UnknownParentException ex = new UnknownParentException();
    // ex.setParentLabel(goParent != null ? goParent.getCode() :
    // serverGo.getCode());
    // throw ex;
    // }
    //
    // jo.addProperty("path", calculatePath(serverGo));
    // }
    // else
    // {
    // jo.addProperty("path", "/");
    // }
    //
    // jo.addProperty("level", this.depth);
    // }

    private void writeAttributes(VertexServerGeoObject serverGo, JsonObject jo)
    {
      // Give our attribute mappings a chance to fill out any standard
      // attributes
      if (this.syncLevel.getMappings() != null)
      {
        for (DHIS2AttributeMapping mapping : this.syncLevel.getMappings())
        {
          mapping.writeStandardAttributes(serverGo, this.dhis2Config.getDate(), jo, this.dhis2Config, this.syncLevel);
        }
      }

      if (!jo.has("id"))
      {
        jo.addProperty("id", this.getOrCreateExternalId(serverGo));
      }

      if (!jo.has("featureType") || !jo.has("coordinates"))
      {
        writeGeometry(jo, serverGo);
      }

      if (!jo.has("translations"))
      {
        jo.add("translations", writeTranslations(serverGo));
      }

      this.writeCustomAttributes(serverGo, jo);

      this.enforceRequiredAttributes(jo);
    }

    private void enforceRequiredAttributes(JsonObject jo)
    {
      if (DHIS2SyncLevel.Type.RELATIONSHIPS.equals(this.syncLevel.getSyncType()) || DHIS2SyncLevel.Type.NONE.equals(this.syncLevel.getSyncType()))
      {
        return;
      }

      final String[] attributes = new String[] { "name", "shortName", "openingDate" };

      List<String> labels = new ArrayList<String>();

      for (String attribute : attributes)
      {
        if (!jo.has(attribute) || jo.get(attribute).isJsonNull() || StringUtils.isEmpty(jo.get(attribute).getAsString()))
        {
          labels.add(attribute);
        }
      }

      if (labels.size() > 0)
      {
        RequiredValueException ex = new RequiredValueException();
        ex.setDhis2AttrLabels(StringUtils.join(labels, ", "));
        ex.setDateLabel(GeoRegistryUtil.formatDateForPresentation(this.dhis2Config.getDate(), false));
        throw ex;
      }
    }

    private void writeCustomAttributes(VertexServerGeoObject serverGo, JsonObject jo)
    {
      final String lastUpdateDate = formatDate(serverGo.getLastUpdateDate());

      final String createDate = formatDate(serverGo.getCreateDate());

      JsonArray attributeValues = new JsonArray();

      if (this.syncLevel.getMappings() != null)
      {
        for (DHIS2AttributeMapping mapping : this.syncLevel.getMappings())
        {
          mapping.writeCustomAttributes(attributeValues, serverGo, this.dhis2Config.getDate(), this.dhis2Config, this.syncLevel, lastUpdateDate, createDate);
        }
      }

      jo.add("attributeValues", attributeValues);
    }

    private void writeGeometry(JsonObject jo, VertexServerGeoObject serverGo)
    {
      final Geometry geom = serverGo.getGeometry();

      if (geom != null)
      {
        try
        {
          final Geometry converted = convertGeometry(geom);

          GeoJsonWriter gw = new GeoJsonWriter();
          String json = gw.write(converted);

          JsonObject joGeom = JsonParser.parseString(json).getAsJsonObject();

          if (this.dhis2.getVersionRemoteServerApi() < 32)
          {
            jo.addProperty("featureType", convertGeometryType(joGeom.get("type").getAsString()));

            jo.addProperty("coordinates", joGeom.get("coordinates").toString());
          }
          else
          {
            // Use geometry column for org unit and org unit group [DHIS2-5597]
            // (#2870)
            // https://github.com/dhis2/dhis2-core/commit/0b40d73efbe5252dcc7ac3e20393e6b922d7a215

            jo.add("geometry", joGeom);
          }
        }
        catch (UnsupportedGeometryException e)
        {
          throw e;
        }
        catch (Throwable t)
        {
          logger.error("Encountered an unexpected error when serializing geometry for GeoObject with code [" + serverGo.getCode() + "] and typeCode [" + serverGo.getType().getCode() + "].", t);
          throw new net.geoprism.registry.io.InvalidGeometryException(t);
        }
      }
    }

    /**
     * DHIS2 does not support Multi-Point geometries. If we encounter one, we
     * must convert it to point if we can, otherwise throw an exception.
     * 
     * @refs https://github.com/dhis2/dhis2-core/blob/f8ea5ba19b51f419eeac9e95f1f1f2ed7ace0ce6/[…]i/src/main/java/org/hisp/dhis/organisationunit/FeatureType.java
     */
    private Geometry convertGeometry(Geometry in)
    {
      final String[] unsupported = new String[] { Geometry.TYPENAME_LINEARRING.toLowerCase(), Geometry.TYPENAME_LINESTRING.toLowerCase(), Geometry.TYPENAME_MULTILINESTRING.toLowerCase(), Geometry.TYPENAME_GEOMETRYCOLLECTION.toLowerCase() };

      // Throw an exception if the type is unsupported
      if (ArrayUtils.contains(unsupported, in.getGeometryType().toLowerCase()))
      {
        final String geometryType = Geometry.TYPENAME_GEOMETRYCOLLECTION.equalsIgnoreCase(in.getGeometryType()) ? Geometry.TYPENAME_GEOMETRYCOLLECTION : LocalizationFacade.localize("georegistry.geometry.line");

        UnsupportedGeometryException ex = new UnsupportedGeometryException();
        ex.setGeometryType(geometryType);
        throw ex;
      }

      // Try to unwrap multi-points
      if (in.getGeometryType().equals(Geometry.TYPENAME_MULTIPOINT))
      {
        Coordinate[] coordinates = in.getCoordinates();

        if (coordinates.length > 1)
        {
          UnsupportedGeometryException ex = new UnsupportedGeometryException();
          ex.setGeometryType(LocalizationFacade.localize("georegistry.geometry.multipoint"));
          throw ex;
        }
        else if (coordinates.length == 1)
        {
          return GeometryFactory.createPointFromInternalCoord(coordinates[0], in);
        }
      }

      return in;
    }

    private String convertGeometryType(String geometryType)
    {
      // Cannot deserialize value of type
      // `org.hisp.dhis.organisationunit.FeatureType` from String
      // "MultiPolygon": value not one of declared Enum instance names: [SYMBOL,
      // POLYGON, MULTI_POLYGON, NONE, POINT] at [Source:
      // (org.apache.catalina.connector.CoyoteInputStream); line: 1, column:
      // 204] (through reference chain:
      // org.hisp.dhis.organisationunit.OrganisationUnit["featureType"])

      String out = geometryType.toUpperCase();

      if (out.equals("MULTIPOLYGON"))
      {
        return "MULTI_POLYGON";
      }

      return out;
    }

    // public static ServerGeoObjectIF getParent(VertexServerGeoObject serverGo,
    // String hierarchyCode)
    // {
    // ServerParentTreeNode sptn = serverGo.getParentGeoObjects(null, false); //
    // TODO : What if we don't have parents (at this date + hierarchy)?. Or what
    // if the parents we have doesn't match the depth we expected?
    //
    // List<ServerParentTreeNode> parents = sptn.getParents();
    //
    // for (ServerParentTreeNode parent : parents)
    // {
    // if (hierarchyCode == null ||
    // parent.getHierarchyType().getCode().equals(hierarchyCode))
    // {
    // return parent.getGeoObject();
    // }
    // }
    //
    // return null;
    // }

    public static String formatDate(Date date)
    {
      SimpleDateFormat format = new SimpleDateFormat(DHIS2Constants.DATE_FORMAT);
      format.setTimeZone(TimeZone.getTimeZone("UTC"));

      if (date != null)
      {
        return format.format(date);
      }
      else
      {
        return null;
      }
    }

    public void writeParents(VertexServerGeoObject serverGo, JsonObject jo)
    {
      if (this.syncLevel.getLevel() == 0)
      {
        jo.addProperty("path", "/");
        jo.addProperty("level", this.depth);
        return;
      }

      String directParentId = null;

      List<String> ancestorExternalIds = new ArrayList<String>();

      List<VertexServerGeoObject> ancestors = new ArrayList<VertexServerGeoObject>();

      if (this.typeService.isRoot(serverGo.getType(), this.hierarchyType))
      {
        InheritedHierarchyAnnotation anno = InheritedHierarchyAnnotation.get(serverGo.getType(), this.hierarchyType);

        if (anno != null)
        {
          ServerHierarchyType shtInherited = ServerHierarchyType.get(anno.getInheritedHierarchyCode());

          ancestors = this.objectService.getAncestors(serverGo, shtInherited, this.dhis2Config.getSyncNonExistent(), true);
        }
      }
      else
      {
        ancestors = this.objectService.getAncestors(serverGo, this.hierarchyType, this.dhis2Config.getSyncNonExistent(), true);
      }

      Collections.reverse(ancestors);

      int parentLevel = this.syncLevel.getLevel() - 1;

      ParentLoop: while (parentLevel >= 0)
      {
        DHIS2SyncLevel parentSyncLevel = this.getLevelAtIndex(parentLevel);

        for (VertexServerGeoObject ancestor : ancestors)
        {
          if (parentSyncLevel.getGeoObjectType().equals(ancestor.getType()))
          {
            String externalId = this.getExternalId(ancestor);

            ancestorExternalIds.add(externalId);

            if (parentLevel == this.syncLevel.getLevel() - 1)
            {
              directParentId = externalId;
            }

            parentLevel--;
            continue ParentLoop;
          }
        }

        NoParentException ex = new NoParentException();
        ex.setSyncLevel(String.valueOf(parentSyncLevel.getLevel() + 1));
        ex.setTypeCode(parentSyncLevel.getGeoObjectType().getCode());
        ex.setHierarchyCode(this.hierarchyType.getCode());
        ex.setDateLabel(GeoRegistryUtil.formatDateForPresentation(this.dhis2Config.getDate(), false));
        throw ex;
      }

      if (directParentId == null)
      {
        DHIS2SyncLevel parentSyncLevel = this.getLevelAtIndex(this.syncLevel.getLevel() - 1);

        NoParentException ex = new NoParentException();
        ex.setSyncLevel(String.valueOf(parentSyncLevel.getLevel() + 1));
        ex.setTypeCode(parentSyncLevel.getGeoObjectType().getCode());
        ex.setHierarchyCode(this.hierarchyType.getCode());
        ex.setDateLabel(GeoRegistryUtil.formatDateForPresentation(this.dhis2Config.getDate(), false));
        throw ex;
      }

      JsonObject parent = new JsonObject();
      parent.addProperty("id", directParentId);
      jo.add("parent", parent);

      String path = "/" + StringUtils.join(ancestorExternalIds, "/");
      jo.addProperty("path", path);

      jo.addProperty("level", this.depth);
    }

    public void calculateDepth()
    {
      this.depth = this.syncLevel.getLevel();

      // if
      // (got.getUniversal().getParents(hierarchyType.getUniversalType()).getAll().size()
      // > 1)
      // {
      // throw new UnsupportedOperationException("Multiple GeoObjectType parents
      // not supported.");
      // }
      //
      // List<GeoObjectType> ancestors =
      // this.got.getTypeAncestors(this.hierarchyType, true);
      //
      // this.depth = ancestors.size() + 1;
    }

    private DHIS2SyncLevel getLevelAtIndex(int i)
    {
      int j = 0;

      for (DHIS2SyncLevel level : this.levels)
      {
        if (j == i)
        {
          return level;
        }

        j++;
      }

      throw new ProgrammingErrorException("Unable to find sync level at index [" + i + "].");
    }
  }
}
