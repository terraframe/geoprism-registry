package net.geoprism.registry.action.tree;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.action.AbstractActionDTO;
import org.commongeoregistry.adapter.action.tree.RemoveChildActionDTO;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;
import org.commongeoregistry.adapter.metadata.MetadataCache;
import org.json.JSONObject;

import com.runwaysdk.session.Session;

import net.geoprism.localization.LocalizationFacade;
import net.geoprism.registry.action.tree.RemoveChildActionBase;
import net.geoprism.registry.service.ServiceFactory;

public class RemoveChildAction extends RemoveChildActionBase
{
  private static final long serialVersionUID = -165581118;

  @Override
  public void execute()
  {
    this.registry.removeChildInTransaction(this.getParentId(), this.getParentTypeCode(), this.getChildId(), this.getChildTypeCode(), this.getHierarchyTypeCode());
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

  @Override
  public JSONObject serialize()
  {
    JSONObject jo = super.serialize();
    jo.put(RemoveChildAction.CHILDID, this.getChildId());
    jo.put(RemoveChildAction.CHILDTYPECODE, this.getChildTypeCode());
    jo.put(RemoveChildAction.PARENTID, this.getParentId());
    jo.put(RemoveChildAction.PARENTTYPECODE, this.getParentTypeCode());
    jo.put(RemoveChildAction.HIERARCHYTYPECODE, this.getHierarchyTypeCode());

    return jo;
  }

  @Override
  public void buildFromJson(JSONObject joAction)
  {
    super.buildFromJson(joAction);

    this.setChildTypeCode(joAction.getString(RemoveChildAction.CHILDTYPECODE));
    this.setChildId(joAction.getString(RemoveChildAction.CHILDID));
    this.setParentId(joAction.getString(RemoveChildAction.PARENTID));
    this.setParentTypeCode(joAction.getString(RemoveChildAction.PARENTTYPECODE));
    this.setHierarchyTypeCode(joAction.getString(RemoveChildAction.HIERARCHYTYPECODE));
  }

  @Override
  protected String getMessage()
  {
    RegistryAdapter adapter = ServiceFactory.getAdapter();
    MetadataCache cache = adapter.getMetadataCache();

    GeoObjectType parentType = cache.getGeoObjectType(this.getParentTypeCode()).get();
    GeoObjectType childType = cache.getGeoObjectType(this.getChildTypeCode()).get();
    HierarchyType hierarchyType = cache.getHierachyType(this.getHierarchyTypeCode()).get();

    String message = LocalizationFacade.getFromBundles("change.request.email.remove.child");
    message = message.replaceAll("\\{0\\}", this.getChildId());
    message = message.replaceAll("\\{1\\}", childType.getLabel().getValue(Session.getCurrentLocale()));
    message = message.replaceAll("\\{2\\}", this.getParentId());
    message = message.replaceAll("\\{3\\}", parentType.getLabel().getValue(Session.getCurrentLocale()));
    message = message.replaceAll("\\{4\\}", hierarchyType.getLabel().getValue(Session.getCurrentLocale()));

    return message;
  }
}
