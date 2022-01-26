package net.geoprism.registry.service;

import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;
import net.geoprism.registry.view.Page;

public class ClassificationService
{
  @Request(RequestType.SESSION)
  public JsonObject apply(String sessionId, String classificationCode, String parentCode, JsonObject object, boolean isNew)
  {
    ClassificationType type = ClassificationType.getByCode(classificationCode);

    Classification parent = parentCode != null ? Classification.get(type, parentCode) : null;

    Classification classification = Classification.construct(type, object, isNew);
    classification.populate(object);
    classification.apply(parent);

    // Return the refreshed copy of the geoObject
    return classification.toJSON();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String classificationCode, String code)
  {
    ClassificationType type = ClassificationType.getByCode(classificationCode);

    Classification classification = Classification.get(type, code);
    classification.delete();
  }

  @Request(RequestType.SESSION)
  public void move(String sessionId, String classificationCode, String code, String parentCode)
  {
    ClassificationType type = ClassificationType.getByCode(classificationCode);
    Classification classification = Classification.get(type, code);
    Classification newParent = Classification.get(type, parentCode);

    classification.move(newParent);
  }

  @Request(RequestType.SESSION)
  public void addChild(String sessionId, String classificationCode, String parentCode, String childCode)
  {
    ClassificationType type = ClassificationType.getByCode(classificationCode);

    Classification parent = Classification.get(type, parentCode);
    Classification child = Classification.get(type, childCode);

    parent.addChild(child);
  }

  @Request(RequestType.SESSION)
  public void removeChild(String sessionId, String classificationCode, String parentCode, String childCode)
  {
    ClassificationType type = ClassificationType.getByCode(classificationCode);

    Classification parent = Classification.get(type, parentCode);
    Classification child = Classification.get(type, childCode);

    parent.removeChild(child);
  }

  @Request(RequestType.SESSION)
  public JsonObject getChildren(String sessionId, String classificationCode, String code, Integer pageSize, Integer pageNumber)
  {
    ClassificationType type = ClassificationType.getByCode(classificationCode);

    if (code != null)
    {
      Classification parent = Classification.get(type, code);

      return parent.getChildren(pageSize, pageNumber).toJSON();
    }

    Classification root = type.getRoot();
    List<Classification> roots = new LinkedList<Classification>();

    if (root != null)
    {
      roots.add(root);
    }

    return new Page<Classification>(roots.size(), pageNumber, pageSize, roots).toJSON();
  }

}
