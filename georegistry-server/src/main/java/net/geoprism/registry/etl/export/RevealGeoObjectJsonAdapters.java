package net.geoprism.registry.etl.export;

import java.lang.reflect.Type;
import java.util.List;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.wololo.jts2geojson.GeoJSONWriter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.geoprism.registry.AdapterUtilities;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerHierarchyType;
import net.geoprism.registry.model.ServerParentTreeNode;
import net.geoprism.registry.service.ServiceFactory;

public class RevealGeoObjectJsonAdapters
{
  public static class RevealSerializer implements JsonSerializer<GeoObject>
  {
    private ServerHierarchyType hierarchyType;
    
    private Boolean includeLevel;
    
    private ServerGeoObjectType got;
    
    private Integer depth;
    
    public RevealSerializer(ServerGeoObjectType got, ServerHierarchyType hierarchyType, Boolean includeLevel)
    {
      this.got = got;
      this.hierarchyType = hierarchyType;
      this.includeLevel = includeLevel;
      
      calculateDepth();
    }
    
    @Override
    public JsonElement serialize(GeoObject go, Type typeOfSrc, JsonSerializationContext context)
    {
      JsonObject joGO = new JsonObject();
      {
        joGO.addProperty("type", "feature");
        
        joGO.addProperty("id", go.getUid());
        
        if (go.getGeometry() != null)
        {
          GeoJSONWriter gw = new GeoJSONWriter();
          org.wololo.geojson.Geometry gJSON = gw.write(go.getGeometry());
    
          JsonObject joGeom = new JsonParser().parse(gJSON.toString()).getAsJsonObject();
          
          joGO.add("geometry", joGeom);
        }
        
        JsonObject props = new JsonObject();
        {
          props.addProperty("status", go.getStatus().getLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
          
          props.addProperty("name", go.getDisplayLabel().getValue());
          
          props.addProperty("version", 0);
          
          props.addProperty("OpenMRS_Id", 0);
          
          props.addProperty("externalId", go.getCode());
          
          props.addProperty("name_en", go.getDisplayLabel().getValue(LocalizedValue.DEFAULT_LOCALE));
          
          if (this.includeLevel)
          {
            props.addProperty("geographicLevel", this.depth);
          }
          
          props.addProperty("parentId", getParentId(go, this.hierarchyType.getCode()));
        }
        joGO.add("properties", props);
        
//        joGO.addProperty("serverVersion", 0);
      }
      return joGO;
    }
    
    public static String getParentId(GeoObject go, String hierarchyCode)
    {
      ServerGeoObjectIF serverGo = ServiceFactory.getGeoObjectService().getGeoObject(go);
      
      ServerParentTreeNode sptn = serverGo.getParentGeoObjects(null, false);
      
      List<ServerParentTreeNode> parents = sptn.getParents();
      
      for (ServerParentTreeNode parent : parents)
      {
        if (hierarchyCode == null || parent.getHierarchyType().getCode().equals(hierarchyCode))
        {
          return parent.getGeoObject().getUid();
        }
      }
      
      return "NULL";
    }
    
    public void calculateDepth()
    {
      if (!this.includeLevel) { return; }
      
      if (got.getUniversal().getParents(hierarchyType.getUniversalType()).getAll().size() > 1)
      {
        throw new UnsupportedOperationException("Multiple GeoObjectType parents not supported when 'includeLevel' is specified.");
      }
      
      List<GeoObjectType> ancestors = AdapterUtilities.getInstance().getTypeAncestors(this.got, this.hierarchyType.getCode());
      
      this.depth = ancestors.size();
    }
  }
}
