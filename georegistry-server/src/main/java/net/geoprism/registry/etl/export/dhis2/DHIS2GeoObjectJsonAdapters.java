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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wololo.jts2geojson.GeoJSONWriter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.localization.LocalizationFacade;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.dhis2.dhis2adapter.DHIS2Constants;
import net.geoprism.dhis2.dhis2adapter.exception.BadServerUriException;
import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.exception.UnexpectedResponseException;
import net.geoprism.dhis2.dhis2adapter.response.model.DHIS2Locale;
import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.etl.DHIS2AttributeMapping;
import net.geoprism.registry.etl.DHIS2SyncConfig;
import net.geoprism.registry.etl.DHIS2SyncLevel;
import net.geoprism.registry.etl.export.ExportRemoteException;
import net.geoprism.registry.etl.upload.ImportConfiguration.ImportStrategy;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.io.InvalidGeometryException;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;

public class DHIS2GeoObjectJsonAdapters
{
  public static class DHIS2Serializer implements JsonSerializer<VertexServerGeoObject>
  {
    private static final Logger logger = LoggerFactory.getLogger(DHIS2Serializer.class);

    private ServerHierarchyType hierarchyType;

    private ServerGeoObjectType got;

    private Integer             depth;

    private ExternalSystem      ex;

    private DHIS2TransportServiceIF      dhis2;

    private DHIS2SyncLevel           syncLevel;
    
    private SortedSet<DHIS2SyncLevel> levels;

    private DHIS2SyncConfig     dhis2Config;
    
    private List<DHIS2Locale>   dhis2Locales;
    
    public DHIS2Serializer(DHIS2TransportServiceIF dhis2, DHIS2SyncConfig dhis2Config, DHIS2SyncLevel syncLevel, List<DHIS2Locale> dhis2Locales)
    {
      this.got = syncLevel.getGeoObjectType();
      this.hierarchyType = dhis2Config.getHierarchy();
      this.dhis2 = dhis2;
      this.ex = dhis2Config.getSystem();
      this.syncLevel = syncLevel;
      this.levels = dhis2Config.getLevels();
      this.dhis2Config = dhis2Config;
      this.dhis2Locales = dhis2Locales;

      this.calculateDepth();
    }
    
    private String getExternalId(ServerGeoObjectIF serverGo)
    {
      String externalId = serverGo.getExternalId(this.ex);
      
      if (externalId == null)
      {
        ParentExternalIdException ex = new ParentExternalIdException();
        ex.setParentLabel(serverGo.getCode());
        throw ex;
      }
      
      return externalId;
    }

    private synchronized String getOrCreateExternalId(ServerGeoObjectIF serverGo)
    {
      String externalId = serverGo.getExternalId(this.ex);

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

        serverGo.createExternalId(this.ex, externalId, ImportStrategy.NEW_ONLY);
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
    
    /**
     * This algorithm attempts to match the locales in the CGR to their best fit within DHIS2.
     * 
     * Best fit in this context means, in order of priority:
     * 1. Both the language and country match exactly (either as specified or as omitted). eg. en_US -> en_US or en -> en
     * 2. The CGR language matches a language in DHIS2, where the CGR locale has a country but the DHIS2 locale does not: eg. en_US -> en
     * 3. The CGR language matches a language in DHIS2, but the countries do not match. If there are more than one locale which fits this criteria one will be chosen at random. eg. en_US -> en_UK
     * 4. The CGR language matches a language in DHIS2, but the CGR locale does not have a country and the only matching language has a country. eg. en -> en_UK
     * 5. The locale could not be matched and will not be exported.
     */
    private Map<String, DHIS2Locale> mapLocales(LocalizedValue lv)
    {
      Map<String, DHIS2Locale> map = new HashMap<String, DHIS2Locale>();
      
      try
      {
        Set<Locale> locales = LocalizationFacade.getInstalledLocales();
        
        for (Locale locale : locales)
        {
          if (lv.contains(locale) && lv.getValue(locale) != null)
          {
            for (DHIS2Locale dhis2Locale : this.dhis2Locales)
            {
              Locale localeDhis2 = LocaleUtils.toLocale(dhis2Locale.getLocale());
              
              if (localeDhis2.getLanguage().equals(locale.getLanguage()))
              {
                if (map.containsKey(locale.toString()))
                {
                  String loopDhis2Country = localeDhis2.getCountry();
                  String existingDhis2Country = LocaleUtils.toLocale(map.get(locale.toString()).getLocale()).getCountry();
                  String cgrCountry = locale.getCountry();
                  
                  boolean existingMatchesCountry = ((existingDhis2Country != null && cgrCountry != null) && existingDhis2Country.equals(cgrCountry)) || (existingDhis2Country == null && cgrCountry == null);
                  boolean loopMatchesCountry = ((loopDhis2Country != null && cgrCountry != null) && loopDhis2Country.equals(cgrCountry)) || (loopDhis2Country == null && cgrCountry == null);
                  boolean coalesceIntoGeneric = (loopDhis2Country == null || loopDhis2Country.length() == 0) && cgrCountry != null && cgrCountry.length() > 0;
                  
                  if (!existingMatchesCountry && (loopMatchesCountry || coalesceIntoGeneric))
                  {
                    map.put(locale.toString(), dhis2Locale);
                  }
                }
                else
                {
                  map.put(locale.toString(), dhis2Locale);
                }
              }
            }
          }
        }
      }
      catch (Throwable t)
      {
        logger.error("Error occurred while mapping cgr locales to dhis2 locales", t);
      }
      
      return map;
    }

    private JsonArray writeTranslations(VertexServerGeoObject go)
    {
      JsonArray translations = new JsonArray();
      LocalizedValue lv = go.getDisplayLabel();
      
      Map<String, DHIS2Locale> mappings = this.mapLocales(lv);

      Set<Locale> locales = LocalizationFacade.getInstalledLocales();
      for (Locale locale : locales)
      {
        if (lv.contains(locale) && lv.getValue(locale) != null)
        {
          String localestring = locale.toString();
          if (mappings != null && mappings.containsKey(locale.toString()))
          {
            DHIS2Locale dhis2Locale = mappings.get(locale.toString());
            localestring = dhis2Locale.getLocale();
          }
          
          JsonObject joLocaleName = new JsonObject();
          joLocaleName.addProperty("property", "NAME");
          joLocaleName.addProperty("locale", localestring);
          joLocaleName.addProperty("value", lv.getValue(locale));
          translations.add(joLocaleName);

          JsonObject joLocaleShort = new JsonObject();
          joLocaleShort.addProperty("property", "SHORT_NAME");
          joLocaleShort.addProperty("locale", localestring);
          joLocaleShort.addProperty("value", lv.getValue(locale));
          translations.add(joLocaleShort);
        }
      }

      return translations;
    }

//    private void writeParents(VertexServerGeoObject serverGo, JsonObject jo)
//    {
//      if (this.depth > 1)
//      {
//        ServerGeoObjectIF goParent = getParent(serverGo, this.hierarchyType.getCode());
//        
//        if (goParent == null)
//        {
//          
//        }
//        
//        String parentId = this.getExternalId(goParent);
//        
//        if (parentId != null)
//        {
//          
//        }
//        
//        JsonObject parent = new JsonObject();
//        parent.addProperty("id", parentId);
//        jo.add("parent", parent);
//        
//        if (!jo.has("parent") && this.depth > 1)
//        {
//          UnknownParentException ex = new UnknownParentException();
//          ex.setParentLabel(goParent != null ? goParent.getCode() : serverGo.getCode());
//          throw ex;
//        }
//  
//        jo.addProperty("path", calculatePath(serverGo));
//      }
//      else
//      {
//        jo.addProperty("path", "/");
//      }
//
//      jo.addProperty("level", this.depth);
//    }

    private void writeAttributes(VertexServerGeoObject serverGo, JsonObject jo)
    {
      // Give our attribute mappings a chance to fill out any standard attributes
      if (this.syncLevel.getMappings() != null)
      {
        for (DHIS2AttributeMapping mapping : this.syncLevel.getMappings())
        {
          mapping.writeStandardAttributes(serverGo, this.dhis2Config.getDate(), jo, this.dhis2Config, this.syncLevel);
        }
      }
      
      // Fill in any required attributes with some sensible defaults
      // TODO : Attribute mapping should be optional
//      if (!jo.has("code"))
//      {
//        jo.addProperty("code", serverGo.getCode());
//      }

      if (!jo.has("id"))
      {
        jo.addProperty("id", this.getOrCreateExternalId(serverGo));
      }

//      if (!jo.has("created"))
//      {
//        jo.addProperty("created", formatDate(serverGo.getCreateDate()));
//      }

//      if (!jo.has("lastUpdated"))
//      {
//        jo.addProperty("lastUpdated", formatDate(serverGo.getLastUpdateDate()));
//      }

//      if (!jo.has("name"))
//      {
//        jo.addProperty("name", serverGo.getDisplayLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
//      }
//
//      if (!jo.has("shortName"))
//      {
//        jo.addProperty("shortName", serverGo.getDisplayLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
//      }

//      if (!jo.has("openingDate"))
//      {
//        jo.addProperty("openingDate", formatDate(serverGo.getCreateDate()));
//      }

      if (!jo.has("featureType") || !jo.has("coordinates"))
      {
        writeGeometry(jo, serverGo);
      }

      if (!jo.has("translations"))
      {
        jo.add("translations", writeTranslations(serverGo));
      }

      this.writeCustomAttributes(serverGo, jo);
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
      Geometry geom = serverGo.getGeometry();

      if (geom != null)
      {
        try
        {
          GeoJSONWriter gw = new GeoJSONWriter();
          org.wololo.geojson.Geometry gJSON = gw.write(geom);

          JsonObject joGeom = JsonParser.parseString(gJSON.toString()).getAsJsonObject();
          
          if (this.dhis2.getVersionRemoteServerApi() < 32)
          {
            jo.addProperty("featureType", convertGeometryType(joGeom.get("type").getAsString()));
  
            jo.addProperty("coordinates", joGeom.get("coordinates").toString());
          }
          else
          {
            // Use geometry column for org unit and org unit group [DHIS2-5597] (#2870)
            // https://github.com/dhis2/dhis2-core/commit/0b40d73efbe5252dcc7ac3e20393e6b922d7a215
            
            jo.add("geometry", joGeom);
          }
        }
        catch (Throwable t)
        {
          logger.error("Encountered an unexpected error when serializing geometry for GeoObject with code [" + serverGo.getCode() + "] and typeCode [" + serverGo.getType().getCode() + "].", t);
          throw new InvalidGeometryException(t);
        }
      }
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

//    public static ServerGeoObjectIF getParent(VertexServerGeoObject serverGo, String hierarchyCode)
//    {
//      ServerParentTreeNode sptn = serverGo.getParentGeoObjects(null, false); // TODO : What if we don't have parents (at this date + hierarchy)?. Or what if the parents we have doesn't match the depth we expected?
//
//      List<ServerParentTreeNode> parents = sptn.getParents();
//
//      for (ServerParentTreeNode parent : parents)
//      {
//        if (hierarchyCode == null || parent.getHierarchyType().getCode().equals(hierarchyCode))
//        {
//          return parent.getGeoObject();
//        }
//      }
//
//      return null;
//    }

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

      List<VertexServerGeoObject> ancestors = serverGo.getAncestors(this.hierarchyType, this.dhis2Config.getSyncNonExistent());

      Collections.reverse(ancestors);
      
      int parentLevel = this.syncLevel.getLevel() - 1;

      ParentLoop:
      while (parentLevel >= 0)
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
        ex.setSyncLevel(String.valueOf(parentSyncLevel.getLevel()+1));
        ex.setTypeCode(parentSyncLevel.getGeoObjectType().getCode());
        ex.setHierarchyCode(this.hierarchyType.getCode());
        ex.setDateLabel(GeoRegistryUtil.formatIso8601(this.dhis2Config.getDate(), false));
        throw ex;
      }
      
      if (directParentId == null)
      {
        DHIS2SyncLevel parentSyncLevel = this.getLevelAtIndex(this.syncLevel.getLevel() - 1);
        
        NoParentException ex = new NoParentException();
        ex.setSyncLevel(String.valueOf(parentSyncLevel.getLevel()+1));
        ex.setTypeCode(parentSyncLevel.getGeoObjectType().getCode());
        ex.setHierarchyCode(this.hierarchyType.getCode());
        ex.setDateLabel(GeoRegistryUtil.formatIso8601(this.dhis2Config.getDate(), false));
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
      
//      if (got.getUniversal().getParents(hierarchyType.getUniversalType()).getAll().size() > 1)
//      {
//        throw new UnsupportedOperationException("Multiple GeoObjectType parents not supported.");
//      }
//
//      List<GeoObjectType> ancestors = this.got.getTypeAncestors(this.hierarchyType, true);
//
//      this.depth = ancestors.size() + 1;
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
