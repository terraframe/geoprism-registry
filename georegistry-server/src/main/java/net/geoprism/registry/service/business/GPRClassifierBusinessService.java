package net.geoprism.registry.service.business;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.query.Coalesce;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.SelectableUUID;
import com.runwaysdk.query.ValueQuery;

import net.geoprism.ontology.Classifier;
import net.geoprism.ontology.ClassifierIsARelationshipAllPathsTableQuery;
import net.geoprism.ontology.ClassifierIsARelationshipQuery;
import net.geoprism.ontology.ClassifierQuery;
import net.geoprism.registry.graph.AttributeTermType;
import net.geoprism.registry.graph.AttributeType;
import net.geoprism.registry.model.ServerGeoObjectType;

@Service
@Primary
public class GPRClassifierBusinessService extends ClassifierBusinessService
{
  public List<ValueObject> getClassifierSuggestions(String typeCode, String attributeCode, String text, Integer limit)
  {
    ServerGeoObjectType code = ServerGeoObjectType.get(typeCode);
    Optional<AttributeType> optional = code.getAttribute(attributeCode);

    if (optional.isPresent())
    {
      AttributeTermType attributeType = (AttributeTermType) optional.get();
      Classifier root = attributeType.getRootTerm();

      ValueQuery query = new ValueQuery(new QueryFactory());

      ClassifierQuery classifierQuery = new ClassifierQuery(query);
      ClassifierIsARelationshipQuery isAQ = new ClassifierIsARelationshipQuery(query);
      ClassifierIsARelationshipAllPathsTableQuery aptQuery = new ClassifierIsARelationshipAllPathsTableQuery(query);

      SelectableUUID oid = classifierQuery.getOid();

      Coalesce label = classifierQuery.getDisplayLabel().localize();
      label.setColumnAlias(Classifier.DISPLAYLABEL);
      label.setUserDefinedAlias(Classifier.DISPLAYLABEL);
      label.setUserDefinedDisplayLabel(Classifier.DISPLAYLABEL);

      query.SELECT(oid, label);
      query.WHERE(label.LIKEi("%" + text + "%"));
      query.AND(isAQ.getParent().EQ(root.getOid()));
      query.AND(aptQuery.getParentTerm().EQ(isAQ.getChild()));
      query.AND(classifierQuery.EQ(aptQuery.getChildTerm()));

      query.ORDER_BY_ASC(label);

      query.restrictRows(limit, 1);

      try (OIterator<ValueObject> it = query.getIterator())
      {
        return it.getAll();
      }
    }

    return new LinkedList<>();
  }

}
