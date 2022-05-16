package net.geoprism.registry.visualization;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import net.geoprism.registry.model.BusinessObject;
import net.geoprism.registry.model.GraphType;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGraphNode;

public class EdgeView
{
  private String id;
  
  private String source;
  
  private String target;
  
  private String label;

  public EdgeView(String id, String source, String target, String label)
  {
    super();
    this.id = id;
    this.source = source;
    this.target = target;
    this.label = label;
  }
  
  public static EdgeView create(BusinessObject source, BusinessObject target)
  {
    return new EdgeView("g-" + source.getCode() + "-" + target.getCode(), "g-" + source.getCode(), "g-" + target.getCode(), "");
  }
  
  public static EdgeView create(ServerGeoObjectIF source, BusinessObject target)
  {
    return new EdgeView("g-" + source.getUid() + "-" + target.getCode(), "g-" + source.getUid(), "g-" + target.getCode(), "");
  }
  
  public static EdgeView create(BusinessObject source, ServerGeoObjectIF target)
  {
    return new EdgeView("g-" + source.getCode() + "-" + target.getUid(), "g-" + source.getCode(), "g-" + target.getUid(), "");
  }
  
  public static EdgeView create(ServerGeoObjectIF source, ServerGeoObjectIF target, GraphType graphType, ServerGraphNode node)
  {
    String label = graphType.getLabel().getValue();
    return new EdgeView("g-" + node.getOid(), "g-" + source.getUid(), "g-" + target.getUid(), label == null ? "" : label);
  }
  
  public JsonObject toJson()
  {
    GsonBuilder builder = new GsonBuilder();

    return (JsonObject) builder.create().toJsonTree(this);
  }
  
  public static EdgeView fromJSON(String sJson)
  {
    GsonBuilder builder = new GsonBuilder();

    return builder.create().fromJson(sJson, EdgeView.class);
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getSource()
  {
    return source;
  }

  public void setSource(String source)
  {
    this.source = source;
  }

  public String getTarget()
  {
    return target;
  }

  public void setTarget(String target)
  {
    this.target = target;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }
}
