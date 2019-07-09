package net.geoprism.registry.query;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.dataaccess.MdTermRelationshipDAOIF;
import com.runwaysdk.dataaccess.metadata.MdTermDAO;
import com.runwaysdk.dataaccess.metadata.MdTermRelationshipDAO;
import com.runwaysdk.generated.system.gis.geo.LocatedInAllPathsTable;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.metadata.MdTerm;
import com.runwaysdk.system.metadata.ontology.DatabaseAllPathsStrategy;

import net.geoprism.registry.service.ConversionService;

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
      String key = ConversionService.buildMdTermRelGeoEntityKey(this.hierarchyCode);
      MdTermRelationshipDAOIF mdTermRelationship = MdTermRelationshipDAO.getMdTermRelationshipDAO(key);

      String packageName = DatabaseAllPathsStrategy.getPackageName((MdTerm) BusinessFacade.get(MdTermDAO.getMdTermDAO(GeoEntity.CLASS)));
      String typeName = DatabaseAllPathsStrategy.getTypeName(mdTermRelationship);

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
