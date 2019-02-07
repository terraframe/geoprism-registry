package net.geoprism.georegistry.action.tree;

import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.action.tree.AddChildActionDTO;

public class AddChildAction extends AddChildActionBase
{
  private static final long serialVersionUID = -325315873;
  
  private String parentId;
  
  private String parentTypeCode;
  
  private String childId;
  
  private String childTypeCode;
  
  private String hierarchyCode;
  
  public String getParentId()
  {
    return parentId;
  }

  public void setParentId(String parentId)
  {
    this.parentId = parentId;
  }

  public String getParentTypeCode()
  {
    return parentTypeCode;
  }

  public void setParentTypeCode(String parentTypeCode)
  {
    this.parentTypeCode = parentTypeCode;
  }

  public String getChildId()
  {
    return childId;
  }

  public void setChildId(String childId)
  {
    this.childId = childId;
  }

  public String getChildTypeCode()
  {
    return childTypeCode;
  }

  public void setChildTypeCode(String childTypeCode)
  {
    this.childTypeCode = childTypeCode;
  }

  public String getHierarchyCode()
  {
    return hierarchyCode;
  }

  public void setHierarchyCode(String hierarchyCode)
  {
    this.hierarchyCode = hierarchyCode;
  }

  @Override
  public void execute()
  {
    this.registry.addChild(sessionId, parentId, parentTypeCode, childId, childTypeCode, hierarchyCode);
  }
  
  @Override
  protected void buildFromDTO(AbstractActionDTO dto)
  {
    super.buildFromDTO(dto);
    
    AddChildActionDTO acaDTO = (AddChildActionDTO) dto;
    
    this.setParentId(acaDTO.getParentId());
    this.setParentTypeCode(acaDTO.getParentTypeCode());
    this.setChildId(acaDTO.getChildId());
    this.setChildTypeCode(acaDTO.getChildTypeCode());
    this.setHierarchyCode(acaDTO.getHierarchyCode());
  }
  
}
