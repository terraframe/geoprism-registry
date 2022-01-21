package net.geoprism.registry.service;

import com.google.gson.JsonObject;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.model.Classification;
import net.geoprism.registry.model.ClassificationType;

public class ClassificationService
{
  @Transaction
  public JsonObject apply(String classificationType, JsonObject object, boolean isNew, boolean isImport)
  {
    ClassificationType type = ClassificationType.getByType(classificationType);

    Classification classification = Classification.construct(type, object, isNew);
    classification.populate(object);
    classification.apply();

    // Return the refreshed copy of the geoObject
    return classification.toJSON();
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

}
