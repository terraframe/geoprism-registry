package net.geoprism.registry.model;

import com.google.gson.JsonObject;

import net.geoprism.registry.view.JsonSerializable;
import net.geoprism.registry.view.Page;

public class ClassificationNode implements JsonSerializable
{
  private Classification           classification;

  private Page<ClassificationNode> children;

  public ClassificationNode()
  {
  }

  public ClassificationNode(Classification classification)
  {
    this.classification = classification;
  }

  public void setClassification(Classification classification)
  {
    this.classification = classification;
  }

  public Classification getClassification()
  {
    return classification;
  }

  public void setChildren(Page<ClassificationNode> children)
  {
    this.children = children;
  }

  public Page<ClassificationNode> getChildren()
  {
    return children;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof ClassificationNode)
    {
      return ( (ClassificationNode) obj ).getClassification().getOid().equals(this.getClassification().getOid());
    }

    return super.equals(obj);
  }

  @Override
  public JsonObject toJSON()
  {
    JsonObject object = new JsonObject();
    object.add("classification", this.classification.toJSON());

    if (children != null)
    {

      object.add("children", this.children.toJSON());
    }

    return object;
  }
}
