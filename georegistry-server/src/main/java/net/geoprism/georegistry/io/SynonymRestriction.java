package net.geoprism.georegistry.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdDimensionDAOIF;
import com.runwaysdk.dataaccess.MdLocalStructDAOIF;
import com.runwaysdk.dataaccess.metadata.MdTermDAO;
import com.runwaysdk.dataaccess.metadata.MdTermRelationshipDAO;
import com.runwaysdk.generated.system.gis.geo.LocatedInAllPathsTable;
import com.runwaysdk.query.Coalesce;
import com.runwaysdk.query.F;
import com.runwaysdk.query.LeftJoinEq;
import com.runwaysdk.query.OR;
import com.runwaysdk.query.SelectableSingle;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.SynonymDisplayLabelQuery;
import com.runwaysdk.system.gis.geo.SynonymQuery;
import com.runwaysdk.system.gis.geo.SynonymRelationshipQuery;
import com.runwaysdk.system.metadata.MdTerm;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.metadata.ontology.DatabaseAllPathsStrategy;

import net.geoprism.georegistry.GeoObjectRestriction;

public class SynonymRestriction implements GeoObjectRestriction
{
  private String             label;

  private GeoObject          parent;

  private MdTermRelationship mdTermRelationship;

  public SynonymRestriction(String label)
  {
    this.label = label;
    this.parent = null;
    this.mdTermRelationship = null;
  }

  public SynonymRestriction(String label, GeoObject parent, MdTermRelationship mdTermRelationship)
  {
    this.label = label;
    this.parent = parent;
    this.mdTermRelationship = mdTermRelationship;
  }

  @Override
  public void restrict(ValueQuery vQuery, GeoEntityQuery geQuery, BusinessQuery bQuery)
  {
    SynonymRelationshipQuery relationshipQuery = new SynonymRelationshipQuery(vQuery);
    SynonymQuery synonymQuery = new SynonymQuery(vQuery);
    SynonymDisplayLabelQuery labelQuery = new SynonymDisplayLabelQuery(vQuery);

    vQuery.WHERE(new LeftJoinEq(geQuery.getOid(), relationshipQuery.parentOid()));
    vQuery.AND(new LeftJoinEq(relationshipQuery.childOid(), synonymQuery.getOid()));
    vQuery.AND(new LeftJoinEq(synonymQuery.getDisplayLabel(), labelQuery.getOid()));
    vQuery.AND(OR.get(geQuery.getGeoId().EQ(this.label), F.TRIM(geQuery.getDisplayLabel().localize()).EQi(this.label), F.TRIM(this.localize(labelQuery)).EQi(this.label)));

    if (this.parent != null && this.mdTermRelationship != null)
    {
      String packageName = DatabaseAllPathsStrategy.getPackageName((MdTerm) BusinessFacade.get(MdTermDAO.getMdTermDAO(GeoEntity.CLASS)));
      String typeName = DatabaseAllPathsStrategy.getTypeName(MdTermRelationshipDAO.get(this.mdTermRelationship.getOid()));

      BusinessQuery aptQuery = new BusinessQuery(vQuery, packageName + "." + typeName);
      GeoEntityQuery parentQuery = new GeoEntityQuery(vQuery);

      vQuery.AND(parentQuery.getGeoId().EQ(this.parent.getCode()));
      vQuery.AND(aptQuery.aReference(LocatedInAllPathsTable.PARENTTERM).EQ(parentQuery));
      vQuery.AND(aptQuery.aReference(LocatedInAllPathsTable.CHILDTERM).EQ(geQuery));
    }
  }

  @Override
  public void restrict(ValueQuery vQuery, BusinessQuery bQuery)
  {
    vQuery.WHERE(OR.get(bQuery.get(DefaultAttribute.CODE.getName()).EQ(this.label), bQuery.aLocalCharacter(DefaultAttribute.LOCALIZED_DISPLAY_LABEL.getName()).localize().EQi(this.label)));
  }

  public Coalesce localize(SynonymDisplayLabelQuery query)
  {
    List<SelectableSingle> selectableList = new ArrayList<SelectableSingle>();

    MdLocalStructDAOIF mdLocalStruct = (MdLocalStructDAOIF) query.getMdEntityIF();
    Locale locale = Session.getCurrentLocale();

    String[] localeStringArray;
    MdDimensionDAOIF mdDimensionDAOIF = Session.getCurrentDimension();
    if (mdDimensionDAOIF != null)
    {
      localeStringArray = new String[2];
      localeStringArray[0] = mdDimensionDAOIF.getLocaleAttributeName(locale);
      localeStringArray[1] = locale.toString();
    }
    else
    {
      localeStringArray = new String[1];
      localeStringArray[0] = locale.toString();
    }

    boolean firstIterationComplete = false;
    for (String localeString : localeStringArray)
    {
      for (int i = localeString.length(); i > 0; i = localeString.lastIndexOf('_', i - 1))
      {
        String subLocale = localeString.substring(0, i);
        for (MdAttributeConcreteDAOIF a : mdLocalStruct.definesAttributes())
        {
          if (a.definesAttribute().equalsIgnoreCase(subLocale))
          {
            selectableList.add(query.get(subLocale));
          }
        }
      }

      // Check the default for the dimension
      if (mdDimensionDAOIF != null && !firstIterationComplete)
      {
        String dimensionDefaultAttr = mdDimensionDAOIF.getDefaultLocaleAttributeName();
        MdAttributeDAOIF definesDimensionDefault = mdLocalStruct.definesAttribute(dimensionDefaultAttr);
        if (definesDimensionDefault != null)
        {
          selectableList.add(query.get(dimensionDefaultAttr));
        }
      }

      firstIterationComplete = true;
    }
    // And finally, add the default at the very end
    selectableList.add(query.get(MdAttributeLocalInfo.DEFAULT_LOCALE));

    SelectableSingle firstSelectable = selectableList.remove(0);
    SelectableSingle[] optionalSelectableArray = new SelectableSingle[selectableList.size()];

    return F.COALESCE(null, null, firstSelectable, selectableList.toArray(optionalSelectableArray));
  }
}
