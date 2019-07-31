/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.action;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.taskdefs.SendEmail;
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
import com.runwaysdk.system.SingleActor;
import com.runwaysdk.system.Users;

import net.geoprism.EmailSetting;
import net.geoprism.GeoprismUser;
import net.geoprism.localization.LocalizationFacade;
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
    object.put(ChangeRequest.MAINTAINERNOTES, this.getMaintainerNotes());

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
    object.put(ChangeRequest.MAINTAINERNOTES, this.getMaintainerNotes());

    return object;
  }

  @Transaction
  public void execute(boolean sendEmail)
  {
    if (this.getApprovalStatus().contains(AllGovernanceStatus.PENDING))
    {
      List<String> messages = new LinkedList<String>();

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

          messages.add(action.getMessage());
        }
      }

      this.appLock();
      this.clearApprovalStatus();
      this.addApprovalStatus(AllGovernanceStatus.ACCEPTED);
      this.apply();

      // Email the contributor
      SingleActor actor = this.getCreatedBy();

      if (sendEmail && actor instanceof GeoprismUser)
      {
        String email = ( (GeoprismUser) actor ).getEmail();

        if (email != null && email.length() > 0)
        {
          String subject = LocalizationFacade.getFromBundles("change.request.email.subject");
          String body = LocalizationFacade.getFromBundles("change.request.email.body");

          body += "\n";

          for (String message : messages)
          {
            body += message + "\n";
          }

          EmailSetting.sendEmail(subject, body, new String[] { email });
        }
      }
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

        // if (!status.equals(AllGovernanceStatus.ACCEPTED) ||
        // action.getApprovalStatus().contains(AllGovernanceStatus.PENDING))
        // {
        action.appLock();
        action.clearApprovalStatus();
        action.addApprovalStatus(status);
        action.apply();
        // }
      }
    }
    finally
    {
      it.close();
    }

    // if (status.equals(AllGovernanceStatus.REJECTED))
    // {
    // this.appLock();
    // this.clearApprovalStatus();
    // this.addApprovalStatus(AllGovernanceStatus.REJECTED);
    // this.apply();
    // }
  }

}
