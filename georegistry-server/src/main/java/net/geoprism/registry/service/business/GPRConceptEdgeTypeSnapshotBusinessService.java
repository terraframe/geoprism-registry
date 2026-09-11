package net.geoprism.registry.service.business;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.business.rbac.UserDAO;
import com.runwaysdk.constants.UserInfo;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;

import net.geoprism.graph.ConceptEdgeTypeSnapshot;
import net.geoprism.graph.ConceptEdgeTypeSnapshotQuery;
import net.geoprism.rbac.RoleConstants;
import net.geoprism.registry.Commit;
import net.geoprism.registry.CommitHasSnapshotQuery;
import net.geoprism.registry.RegistryConstants;
import net.geoprism.registry.model.SnapshotContainer;

@Service
@Primary
public class GPRConceptEdgeTypeSnapshotBusinessService extends ConceptEdgeTypeSnapshotBusinessService implements ConceptEdgeTypeSnapshotBusinessServiceIF
{
  @Override
  public ConceptEdgeTypeSnapshot get(SnapshotContainer<?> version, String code)
  {
    if (version instanceof Commit)
    {
      QueryFactory factory = new QueryFactory();

      CommitHasSnapshotQuery vQuery = new CommitHasSnapshotQuery(factory);
      vQuery.WHERE(vQuery.getParent().EQ((Commit) version));

      ConceptEdgeTypeSnapshotQuery query = new ConceptEdgeTypeSnapshotQuery(factory);
      query.WHERE(query.EQ(vQuery.getChild()));
      query.AND(query.getCode().EQ(code));

      try (OIterator<? extends ConceptEdgeTypeSnapshot> it = query.getIterator())
      {
        if (it.hasNext())
        {
          return it.next();
        }
      }

      return null;

    }

    return super.get(version, code);
  }

  @Override
  protected void assignPermissions(ComponentIF component)
  {
    RoleDAO adminRole = RoleDAO.findRole(RoleConstants.ADMIN).getBusinessDAO();
    adminRole.grantPermission(Operation.CREATE, component.getOid());
    adminRole.grantPermission(Operation.DELETE, component.getOid());
    adminRole.grantPermission(Operation.WRITE, component.getOid());
    adminRole.grantPermission(Operation.WRITE_ALL, component.getOid());

    RoleDAO maintainer = RoleDAO.findRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE).getBusinessDAO();
    maintainer.grantPermission(Operation.CREATE, component.getOid());
    maintainer.grantPermission(Operation.DELETE, component.getOid());
    maintainer.grantPermission(Operation.WRITE, component.getOid());
    maintainer.grantPermission(Operation.WRITE_ALL, component.getOid());

    RoleDAO consumer = RoleDAO.findRole(RegistryConstants.API_CONSUMER_ROLE).getBusinessDAO();
    consumer.grantPermission(Operation.READ, component.getOid());
    consumer.grantPermission(Operation.READ_ALL, component.getOid());

    RoleDAO contributor = RoleDAO.findRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE).getBusinessDAO();
    contributor.grantPermission(Operation.READ, component.getOid());
    contributor.grantPermission(Operation.READ_ALL, component.getOid());

    UserDAO publicRole = UserDAO.findUser(UserInfo.PUBLIC_USER_NAME).getBusinessDAO();
    publicRole.grantPermission(Operation.READ, component.getOid());
    publicRole.grantPermission(Operation.READ_ALL, component.getOid());
  }

}
