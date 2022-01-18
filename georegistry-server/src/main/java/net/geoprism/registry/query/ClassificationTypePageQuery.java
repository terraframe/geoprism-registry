package net.geoprism.registry.query;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.constants.graph.MdClassificationInfo;
import com.runwaysdk.dataaccess.MdClassificationDAOIF;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;

import net.geoprism.registry.model.ClassificationType;

public class ClassificationTypePageQuery extends AbstractBusinessPageQuery<ClassificationType>
{

  public ClassificationTypePageQuery(JsonObject criteria)
  {
    super(MdBusinessDAO.getMdBusinessDAO(MdClassificationInfo.CLASS), criteria);
  }

  @Override
  protected List<ClassificationType> getResults(List<? extends Business> results)
  {

    return results.stream().map(row -> {
      MdClassificationDAOIF mdClassification = (MdClassificationDAOIF) BusinessFacade.getEntityDAO(row);

      return new ClassificationType(mdClassification);

    }).collect(Collectors.toList());
  }

}
