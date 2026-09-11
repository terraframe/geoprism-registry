package net.geoprism.registry.axon.event.repository;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.geoprism.registry.view.PublishDTO;
import net.geoprism.registry.view.TypeInfo;

public class RemoveGeoObjectEdgeEvent extends AbstractRepositoryEvent implements BaseObjectEvent
{
  private String   sourceCode;

  private TypeInfo sourceType;

  private String   targetCode;

  private TypeInfo tagetType;

  private TypeInfo edgeType;

  public RemoveGeoObjectEdgeEvent()
  {
  }

  public RemoveGeoObjectEdgeEvent(String targetCode, TypeInfo tagetType, TypeInfo edgeType)
  {
    super(UUID.randomUUID().toString());
    this.targetCode = targetCode;
    this.tagetType = tagetType;
    this.edgeType = edgeType;
  }

  public RemoveGeoObjectEdgeEvent(String targetCode, TypeInfo tagetType, String sourceCode, TypeInfo sourceType, TypeInfo edgeType)
  {
    super(UUID.randomUUID().toString());
    this.targetCode = targetCode;
    this.tagetType = tagetType;
    this.sourceCode = sourceCode;
    this.sourceType = sourceType;
    this.edgeType = edgeType;
  }

  public String getSourceCode()
  {
    return sourceCode;
  }

  public void setSourceCode(String sourceCode)
  {
    this.sourceCode = sourceCode;
  }

  public TypeInfo getSourceType()
  {
    return sourceType;
  }

  public void setSourceType(TypeInfo sourceType)
  {
    this.sourceType = sourceType;
  }

  public String getTargetCode()
  {
    return targetCode;
  }

  public void setTargetCode(String targetCode)
  {
    this.targetCode = targetCode;
  }

  public TypeInfo getTagetType()
  {
    return tagetType;
  }

  public void setTagetType(TypeInfo tagetType)
  {
    this.tagetType = tagetType;
  }

  public TypeInfo getEdgeType()
  {
    return edgeType;
  }

  public void setEdgeType(TypeInfo edgeType)
  {
    this.edgeType = edgeType;
  }

  @Override
  @JsonIgnore
  public String getBaseObjectId()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  @JsonIgnore
  public EventPhase getEventPhase()
  {
    return EventPhase.EDGE;
  }

  @Override
  public Boolean isValidFor(PublishDTO dto)
  {
    return dto.getTypes().stream().anyMatch(this.getEdgeType()::equals);
  }

}
