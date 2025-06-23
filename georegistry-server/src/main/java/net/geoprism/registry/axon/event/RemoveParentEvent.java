package net.geoprism.registry.axon.event;

public class RemoveParentEvent extends AbstractGeoObjectEvent implements GeoObjectEvent
{
  private String type;

  private String uid;

  private String edgeType;

  private String edgeUid;

  public RemoveParentEvent()
  {
  }

  public RemoveParentEvent(String uid, String type, String edgeUid, String edgeType)
  {
    super();
    
    this.uid = uid;
    this.type = type;
    this.edgeUid = edgeUid;
    this.edgeType = edgeType;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getUid()
  {
    return uid;
  }

  public void setUid(String uid)
  {
    this.uid = uid;
  }

  public String getEdgeType()
  {
    return edgeType;
  }

  public void setEdgeType(String edgeType)
  {
    this.edgeType = edgeType;
  }

  public String getEdgeUid()
  {
    return edgeUid;
  }

  public void setEdgeUid(String edgeUid)
  {
    this.edgeUid = edgeUid;
  }

}
