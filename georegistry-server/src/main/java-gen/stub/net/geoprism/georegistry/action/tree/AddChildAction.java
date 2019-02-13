package net.geoprism.georegistry.action.tree;

import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.action.tree.AddChildActionDTO;
import org.json.JSONObject;

public class AddChildAction extends AddChildActionBase
{
  private static final long serialVersionUID = -325315873;

  @Override
  public void execute()
  {
    this.registry.addChildInTransaction(this.getParentId(), this.getParentTypeCode(), this.getChildId(), this.getChildTypeCode(), this.getHierarchyTypeCode());
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
    this.setHierarchyTypeCode(acaDTO.getHierarchyCode());
  }

  @Override
  public JSONObject serialize()
  {
    JSONObject jo = super.serialize();
    jo.put(AddChildAction.CHILDID, this.getChildId());
    jo.put(AddChildAction.CHILDTYPECODE, this.getChildTypeCode());
    jo.put(AddChildAction.PARENTID, this.getParentId());
    jo.put(AddChildAction.PARENTTYPECODE, this.getParentTypeCode());
    jo.put(AddChildAction.HIERARCHYTYPECODE, this.getHierarchyTypeCode());

    return jo;
  }
  
  @Override
  public void buildFromJson(JSONObject joAction)
  {
    this.setChildTypeCode(joAction.getString(AddChildAction.CHILDTYPECODE));
    this.setChildId(joAction.getString(AddChildAction.CHILDID));
    this.setParentId(joAction.getString(AddChildAction.PARENTID));
    this.setParentTypeCode(joAction.getString(AddChildAction.PARENTTYPECODE));
    this.setHierarchyTypeCode(joAction.getString(AddChildAction.HIERARCHYTYPECODE));
  }

}
