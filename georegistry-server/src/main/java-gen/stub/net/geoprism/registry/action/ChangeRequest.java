/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
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
import com.runwaysdk.system.SingleActor;
import com.runwaysdk.system.Users;

import net.geoprism.GeoprismUser;
import net.geoprism.localization.LocalizationFacade;
import net.geoprism.registry.io.GeoObjectImportConfiguration;
import net.geoprism.registry.model.ServerGeoObjectType;

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
    DateFormat format = new SimpleDateFormat(GeoObjectImportConfiguration.DATE_FORMAT);

    Users user = (Users) this.getCreatedBy();
    AllGovernanceStatus status = this.getApprovalStatus().get(0);

    JSONObject object = new JSONObject();
    object.put(ChangeRequest.OID, this.getOid());
    object.put(ChangeRequest.CREATEDATE, format.format(this.getCreateDate()));
    object.put(ChangeRequest.CREATEDBY, user.getUsername());
    object.put(ChangeRequest.APPROVALSTATUS, status.getEnumName());
    object.put(ChangeRequest.MAINTAINERNOTES, this.getMaintainerNotes());
    object.put("statusLabel", status.getDisplayLabel());

    if (user instanceof GeoprismUser)
    {
      object.put("email", ( (GeoprismUser) user ).getEmail());
      object.put("phoneNumber", ( (GeoprismUser) user ).getPhoneNumber());
    }

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
      List<String> accepted = new LinkedList<String>();

      List<String> rejected = new LinkedList<String>();

      List<AbstractAction> actions = this.getOrderedActions();

      AllGovernanceStatus status = AllGovernanceStatus.REJECTED;

      for (AbstractAction action : actions)
      {
        if (action.getApprovalStatus().contains(AllGovernanceStatus.PENDING))
        {
          throw new ActionExecuteException("Unable to execute an action with the pending status");
        }
        else if (action.getApprovalStatus().contains(AllGovernanceStatus.ACCEPTED))
        {
          action.execute();

          accepted.add(action.getMessage());

          status = AllGovernanceStatus.ACCEPTED;
        }
        else if (action.getApprovalStatus().contains(AllGovernanceStatus.REJECTED))
        {
          rejected.add(action.getMessage());
        }
      }

      this.appLock();
      this.clearApprovalStatus();
      this.addApprovalStatus(status);
      this.apply();

      // Email the contributor
      SingleActor actor = this.getCreatedBy();

      if (sendEmail && actor instanceof GeoprismUser)
      {
        String email = ( (GeoprismUser) actor ).getEmail();

        if (email != null && email.length() > 0)
        {
          String subject = LocalizationFacade.getFromBundles("change.request.email.subject");
          subject = subject.replaceAll("\\{0\\}", status.getDisplayLabel());

          String body = new String();

          if (accepted.size() > 0)
          {
            body += append(accepted, "change.request.email.body.approved");
          }

          if (rejected.size() > 0)
          {
            if (accepted.size() > 0)
            {
              body += "\n";
              body += "\n";
            }

            body += append(rejected, "change.request.email.body.rejected");
          }

          // EmailSetting.sendEmail(subject, body, new String[] { email });
        }
      }
    }
  }

  private String append(List<String> list, String key)
  {
    String body = LocalizationFacade.getFromBundles(key);

    String messages = "\n";

    for (String message : list)
    {
      messages += message + "\n";
    }

    return body.replaceAll("\\{0\\}", messages);
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

  public boolean isVisible()
  {
    if (Session.getCurrentSession() != null && Session.getCurrentSession().getUser() != null)
    {
      if (this.getOwnerId().equals(Session.getCurrentSession().getUser().getOid()))
      {
        return true;
      }
    }

    List<AbstractAction> actions = this.getOrderedActions();

    for (AbstractAction action : actions)
    {
      if (!action.isVisible())
      {
        return false;
      }
    }

    return true;
  }
  
  public boolean referencesType(ServerGeoObjectType type)
  {
    List<AbstractAction> actions = this.getOrderedActions();
    
    for (AbstractAction action : actions)
    {
      if (action.referencesType(type))
      {
        return true;
      }
    }
    
    return false;
  }
  
  @Transaction
  public void invalidate(String localizedReason)
  {
    this.lock();
    
    this.clearApprovalStatus();
    this.addApprovalStatus(AllGovernanceStatus.REJECTED);
    
    if (this.getMaintainerNotes() != null && this.getMaintainerNotes().length() > 0)
    {
      this.setMaintainerNotes(this.getMaintainerNotes() + " " + localizedReason);
    }
    else
    {
      this.setMaintainerNotes(localizedReason);
    }
    
    List<AbstractAction> actions = this.getOrderedActions();
    
    for (AbstractAction action : actions)
    {
      action.lock();
      
      if (action.getMaintainerNotes() != null && action.getMaintainerNotes().length() > 0)
      {
        action.setMaintainerNotes(action.getMaintainerNotes() + " " + localizedReason);
      }
      else
      {
        action.setMaintainerNotes(localizedReason);
      }
      
      action.apply();
    }
    
    this.apply();
  }

}
