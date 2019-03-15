package net.geoprism.registry.action;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.business.RelationshipQuery;
import com.runwaysdk.dataaccess.MdRelationshipDAOIF;
import com.runwaysdk.dataaccess.metadata.MdRelationshipDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.Users;

import net.geoprism.registry.action.ChangeRequestBase;

public class ChangeRequest extends ChangeRequestBase
{
  private static final long serialVersionUID = 763209854;

  public ChangeRequest()
  {
    super();
  }

  public List<AbstractAction> getOrderedActions()
  {
    // TODO : Runway querybuilder is dumb
    // QueryFactory qf = new QueryFactory();
    //
    // ChangeRequestQuery crq = new ChangeRequestQuery(qf);
    // AbstractActionQuery aaq = new AbstractActionQuery(qf);
    //
    // aaq.ORDER_BY(aaq.getCreateActionDate(), SortOrder.DESC);
    // aaq.WHERE(aaq.request(crq));
    // aaq.WHERE(crq.getOid().EQ(this.getOid()));
    //
    // return aaq.getIterator().getAll();

    MdRelationshipDAOIF mdRelationshipIF = MdRelationshipDAO.getMdRelationshipDAO(HasActionRelationship.CLASS);

    QueryFactory queryFactory = new QueryFactory();

    // Get the relationships where this object is the parent
    RelationshipQuery rq = queryFactory.relationshipQuery(HasActionRelationship.CLASS);
    rq.WHERE(rq.parentOid().EQ(this.getOid()));

    // Now fetch the child objects
    BusinessQuery bq = queryFactory.businessQuery(mdRelationshipIF.getChildMdBusiness().definesType());
    bq.WHERE(bq.isChildIn(rq));

    bq.ORDER_BY(bq.get(AbstractAction.CREATEACTIONDATE), SortOrder.ASC);

    List<Business> bizes = bq.getIterator().getAll();
    List<AbstractAction> actions = new ArrayList<AbstractAction>();

    for (Business biz : bizes)
    {
      actions.add((AbstractAction) biz);
    }

    return actions;
  }

  @Override
  public void delete()
  {
    List<? extends AbstractAction> actions = this.getOrderedActions();
    for (AbstractAction action : actions)
    {
      action.delete();
    }

    super.delete();
  }

  public JSONObject toJSON()
  {
    DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Session.getCurrentLocale());

    Users user = (Users) this.getCreatedBy();
    AllGovernanceStatus status = this.getApprovalStatus().get(0);

    JSONObject object = new JSONObject();
    object.put(ChangeRequest.OID, this.getOid());
    object.put(ChangeRequest.CREATEDATE, format.format(this.getCreateDate()));
    object.put(ChangeRequest.CREATEDBY, user.getUsername());
    object.put(ChangeRequest.APPROVALSTATUS, status.getDisplayLabel());

    return object;
  }

  public JSONObject getDetails()
  {
    int total = 0;
    int pending = 0;

    OIterator<? extends AbstractAction> it = this.getAllAction();

    try
    {
      while (it.hasNext())
      {
        total++;

        AbstractAction action = it.next();

        if (action.getApprovalStatus().contains(AllGovernanceStatus.PENDING))
        {
          pending++;
        }
      }
    }
    finally
    {
      it.close();
    }
    AllGovernanceStatus status = this.getApprovalStatus().get(0);

    JSONObject object = this.toJSON();
    object.put("total", total);
    object.put("pending", pending);
    object.put("statusCode", status.getEnumName());

    return object;
  }

  @Transaction
  public void execute()
  {
    if (this.getApprovalStatus().contains(AllGovernanceStatus.PENDING))
    {
      List<AbstractAction> actions = this.getOrderedActions();

      for (AbstractAction action : actions)
      {
        if (action.getApprovalStatus().contains(AllGovernanceStatus.PENDING))
        {
          throw new ActionExecuteException("Unable to execute an action with the pending status");
        }
        else if (action.getApprovalStatus().contains(AllGovernanceStatus.ACCEPTED))
        {
          action.execute();
        }
      }

      this.appLock();
      this.clearApprovalStatus();
      this.addApprovalStatus(AllGovernanceStatus.ACCEPTED);
      this.apply();
    }
  }

  @Transaction
  public void setAllActionsStatus(AllGovernanceStatus status)
  {
    OIterator<? extends AbstractAction> it = this.getAllAction();

    try
    {
      while (it.hasNext())
      {
        AbstractAction action = it.next();

        if (!status.equals(AllGovernanceStatus.ACCEPTED) || action.getApprovalStatus().contains(AllGovernanceStatus.PENDING))
        {
          action.appLock();
          action.clearApprovalStatus();
          action.addApprovalStatus(status);
          action.apply();
        }
      }
    }
    finally
    {
      it.close();
    }

    if (status.equals(AllGovernanceStatus.REJECTED))
    {
      this.appLock();
      this.clearApprovalStatus();
      this.addApprovalStatus(AllGovernanceStatus.REJECTED);
      this.apply();
    }
  }

}
