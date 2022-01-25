package net.geoprism.registry.service;

import java.util.stream.Collector;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;

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
  public JsonArray getChildren(String sessionId, String classificationCode, String code)
  {
    ClassificationType type = ClassificationType.getByCode(classificationCode);

    if (code != null)
    {
      Classification parent = Classification.get(type, code);

      return parent.getChildren().stream().map(child -> child.toJSON()).collect(Collector.of(() -> new JsonArray(), (r, t) -> r.add((JsonObject) t), (x1, x2) -> {
        x1.addAll(x2);
        return x1;
      }));
    }

    Classification root = type.getRoot();
    JsonArray roots = new JsonArray();

    if (root != null)
    {
      roots.add(root.toJSON());
    }

    return roots;
  }

}
