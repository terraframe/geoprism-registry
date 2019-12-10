package net.geoprism.registry.action.geoobject;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.session.Session;

import net.geoprism.localization.LocalizationFacade;
import net.geoprism.registry.model.ServerGeoObjectIF;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.ServerParentTreeNodeOverTime;
import net.geoprism.registry.service.ServerGeoObjectService;

public class SetParentAction extends SetParentActionBase
{
  private static final long serialVersionUID = 876924243;

  public SetParentAction()
  {
    super();
  }

  @Override
  public void execute()
  {
    ServerGeoObjectService service = new ServerGeoObjectService();
    ServerGeoObjectIF child = service.getGeoObjectByCode(this.getChildCode(), this.getChildTypeCode());

    ServerParentTreeNodeOverTime ptnOt = ServerParentTreeNodeOverTime.fromJSON(child.getType(), this.getJson());

    child.setParents(ptnOt);
  }

  @Override
  public JSONObject serialize()
  {
    JSONObject jo = super.serialize();
    jo.put(SetParentAction.JSON, new JSONArray(this.getJson()));
    jo.put(SetParentAction.CHILDTYPECODE, this.getChildTypeCode());
    jo.put(SetParentAction.CHILDCODE, this.getChildCode());

    return jo;
  }

  @Override
  public void buildFromJson(JSONObject joAction)
  {
    super.buildFromJson(joAction);

    this.setChildTypeCode(joAction.getString(SetParentAction.CHILDTYPECODE));
    this.setChildCode(joAction.getString(SetParentAction.CHILDCODE));
    this.setJson(joAction.getJSONArray(SetParentAction.JSON).toString());
  }

  @Override
  protected String getMessage()
  {
    ServerGeoObjectType childType = ServerGeoObjectType.get(this.getChildTypeCode());

    String message = LocalizationFacade.getFromBundles("change.request.email.set.parent");
    message = message.replaceAll("\\{0\\}", childType.getLabel().getValue(Session.getCurrentLocale()));
    message = message.replaceAll("\\{1\\}", this.getChildCode());

    return message;
  }

}
