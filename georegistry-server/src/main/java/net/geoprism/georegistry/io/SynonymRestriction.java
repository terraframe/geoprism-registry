package net.geoprism.georegistry.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.commongeoregistry.adapter.constants.DefaultAttribute;

import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeDAOIF;
import com.runwaysdk.dataaccess.MdDimensionDAOIF;
import com.runwaysdk.dataaccess.MdLocalStructDAOIF;
import com.runwaysdk.query.Coalesce;
import com.runwaysdk.query.F;
import com.runwaysdk.query.LeftJoinEq;
import com.runwaysdk.query.OR;
import com.runwaysdk.query.SelectableSingle;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.SynonymDisplayLabelQuery;
import com.runwaysdk.system.gis.geo.SynonymQuery;
import com.runwaysdk.system.gis.geo.SynonymRelationshipQuery;

import net.geoprism.georegistry.GeoObjectRestriction;

public class SynonymRestriction implements GeoObjectRestriction
{
  private String label;

  public SynonymRestriction(String label)
  {
    this.label = label;
  }

  @Override
  public void restrict(ValueQuery vQuery, GeoEntityQuery geQuery, BusinessQuery bQuery)
  {
    SynonymRelationshipQuery relationshipQuery = new SynonymRelationshipQuery(vQuery);
    SynonymQuery synonymQuery = new SynonymQuery(vQuery);
    SynonymDisplayLabelQuery labelQuery = new SynonymDisplayLabelQuery(vQuery);

    // query.AND(query.getOid().EQ(aptQuery.getChildTerm().getOid()));
    vQuery.WHERE(new LeftJoinEq(geQuery.getOid(), relationshipQuery.parentOid()));
    vQuery.WHERE(new LeftJoinEq(relationshipQuery.childOid(), synonymQuery.getOid()));
    vQuery.WHERE(new LeftJoinEq(synonymQuery.getDisplayLabel(), labelQuery.getOid()));
    vQuery.WHERE(OR.get(geQuery.getGeoId().EQ(this.label),
        F.TRIM(geQuery.getDisplayLabel().localize()).EQi(this.label),
        F.TRIM(this.localize(labelQuery)).EQi(this.label))
    );
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

  @Override
  public void restrict(ValueQuery vQuery, BusinessQuery bQuery)
  {
    vQuery.WHERE(OR.get(bQuery.get(DefaultAttribute.CODE.getName()).EQ(this.label), bQuery.aLocalCharacter(DefaultAttribute.LOCALIZED_DISPLAY_LABEL.getName()).localize().EQi(this.label)));
  }

}
