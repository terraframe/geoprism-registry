package net.geoprism.georegistry.action.tree;

import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.action.tree.RemoveChildActionDTO;

public class RemoveChildAction extends RemoveChildActionBase
{
  private static final long serialVersionUID = -165581118;

  @Override
  public void execute()
  {
    this.registry.removeChild(sessionId, this.getParentId(), this.getParentTypeCode(), this.getChildId(), this.getChildTypeCode(), this.getHierarchyTypeCode());
  }
  
  @Override
  protected void buildFromDTO(AbstractActionDTO dto)
  {
    super.buildFromDTO(dto);
    
    RemoveChildActionDTO acaDTO = (RemoveChildActionDTO) dto;
    
    this.setParentId(acaDTO.getParentId());
    this.setParentTypeCode(acaDTO.getParentTypeCode());
    this.setChildId(acaDTO.getChildId());
    this.setChildTypeCode(acaDTO.getChildTypeCode());
    this.setHierarchyTypeCode(acaDTO.getHierarchyCode());
  }
}
