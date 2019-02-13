package net.geoprism.georegistry.service;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Request;
import com.runwaysdk.session.RequestType;

import net.geoprism.georegistry.action.AbstractAction;
import net.geoprism.georegistry.action.AbstractActionQuery;
import net.geoprism.georegistry.action.AllGovernanceStatus;
import net.geoprism.georegistry.action.ChangeRequest;
import net.geoprism.georegistry.action.ChangeRequestQuery;
import net.geoprism.georegistry.action.geoobject.CreateGeoObjectAction;
import net.geoprism.georegistry.action.geoobject.UpdateGeoObjectAction;
import net.geoprism.georegistry.action.tree.AddChildAction;
import net.geoprism.georegistry.action.tree.RemoveChildAction;

public class ChangeRequestService
{
  @Request(RequestType.SESSION)
  public void acceptAction(String sessionId, String sAction)
  {
    acceptActionInTransaction(sAction);
  }

  @Transaction
  public void acceptActionInTransaction(String sAction)
  {
    JSONObject joAction = new JSONObject(sAction);

    AbstractAction action = AbstractAction.get(joAction.getString("oid"));

    action.appLock();

    updateActionFromJson(action, joAction);

    action.execute();

    action.clearApprovalStatus();
    action.addApprovalStatus(AllGovernanceStatus.ACCEPTED);
    action.apply();
  }

  @Request(RequestType.SESSION)
  public void rejectAction(String sessionId, String sAction)
  {
    rejectActionInTransaction(sAction);
  }

  @Transaction
  public void rejectActionInTransaction(String sAction)
  {
    JSONObject joAction = new JSONObject(sAction);

    AbstractAction action = AbstractAction.get(joAction.getString("oid"));

    action.appLock();

    updateActionFromJson(action, joAction);

    action.clearApprovalStatus();
    action.addApprovalStatus(AllGovernanceStatus.REJECTED);
    action.apply();
  }

  @Request(RequestType.SESSION)
  public String getAllActions(String sessionId, String requestId)
  {
    // JSONObject changeRequest = new JSONObject();
    //
    // changeRequest.put(AbstractAction.APPROVALSTATUS,
    // buildGovernanceStatus(AllGovernanceStatus.PENDING));

    JSONArray actions = new JSONArray();
    QueryFactory factory = new QueryFactory();

    AbstractActionQuery query = new AbstractActionQuery(factory);

    if (requestId != null)
    {
      ChangeRequestQuery rQuery = new ChangeRequestQuery(factory);
      rQuery.WHERE(rQuery.getOid().EQ(requestId));

      query.WHERE(query.request(rQuery));
    }

    query.ORDER_BY(query.getCreateActionDate(), SortOrder.ASC);

    Iterator<? extends AbstractAction> it = query.getIterator();

    while (it.hasNext())
    {
      AbstractAction action = it.next();

      actions.put(action.serialize());
    }
    // changeRequest.put("actions", actions);
    //
    // return changeRequest.toString();

    return actions.toString();
  }

  @Request(RequestType.SESSION)
  public JSONObject approveAllActions(String sessionId, String requestId)
  {
    ChangeRequest request = ChangeRequest.get(requestId);
    request.setAllActionsStatus(AllGovernanceStatus.ACCEPTED);

    return request.getDetails();
  }

  @Request(RequestType.SESSION)
  public JSONObject rejectAllActions(String sessionId, String requestId)
  {
    ChangeRequest request = ChangeRequest.get(requestId);
    request.setAllActionsStatus(AllGovernanceStatus.REJECTED);

    return request.getDetails();
  }

  @Request(RequestType.SESSION)
  public JSONArray getAllRequests(String sessionId)
  {
    ChangeRequestQuery query = new ChangeRequestQuery(new QueryFactory());
    query.ORDER_BY_DESC(query.getCreateDate());

    OIterator<? extends ChangeRequest> it = query.getIterator();

    try
    {
      JSONArray requests = new JSONArray();

      while (it.hasNext())
      {
        ChangeRequest request = it.next();
        requests.put(request.toJSON());
      }

      return requests;
    }
    finally
    {
      it.close();
    }
  }

  @Request(RequestType.SESSION)
  public JSONObject getRequestDetails(String sessionId, String requestId)
  {
    ChangeRequest request = ChangeRequest.get(requestId);

    return request.getDetails();
  }

  @Request(RequestType.SESSION)
  public void executeActions(String sessionId, String requestId)
  {
    ChangeRequest request = ChangeRequest.get(requestId);
    request.execute();
  }

  private void updateActionFromJson(AbstractAction action, JSONObject joAction)
  {
    if (action instanceof CreateGeoObjectAction)
    {
      ( (CreateGeoObjectAction) action ).setGeoObjectJson(joAction.getJSONObject(CreateGeoObjectAction.GEOOBJECTJSON).toString());
    }
    else if (action instanceof UpdateGeoObjectAction)
    {
      ( (UpdateGeoObjectAction) action ).setGeoObjectJson(joAction.getJSONObject(UpdateGeoObjectAction.GEOOBJECTJSON).toString());
    }
    else if (action instanceof AddChildAction)
    {
      AddChildAction aca = (AddChildAction) action;

      aca.setChildTypeCode(joAction.getString(AddChildAction.CHILDTYPECODE));
      aca.setChildId(joAction.getString(AddChildAction.CHILDID));
      aca.setParentId(joAction.getString(AddChildAction.PARENTID));
      aca.setParentTypeCode(joAction.getString(AddChildAction.PARENTTYPECODE));
      aca.setHierarchyTypeCode(joAction.getString(AddChildAction.HIERARCHYTYPECODE));
    }
    else if (action instanceof RemoveChildAction)
    {
      RemoveChildAction rca = (RemoveChildAction) action;

      rca.setChildTypeCode(joAction.getString(RemoveChildAction.CHILDTYPECODE));
      rca.setChildId(joAction.getString(RemoveChildAction.CHILDID));
      rca.setParentId(joAction.getString(RemoveChildAction.PARENTID));
      rca.setParentTypeCode(joAction.getString(RemoveChildAction.PARENTTYPECODE));
      rca.setHierarchyTypeCode(joAction.getString(RemoveChildAction.HIERARCHYTYPECODE));
    }
  }
}
