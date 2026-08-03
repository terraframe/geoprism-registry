package net.geoprism.registry.visualization;

import com.google.gson.JsonElement;

public class RelationshipTypeCountMetadata
{
  private final String     code;

  private final String     edgeClass;

  private final String     relationshipType;

  private final String     layout;

  private final JsonElement label;

  public RelationshipTypeCountMetadata(
      String code,
      String edgeClass,
      String relationshipType,
      String layout,
      JsonElement label
  )
  {
    this.code = code;
    this.edgeClass = edgeClass;
    this.relationshipType = relationshipType;
    this.layout = layout;
    this.label = label;
  }

  public String getCode()
  {
    return this.code;
  }

  public String getEdgeClass()
  {
    return this.edgeClass;
  }

  public String getRelationshipType()
  {
    return this.relationshipType;
  }

  public String getLayout()
  {
    return this.layout;
  }

  public JsonElement getLabel()
  {
    return this.label;
  }
}
