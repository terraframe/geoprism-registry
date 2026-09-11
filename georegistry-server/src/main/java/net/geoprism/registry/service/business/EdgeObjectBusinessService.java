package net.geoprism.registry.service.business;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;

import net.geoprism.registry.model.EdgeType;

@Service
public class EdgeObjectBusinessService
{
  public Optional<EdgeObject> getByOid(EdgeType type, String oid)
  {
    String dbClassName = type.getMdEdgeDAO().getDBClassName();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + dbClassName);
    statement.append(" WHERE oid = :oid");

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement.toString());
    query.setParameter("oid", oid);

    return Optional.ofNullable(query.getSingleResult());
  }

  public Optional<EdgeObject> getByUid(EdgeType type, String uid)
  {
    String dbClassName = type.getMdEdgeDAO().getDBClassName();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + dbClassName);
    statement.append(" WHERE uid = :uid");

    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(statement.toString());
    query.setParameter("uid", uid);

    return Optional.ofNullable(query.getSingleResult());
  }

}
