/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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
