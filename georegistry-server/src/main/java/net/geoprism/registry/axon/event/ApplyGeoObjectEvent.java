package net.geoprism.registry.axon.event;

public class ApplyGeoObjectEvent
{
  private String  uid;

  private Boolean isNew;

  private Boolean isImport;

  private String  object;

  private String  parents;

  public ApplyGeoObjectEvent()
  {
  }

  public ApplyGeoObjectEvent(String uid, Boolean isNew, Boolean isImport, String object, String parents)
  {
    super();
    this.uid = uid;
    this.isNew = isNew;
    this.isImport = isImport;
    this.object = object;
    this.parents = parents;
  }

  public String getUid()
  {
    return uid;
  }

  public void setUid(String uid)
  {
    this.uid = uid;
  }

  public Boolean getIsNew()
  {
    return isNew;
  }

  public void setIsNew(Boolean isNew)
  {
    this.isNew = isNew;
  }

  public Boolean getIsImport()
  {
    return isImport;
  }

  public void setIsImport(Boolean isImport)
  {
    this.isImport = isImport;
  }

  public String getObject()
  {
    return object;
  }

  public void setObject(String object)
  {
    this.object = object;
  }

  public String getParents()
  {
    return parents;
  }

  public void setParents(String parents)
  {
    this.parents = parents;
  }
}
