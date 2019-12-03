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
package net.geoprism.registry.query.postgres;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.dataaccess.metadata.MdTermDAO;
import com.runwaysdk.generated.system.gis.geo.LocatedInAllPathsTable;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.metadata.MdTerm;
import com.runwaysdk.system.metadata.ontology.DatabaseAllPathsStrategy;

import net.geoprism.registry.model.ServerHierarchyType;

public class LookupRestriction implements GeoObjectRestriction
{
  private String text;

  private String parentCode;

  private String hierarchyCode;

  public LookupRestriction(String text, String parentCode, String hierarchyCode)
  {
    this.text = text;
    this.parentCode = parentCode;
    this.hierarchyCode = hierarchyCode;
  }

  @Override
  public void restrict(ValueQuery vQuery, GeoEntityQuery geQuery, BusinessQuery bQuery)
  {
    if (this.text != null && this.text.length() > 0)
    {
      vQuery.AND(geQuery.getDisplayLabel().localize().LIKEi("%" + this.text + "%"));
    }

    if (this.parentCode != null && this.hierarchyCode != null && this.parentCode.length() > 0 && this.hierarchyCode.length() > 0)
    {
      ServerHierarchyType hierarchyType = ServerHierarchyType.get(this.hierarchyCode);

      String packageName = DatabaseAllPathsStrategy.getPackageName((MdTerm) BusinessFacade.get(MdTermDAO.getMdTermDAO(GeoEntity.CLASS)));
      String typeName = DatabaseAllPathsStrategy.getTypeName(hierarchyType.getEntityRelationshipDAO());

      BusinessQuery aptQuery = new BusinessQuery(vQuery, packageName + "." + typeName);
      GeoEntityQuery parentQuery = new GeoEntityQuery(vQuery);

      vQuery.AND(parentQuery.getGeoId().EQ(this.parentCode));
      vQuery.AND(aptQuery.aReference(LocatedInAllPathsTable.PARENTTERM).EQ(parentQuery));
      vQuery.AND(aptQuery.aReference(LocatedInAllPathsTable.CHILDTERM).EQ(geQuery));
    }
  }

  @Override
  public void restrict(ValueQuery vQuery, BusinessQuery bQuery)
  {
    throw new UnsupportedOperationException();
  }

}
