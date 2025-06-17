package net.geoprism.registry.axon.event;

public class CreateGeoObjectEvent extends ApplyGeoObjectEvent
{

  public CreateGeoObjectEvent()
  {
    super();
  }

  public CreateGeoObjectEvent(String uid, Boolean isImport, String object, String listId)
  {
    super(uid, true, isImport, object, listId);
  }

}
