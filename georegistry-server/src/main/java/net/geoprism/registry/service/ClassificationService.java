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
  public JsonObject apply(String sessionId, String classificationType, String parentCode, JsonObject object, boolean isNew)
  {
    ClassificationType type = ClassificationType.getByType(classificationType);

    Classification parent = parentCode != null ? Classification.get(type, parentCode) : null;

    Classification classification = Classification.construct(type, object, isNew);
    classification.populate(object);
    classification.apply(parent);

    // Return the refreshed copy of the geoObject
    return classification.toJSON();
  }

  @Request(RequestType.SESSION)
  public void remove(String sessionId, String classificationType, String code)
  {
    ClassificationType type = ClassificationType.getByType(classificationType);

    Classification classification = Classification.get(type, code);
    classification.delete();
  }

  @Request(RequestType.SESSION)
  public void addChild(String sessionId, String classificationType, String parentCode, String childCode)
  {
    ClassificationType type = ClassificationType.getByType(classificationType);

    Classification parent = Classification.get(type, parentCode);
    Classification child = Classification.get(type, childCode);

    parent.addChild(child);
  }

  @Request(RequestType.SESSION)
  public void removeChild(String sessionId, String classificationType, String parentCode, String childCode)
  {
    ClassificationType type = ClassificationType.getByType(classificationType);

    Classification parent = Classification.get(type, parentCode);
    Classification child = Classification.get(type, childCode);

    parent.removeChild(child);
  }

  @Request(RequestType.SESSION)
  public JsonArray getChildren(String sessionId, String classificationType, String code)
  {
    ClassificationType type = ClassificationType.getByType(classificationType);

    Classification parent = code != null ? Classification.get(type, code) : type.getRoot();

    return parent.getChildren().stream().map(child -> child.toJSON()).collect(Collector.of(() -> new JsonArray(), (r, t) -> r.add((JsonObject) t), (x1, x2) -> {
      x1.addAll(x2);
      return x1;
    }));
  }

}
