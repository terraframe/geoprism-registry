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

public class ChangeRequestService
{
  @Request(RequestType.SESSION)
  public void applyAction(String sessionId, String sAction)
  {
    applyActionInTransaction(sAction);
  }

  @Transaction
  public void applyActionInTransaction(String sAction)
  {
    JSONObject joAction = new JSONObject(sAction);

    AbstractAction action = AbstractAction.get(joAction.getString("oid"));

    action.buildFromJson(joAction);

    action.apply();
  }

  @Request(RequestType.SESSION)
  public String getAllActions(String sessionId, String requestId)
  {
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

  @Request(RequestType.SESSION)
  public String lockAction(String sessionId, String actionId)
  {
    AbstractAction action = AbstractAction.get(actionId);
    
    action.lock();
    
    return action.serialize().toString();
  }

  @Request(RequestType.SESSION)
  public String unlockAction(String sessionId, String actionId)
  {
    AbstractAction action = AbstractAction.get(actionId);
    
    action.unlock();
    
    return action.serialize().toString();
  }
}
