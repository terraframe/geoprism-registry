/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wololo.jts2geojson.GeoJSONWriter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.runwaysdk.LocalizationFacade;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.dhis2.dhis2adapter.exception.HTTPException;
import net.geoprism.dhis2.dhis2adapter.exception.InvalidLoginException;
import net.geoprism.dhis2.dhis2adapter.exception.UnexpectedResponseException;
import net.geoprism.ontology.Classifier;
import net.geoprism.registry.AdapterUtilities;
import net.geoprism.registry.etl.DHIS2SyncConfig;
import net.geoprism.registry.etl.DHIS2TermMapping;
import net.geoprism.registry.etl.SyncLevel;
import net.geoprism.registry.etl.export.ExportRemoteException;
import net.geoprism.registry.graph.ExternalSystem;
import net.geoprism.registry.io.InvalidGeometryException;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
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

    private DHIS2ServiceIF      dhis2;

    private SyncLevel           syncLevel;
    
    private DHIS2SyncConfig     dhis2Config;
    
    public DHIS2Serializer(DHIS2ServiceIF dhis2, DHIS2SyncConfig dhis2Config, SyncLevel syncLevel, ServerGeoObjectType got, ServerHierarchyType hierarchyType, ExternalSystem ex)
    {
      this.got = got;
      this.hierarchyType = hierarchyType;
      this.dhis2 = dhis2;
      this.ex = ex;
      this.syncLevel = syncLevel;
      this.dhis2Config = dhis2Config;

      this.calculateDepth();
    }

    private synchronized String getExternalId(ServerGeoObjectIF serverGo)
    {
      String externalId = serverGo.getExternalId(this.ex);

      if (externalId == null || externalId.length() == 0)
      {
        try
        {
          externalId = this.dhis2.getDhis2Id();
        }
        catch (HTTPException | InvalidLoginException | UnexpectedResponseException e)
        {
          ExportRemoteException remoteEx = new ExportRemoteException();
          remoteEx.setRemoteError(e.getLocalizedMessage()); // TODO : Pull this
                                                            // error message
                                                            // from DHIS2
          throw remoteEx;
        }

        serverGo.createExternalId(this.ex, externalId);
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

    private JsonArray writeTranslations(VertexServerGeoObject go)
    {
      JsonArray translations = new JsonArray();
      LocalizedValue lv = go.getDisplayLabel();

      List<Locale> locales = LocalizationFacade.getInstalledLocales();
      for (Locale locale : locales)
      {
        if (lv.contains(locale) && lv.getValue(locale) != null)
        {
          JsonObject joLocaleName = new JsonObject();
          joLocaleName.addProperty("property", "NAME");
          joLocaleName.addProperty("locale", locale.toString());
          joLocaleName.addProperty("value", lv.getValue(locale));
          translations.add(joLocaleName);
          
          JsonObject joLocaleShort = new JsonObject();
          joLocaleShort.addProperty("property", "SHORT_NAME");
          joLocaleShort.addProperty("locale", locale.toString());
          joLocaleShort.addProperty("value", lv.getValue(locale));
          translations.add(joLocaleShort);
        }
      }
      
      return translations;
    }

    private void writeParents(VertexServerGeoObject serverGo, JsonObject jo)
    {
//      if (this.syncLevel.getSyncType() == SyncLevel.Type.ALL || this.syncLevel.getSyncType() == SyncLevel.Type.RELATIONSHIPS)
//      {
        ServerGeoObjectIF goParent = getParent(serverGo, this.hierarchyType.getCode());
        if (goParent != null)
        {
          JsonObject parent = new JsonObject();
          parent.addProperty("id", this.getExternalId(goParent)); // TODO : Is
                                                                  // this the
                                                                  // correct id?
          jo.add("parent", parent);
        }

        jo.addProperty("path", calculatePath(serverGo));

        jo.addProperty("level", this.depth);
//      }
    }

    private void writeAttributes(VertexServerGeoObject serverGo, JsonObject jo)
    {
//      if (this.syncLevel.getSyncType() == SyncLevel.Type.ALL || this.syncLevel.getSyncType() == SyncLevel.Type.ORG_UNITS)
//      {
        jo.addProperty("code", serverGo.getCode());

        jo.addProperty("id", this.getExternalId(serverGo));

        jo.addProperty("created", formatDate(serverGo.getCreateDate()));

        jo.addProperty("lastUpdated", formatDate(serverGo.getLastUpdateDate()));
        
        jo.addProperty("name", serverGo.getDisplayLabel().getValue(LocalizedValue.DEFAULT_LOCALE));

        jo.addProperty("shortName", serverGo.getDisplayLabel().getValue(LocalizedValue.DEFAULT_LOCALE));

        jo.addProperty("openingDate", formatDate(serverGo.getCreateDate())); // TODO : Correct value?
        
        writeGeometry(jo, serverGo);

        jo.add("translations", writeTranslations(serverGo));
        
        this.writeCustomAttributes(serverGo, jo);
//      }
    }
    
    private void writeCustomAttributes(VertexServerGeoObject serverGo, JsonObject jo)
    {
      final String lastUpdateDate = formatDate(serverGo.getLastUpdateDate());
      
      final String createDate = formatDate(serverGo.getCreateDate());
      
      JsonArray attributeValues = new JsonArray();
      
      Map<String, AttributeType> attrs = this.got.getAttributeMap();
      
      for (AttributeType attr : attrs.values())
      {
        if (!attr.getIsDefault() && this.syncLevel.hasAttribute(attr.getName()))
        {
          JsonObject av = new JsonObject();
          
          av.addProperty("lastUpdated", lastUpdateDate);
          
          av.addProperty("created", createDate);
          
          if (attr instanceof AttributeBooleanType)
          {
            av.addProperty("value", (Boolean) serverGo.getValue(attr.getName()));
          }
          else if (attr instanceof AttributeIntegerType)
          {
            av.addProperty("value", (Long) serverGo.getValue(attr.getName()));
          }
          else if (attr instanceof AttributeFloatType)
          {
            av.addProperty("value", (Double) serverGo.getValue(attr.getName()));
          }
          else if (attr instanceof AttributeDateType)
          {
            av.addProperty("value", formatDate((Date) serverGo.getValue(attr.getName())));
          }
          else if (attr instanceof AttributeTermType)
          {
            Classifier classy = (Classifier) serverGo.getValue(attr.getName());
            
            DHIS2TermMapping mapping = this.dhis2Config.getTermMapping(classy.getOid());
            
            if (mapping == null)
            {
              MissingDHIS2TermMapping ex = new MissingDHIS2TermMapping();
              ex.setTermCode(classy.getClassifierId());
              throw ex;
            }
            
            av.addProperty("value", mapping.getExternalId());
          }
          else
          {
            av.addProperty("value", String.valueOf(serverGo.getValue(attr.getName())));
          }
          
          JsonObject joAttr = new JsonObject();
          joAttr.addProperty("id", this.syncLevel.getAttribute(attr.getName()).getExternalId());
          av.add("attribute", joAttr);
          
          attributeValues.add(av);
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
          
          jo.addProperty("featureType", convertGeometryType(joGeom.get("type").getAsString()));
          
//          jo.add("coordinates", joGeom.get("coordinates").getAsJsonArray());
          jo.addProperty("coordinates", joGeom.get("coordinates").toString());
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
      // Cannot deserialize value of type `org.hisp.dhis.organisationunit.FeatureType` from String "MultiPolygon": value not one of declared Enum instance names: [SYMBOL, POLYGON, MULTI_POLYGON, NONE, POINT] at [Source: (org.apache.catalina.connector.CoyoteInputStream); line: 1, column: 204] (through reference chain: org.hisp.dhis.organisationunit.OrganisationUnit["featureType"])
      
      String out = geometryType.toUpperCase();
      
      if (out.equals("MULTIPOLYGON"))
      {
        return "MULTI_POLYGON";
      }
      
      return out;
    }

    public static ServerGeoObjectIF getParent(VertexServerGeoObject serverGo, String hierarchyCode)
    {
      ServerParentTreeNode sptn = serverGo.getParentGeoObjects(null, false);

      List<ServerParentTreeNode> parents = sptn.getParents();

      for (ServerParentTreeNode parent : parents)
      {
        if (hierarchyCode == null || parent.getHierarchyType().getCode().equals(hierarchyCode))
        {
          return parent.getGeoObject();
        }
      }

      return null;
    }

    public static String formatDate(Date date)
    {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
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

    public String calculatePath(VertexServerGeoObject serverGo)
    {
      List<String> ancestorExternalIds = new ArrayList<String>();

      List<VertexServerGeoObject> ancestors = serverGo.getAncestors(this.hierarchyType);

      Collections.reverse(ancestors);

//      ancestors.forEach(ancestor -> {
//        ancestorExternalIds.add(this.getExternalId(ancestor));
//      }); 
      
      for (VertexServerGeoObject ancestor : ancestors)
      {
        ancestorExternalIds.add(this.getExternalId(ancestor));
      }

      return "/" + StringUtils.join(ancestorExternalIds, "/");
    }

    public void calculateDepth()
    {
      if (got.getUniversal().getParents(hierarchyType.getUniversalType()).getAll().size() > 1)
      {
        throw new UnsupportedOperationException("Multiple GeoObjectType parents not supported.");
      }

      List<GeoObjectType> ancestors = AdapterUtilities.getInstance().getTypeAncestors(this.got, this.hierarchyType.getCode());

      this.depth = ancestors.size() + 1;
    }
  }
}
