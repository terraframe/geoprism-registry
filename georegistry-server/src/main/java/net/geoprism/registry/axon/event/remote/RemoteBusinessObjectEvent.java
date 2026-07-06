package net.geoprism.registry.axon.event.remote;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.ObjectAtTimeDTO;
import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeClass;
import net.geoprism.registry.view.TypeInfo;

public class RemoteBusinessObjectEvent extends RemoteObjectEvent implements RemoteEvent
{

  public RemoteBusinessObjectEvent()
  {
    super();
  }

  public RemoteBusinessObjectEvent(String commitId, String code, String type, ObjectAtTimeDTO object, Date startDate, Date endDate)
  {
    super(commitId, code, type, object, startDate, endDate);
  }

  public RemoteBusinessObjectEvent(String commitId, String key, String code, String type, ObjectAtTimeDTO object, Date startDate, Date endDate)
  {
    super(commitId, key, code, type, object, startDate, endDate);
  }

  @Override
  @JsonIgnore
  public String getBaseObjectId()
  {
    return this.getCode() + "#" + this.getType() + "#B";
  }

  @Override
  public boolean isValid(PublishDTO dto)
  {
    return !dto.getExclusions().contains(TypeInfo.build(this.getType(), TypeClass.BUSINESS_TYPE));
  }
}
