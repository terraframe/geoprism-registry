package net.geoprism.registry.axon.event;

public class UpdateGeoObjectEvent extends ApplyGeoObjectEvent
{

  public UpdateGeoObjectEvent()
  {
    super();
  }

  public UpdateGeoObjectEvent(String uid, Boolean isImport, String object, String listId)
  {
    super(uid, false, isImport, object, listId);
  }

}
