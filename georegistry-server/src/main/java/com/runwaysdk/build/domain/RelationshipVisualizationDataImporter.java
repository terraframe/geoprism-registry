package com.runwaysdk.build.domain;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.OrganizationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.resource.ApplicationResource;
import com.runwaysdk.resource.StreamResource;
import com.runwaysdk.session.Request;

import net.geoprism.registry.GeoRegistryUtil;
import net.geoprism.registry.Organization;
import net.geoprism.registry.conversion.OrganizationConverter;
import net.geoprism.registry.conversion.ServerGeoObjectTypeConverter;
import net.geoprism.registry.geoobject.ServerGeoObjectService;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.permission.AllowAllGeoObjectPermissionService;
import net.geoprism.registry.service.ServiceFactory;

public class RelationshipVisualizationDataImporter
{
  private static final Logger logger = LoggerFactory.getLogger(RelationshipVisualizationDataImporter.class);
  
  private Organization organization;
  
  public static final Date START_DATE;
  
  static
  {
    Calendar cal = Calendar.getInstance(GeoRegistryUtil.SYSTEM_TIMEZONE);
    cal.clear();
    cal.set(2020, Calendar.JANUARY, 1);

    START_DATE = cal.getTime();
  }
  
  public static final Date END_DATE = ValueOverTime.INFINITY_END_DATE;
  
  public static final String GOT_DOCK = "Dock";
  private ServerGeoObjectType sgotDock;
  
  public static void main(String[] args) throws Exception
  {
    new RelationshipVisualizationDataImporter().doItInReq();
  }
  
  @Request
  public void doItInReq() throws Exception
  {
    this.doIt();
  }
  
  @Transaction
  public void doIt() throws Exception
  {
    this.defineMetadata();
  }
  
  private void defineMetadata() throws Exception
  {
    this.createOrganization();

    this.createTypes();
    
    this.importInstanceData();
  }
  
  private void createOrganization()
  {
    logger.info("Creating organization.");
    
    LocalizedValue displayLabel = new LocalizedValue("United States Army Core of Engineers");
    LocalizedValue contactInfo = new LocalizedValue("USACE");
    
    OrganizationDTO dto = new OrganizationDTO("USACE", displayLabel, contactInfo);
      
    this.organization = new OrganizationConverter().create(dto);
  }
  
  private void createTypes()
  {
    logger.info("Creating types.");
    
    this.sgotDock = new ServerGeoObjectTypeConverter().create(new GeoObjectType(GOT_DOCK, GeometryType.MULTIPOINT, new LocalizedValue("Dock"), new LocalizedValue("Dock"), true, this.organization.getCode(), ServiceFactory.getAdapter()));
    this.sgotDock.createAttributeType(AttributeType.factory("locationDescription", new LocalizedValue("Location Description"), new LocalizedValue("Location Description"), AttributeCharacterType.TYPE, false, false, false));
    this.sgotDock.createAttributeType(AttributeType.factory("remarks", new LocalizedValue("Remarks"), new LocalizedValue("Remarks"), AttributeCharacterType.TYPE, false, false, false));
    this.sgotDock.createAttributeType(AttributeType.factory("commoditie", new LocalizedValue("Commoditie"), new LocalizedValue("Commoditie"), AttributeCharacterType.TYPE, false, false, false));
    this.sgotDock.createAttributeType(AttributeType.factory("purpose", new LocalizedValue("Purpose"), new LocalizedValue("Purpose"), AttributeCharacterType.TYPE, false, false, false));
    this.sgotDock.createAttributeType(AttributeType.factory("owners", new LocalizedValue("Owners"), new LocalizedValue("Owners"), AttributeCharacterType.TYPE, false, false, false));
    this.sgotDock.createAttributeType(AttributeType.factory("streetAddress", new LocalizedValue("Street Address"), new LocalizedValue("Street Address"), AttributeCharacterType.TYPE, false, false, false));
  }
  
  private void importInstanceData() throws Exception
  {
    this.importDocks();
  }
  
  private void importDocks() throws Exception
  {
    ApplicationResource resource = new StreamResource(RelationshipVisualizationDataImporter.class.getResourceAsStream("/relationship-visualization/docks.geojson"), "docks.geojson");
    
    final ServerGeoObjectService service = new ServerGeoObjectService(new AllowAllGeoObjectPermissionService());
    
    try (InputStream stream = resource.openNewStream())
    {
      JsonObject featureCollection = JsonParser.parseString(IOUtils.toString(stream, "UTF-8")).getAsJsonObject();
      
      JsonArray features = featureCollection.get("features").getAsJsonArray();
      
      logger.info("About to import [" + features.size() + "] features into GeoObjectType [" + this.sgotDock.getCode() + "].");
      
      for (int i = 0; i < features.size(); ++i)
      {
        JsonObject feature = features.get(i).getAsJsonObject();
        JsonObject sourceProps = feature.get("properties").getAsJsonObject();
        
        JsonObject geoObject = new JsonObject();
        geoObject.addProperty("type", "Feature");
        geoObject.add("geometry", convertPointToMultiPoint(feature.get("geometry").getAsJsonObject()));
        
        JsonObject targetProps = new JsonObject();
        populateTargetFromSource("ID", "code", sourceProps, targetProps, AttributeCharacterType.TYPE);
        targetProps.addProperty("type", this.sgotDock.getCode());
        targetProps.addProperty("invalid", false);
        populateTargetFromSource("LOCATION_D", "locationDescription", sourceProps, targetProps, AttributeCharacterType.TYPE);
        populateTargetFromSource("REMARKS", "remarks", sourceProps, targetProps, AttributeCharacterType.TYPE);
        populateTargetFromSource("COMMODITIE", "commoditie", sourceProps, targetProps, AttributeCharacterType.TYPE);
        populateTargetFromSource("PURPOSE", "purpose", sourceProps, targetProps, AttributeCharacterType.TYPE);
        populateTargetFromSource("OWNERS", "owners", sourceProps, targetProps, AttributeCharacterType.TYPE);
        populateTargetFromSource("STREET_ADD", "streetAddress", sourceProps, targetProps, AttributeCharacterType.TYPE);
        geoObject.add("properties", targetProps);
        
        GeoObject go = GeoObject.fromJSON(ServiceFactory.getAdapter(), geoObject.toString());
        
        go.setDisplayLabel(new LocalizedValue(readFromSource("DOCK", sourceProps)));
        
        service.apply(go, START_DATE, END_DATE, true, true);
      }
    }
  }
  
  private String readFromSource(String name, JsonObject sourceProps)
  {
    if (!sourceProps.has(name))
    {
      return null;
    }
    
    JsonElement value = sourceProps.get(name);
    
    if (value.isJsonNull())
    {
      return null;
    }
    
    return value.getAsString();
  }
  
  private void populateTargetFromSource(String sourceName, String targetName, JsonObject sourceProps, JsonObject targetProps, String type)
  {
    if (!sourceProps.has(sourceName))
    {
      targetProps.add(targetName, JsonNull.INSTANCE);
      return;
    }
    
    JsonElement value = sourceProps.get(sourceName);
    
    if (value.isJsonNull())
    {
      targetProps.add(targetName, JsonNull.INSTANCE);
      return;
    }
    
    if (type == AttributeCharacterType.TYPE)
    {
      targetProps.addProperty(targetName, value.getAsString());
    }
    else
    {
      targetProps.addProperty(targetName, value.getAsString());
    }
  }
  
  private JsonElement convertPointToMultiPoint(JsonObject geometry)
  {
    JsonObject newGeom = new JsonObject();
    
    newGeom.addProperty("type", "MultiPoint");
    
    JsonArray multiPoint = new JsonArray();
    multiPoint.add(geometry.get("coordinates").getAsJsonArray());
    newGeom.add("coordinates", multiPoint);
    
    return newGeom;
  }
}
