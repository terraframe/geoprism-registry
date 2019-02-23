package net.geoprism.georegistry.io;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.dataaccess.metadata.MdTermDAO;
import com.runwaysdk.dataaccess.metadata.MdTermRelationshipDAO;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;
import com.runwaysdk.generated.system.gis.geo.LocatedInAllPathsTable;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.GeoEntityDisplayLabelQuery.GeoEntityDisplayLabelQueryStructIF;
import com.runwaysdk.system.gis.geo.GeoEntityQuery;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.geo.UniversalQuery;
import com.runwaysdk.system.metadata.MdTerm;
import com.runwaysdk.system.metadata.MdTermRelationship;
import com.runwaysdk.system.metadata.ontology.DatabaseAllPathsStrategy;

import net.geoprism.georegistry.service.ServiceFactory;

public class GeoObjectUtil
{
  @SuppressWarnings("unchecked")
  public static String convertToTermString(AttributeTermType attributeType, Object value)
  {
    Iterator<String> codes = (Iterator<String>) value;
    StringBuilder builder = new StringBuilder();

    boolean first = true;

    while (codes.hasNext())
    {
      String code = codes.next();
      Term term = attributeType.getTermByCode(code).get();

      if (!first)
      {
        builder.append(",");
      }

      builder.append(term.getLabel());
      first = false;
    }

    return builder.toString();
  }

  public static Map<String, ValueObject> getAncestorMap(GeoObject object, HierarchyType hierarchy)
  {
    Map<String, ValueObject> map = new HashMap<String, ValueObject>();

    if (object.getType().isLeaf())
    {
      throw new java.lang.UnsupportedOperationException();
    }
    else
    {
      MdTermRelationship mdTermRelationship = ServiceFactory.getConversionService().existingHierarchyToGeoEntityMdTermRelationiship(hierarchy);

      String packageName = DatabaseAllPathsStrategy.getPackageName((MdTerm) BusinessFacade.get(MdTermDAO.getMdTermDAO(GeoEntity.CLASS)));
      String typeName = DatabaseAllPathsStrategy.getTypeName(MdTermRelationshipDAO.get(mdTermRelationship.getOid()));

      ValueQuery vQuery = new ValueQuery(new QueryFactory());
      BusinessQuery aptQuery = new BusinessQuery(vQuery, packageName + "." + typeName);
      GeoEntityQuery parentQuery = new GeoEntityQuery(vQuery);
      GeoEntityQuery childQuery = new GeoEntityQuery(vQuery);
      UniversalQuery universalQuery = new UniversalQuery(vQuery);
      
      GeoEntityDisplayLabelQueryStructIF label = parentQuery.getDisplayLabel();

      vQuery.SELECT(parentQuery.getGeoId());
      vQuery.SELECT(universalQuery.getKeyName());
      vQuery.SELECT(label.get(MdAttributeLocalInfo.DEFAULT_LOCALE, DefaultAttribute.DISPLAY_LABEL.getName()));

      List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

      for (Locale locale : locales)
      {
        vQuery.SELECT(label.get(locale.toString(), DefaultAttribute.DISPLAY_LABEL.getName() + "_" + locale.toString()));
      }

      vQuery.AND(childQuery.getGeoId().EQ(object.getCode()));
      vQuery.AND(parentQuery.getUniversal().EQ(universalQuery));
      vQuery.AND(aptQuery.aReference(LocatedInAllPathsTable.PARENTTERM).EQ(parentQuery));
      vQuery.AND(aptQuery.aReference(LocatedInAllPathsTable.CHILDTERM).EQ(childQuery));

      OIterator<ValueObject> it = vQuery.getIterator();

      try
      {
        while (it.hasNext())
        {
          ValueObject vObject = it.next();
          String key = vObject.getValue(Universal.KEYNAME);

          map.put(key, vObject);
        }
      }
      finally
      {
        it.close();
      }
    }

    return map;
  }

}
