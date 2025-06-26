package net.geoprism.registry.axon.command.repository;

import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.commongeoregistry.adapter.dataaccess.GeoObjectOverTime;

public abstract class GeoObjectApplyCommand
{

  @TargetAggregateIdentifier
  private String  uid;

  private Boolean isNew;

  private Boolean isImport;

  // private GeoObjectOverTime object;
  private String  object;

  private Boolean refreshWorking;

  public GeoObjectApplyCommand()
  {
  }

  public GeoObjectApplyCommand(String uid, Boolean isNew, Boolean isImport, String object, Boolean refreshWorking)
  {
    super();
    this.uid = uid;
    this.isNew = isNew;
    this.isImport = isImport;
    this.object = object;
    this.refreshWorking = refreshWorking;
  }

  public GeoObjectApplyCommand(String uid, Boolean isNew, Boolean isImport, GeoObjectOverTime object, Boolean refreshWorking)
  {
    super();
    this.uid = uid;
    this.isNew = isNew;
    this.isImport = isImport;

    if (object != null)
    {
      this.object = object.toJSON().toString();
    }
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

  public Boolean getRefreshWorking()
  {
    return refreshWorking;
  }

  public void setRefreshWorking(Boolean refreshWorking)
  {
    this.refreshWorking = refreshWorking;
  }
}