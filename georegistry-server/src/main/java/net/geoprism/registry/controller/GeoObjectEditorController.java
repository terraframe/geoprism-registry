package net.geoprism.registry.controller;

import java.util.List;

import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.ParentTreeNode;
import org.json.JSONException;

import com.runwaysdk.constants.ClientRequestIF;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.mvc.Controller;
import com.runwaysdk.mvc.Endpoint;
import com.runwaysdk.mvc.ErrorSerialization;
import com.runwaysdk.mvc.RequestParamter;
import com.runwaysdk.mvc.ResponseIF;
import com.runwaysdk.mvc.RestResponse;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.registry.service.RegistryService;
import net.geoprism.registry.service.ServiceFactory;

@Controller(url = "geoobject-editor")
public class GeoObjectEditorController
{
  @Endpoint(error = ErrorSerialization.JSON)
  public ResponseIF apply(ClientRequestIF request, @RequestParamter(name = "parentTreeNode") String parentTreeNode, @RequestParamter(name = "geoObject") String geoObject) throws JSONException
  {
    apply(request.getSessionId(), parentTreeNode, geoObject);
    
    return new RestResponse();
  }
  
  @Request(RequestType.SESSION)
  private void apply(String sessionId, String ptn, String go)
  {
    applyInTransaction(sessionId, ptn, go);
  }
  @Transaction
  private void applyInTransaction(String sessionId, String sPtn, String sGo)
  {
    RegistryService.getInstance().updateGeoObject(sessionId, sGo.toString());
    
    ParentTreeNode ptn = ParentTreeNode.fromJSON(sPtn.toString(), ServiceFactory.getAdapter());
    
    applyPtn(sessionId, ptn);
  }
  private void applyPtn(String sessionId, ParentTreeNode ptn)
  {
    GeoObject child = ptn.getGeoObject();
    
    // First remove all existing parents
    List<ParentTreeNode> childDbParents = RegistryService.getInstance().getParentGeoObjects(sessionId, child.getUid(), child.getType().getCode(), null, false).getParents(); // TODO : Leaf nodes
    for (ParentTreeNode childDbParent : childDbParents)
    {
      RegistryService.getInstance().removeChild(sessionId, childDbParent.getGeoObject().getUid(), childDbParent.getGeoObject().getType().getCode(), child.getUid(), child.getType().getCode(), childDbParent.getHierachyType().getCode());
    }
    
    // Now make sure our parents are exactly as this ParentTreeNode specifies
    for (int i = 0; i < ptn.getParents().size(); ++i)
    {
      ParentTreeNode ptnParent = ptn.getParents().get(i);
      GeoObject parent = ptnParent.getGeoObject();
      
      RegistryService.getInstance().addChild(sessionId, parent.getUid(), parent.getType().getCode(), child.getUid(), child.getType().getCode(), ptnParent.getHierachyType().getCode());
      
      applyPtn(sessionId, ptnParent);
    }
  }
}
