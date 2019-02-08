package net.geoprism.georegistry.action;

import java.util.ArrayList;
import java.util.List;

import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.business.RelationshipQuery;
import com.runwaysdk.dataaccess.MdRelationshipDAOIF;
import com.runwaysdk.dataaccess.metadata.MdRelationshipDAO;
import com.runwaysdk.query.OrderBy.SortOrder;
import com.runwaysdk.query.QueryFactory;

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
//    QueryFactory qf = new QueryFactory();
//    
//    ChangeRequestQuery crq = new ChangeRequestQuery(qf);
//    AbstractActionQuery aaq = new AbstractActionQuery(qf);
//    
//    aaq.ORDER_BY(aaq.getCreateActionDate(), SortOrder.DESC);
//    aaq.WHERE(aaq.request(crq));
//    aaq.WHERE(crq.getOid().EQ(this.getOid()));
//    
//    return aaq.getIterator().getAll();
    
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
  
}
