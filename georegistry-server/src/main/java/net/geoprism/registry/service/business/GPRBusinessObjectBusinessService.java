package net.geoprism.registry.service.business;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.business.graph.VertexObject;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;

import net.geoprism.registry.BusinessType;
import net.geoprism.registry.model.BusinessObject;

@Service
@Primary
public class GPRBusinessObjectBusinessService extends BusinessObjectBusinessService implements BusinessObjectBusinessServiceIF
{

  public List<BusinessObject> getAll(BusinessType type, Long skip, Integer limit)
  {
    MdVertexDAOIF mdVertex = type.getMdVertexDAO();
    MdAttributeDAOIF mdAttribute = mdVertex.definesAttribute(BusinessObject.CODE);

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT FROM " + mdVertex.getDBClassName());
    statement.append(" ORDER BY " + mdAttribute.getColumnName());
    statement.append(" SKIP " + skip);
    statement.append(" LIMIT " + limit);

    GraphQuery<VertexObject> query = new GraphQuery<VertexObject>(statement.toString());

    return query.getResults().stream().map(r -> {
      return new BusinessObject(r, type);
    }).collect(Collectors.toList());
  }

  public Long getCount(BusinessType type)
  {
    MdVertexDAOIF mdVertex = type.getMdVertexDAO();

    StringBuilder statement = new StringBuilder();
    statement.append("SELECT COUNT(*) FROM " + mdVertex.getDBClassName());

    GraphQuery<Long> query = new GraphQuery<Long>(statement.toString());

    return query.getSingleResult();
  }
}
