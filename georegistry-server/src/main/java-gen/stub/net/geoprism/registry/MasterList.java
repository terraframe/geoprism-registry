package net.geoprism.registry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.constants.BusinessInfo;
import com.runwaysdk.constants.ComponentInfo;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeCharacterInfo;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.MdTableInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdAttributeReferenceDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.metadata.MdAttributeCharacterDAO;
import com.runwaysdk.dataaccess.metadata.MdAttributeDAO;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.query.Selectable;
import com.runwaysdk.query.ValueQuery;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.metadata.MdBusiness;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.query.GeoObjectQuery;
import net.geoprism.registry.service.ConversionService;
import net.geoprism.registry.service.LocaleSerializer;
import net.geoprism.registry.service.ServiceFactory;

public class MasterList extends MasterListBase
{
  private static final long serialVersionUID = 190790165;

  public static String      TYPE_CODE        = "typeCode";

  public static String      PREFIX           = "ml_";

  public MasterList()
  {
    super();
  }

  @Override
  @Transaction
  public void delete()
  {
    MdBusiness mdTable = this.getMdBusiness();

    super.delete();

    if (mdTable != null)
    {
      mdTable.delete();
    }
  }

  private MdBusiness createTable()
  {
    Locale currentLocale = Session.getCurrentLocale();

    String viewName = this.getTableName();

    // Create the MdTable
    MdBusinessDAO mdTableDAO = MdBusinessDAO.newInstance();
    mdTableDAO.setValue(MdTableInfo.NAME, viewName);
    mdTableDAO.setValue(MdTableInfo.PACKAGE, RegistryConstants.TABLE_PACKAGE);
    mdTableDAO.setStructValue(MdTableInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, this.getDisplayLabel().getValue());
    mdTableDAO.setValue(MdTableInfo.TABLE_NAME, viewName);
    mdTableDAO.setValue(MdTableInfo.GENERATE_SOURCE, MdAttributeBooleanInfo.FALSE);
    mdTableDAO.apply();

    MdBusinessDAOIF mdClassifier = MdBusinessDAO.getMdBusinessDAO(Classifier.CLASS);
    MdAttributeConcreteDAOIF mdClassifierId = mdClassifier.definesAttribute(Classifier.CLASSIFIERID);

    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    Universal universal = this.getUniversal();
    GeoObjectType type = ServiceFactory.getConversionService().universalToGeoObjectType(universal);

    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(universal.getMdBusinessOid());

    List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdBusiness.definesAttributes();

    for (MdAttributeConcreteDAOIF mdAttribute : mdAttributes)
    {
      if (isValid(mdAttribute))
      {
        if (mdAttribute instanceof MdAttributeReferenceDAOIF)
        {
          MdBusinessDAOIF referenceMdBusiness = ( (MdAttributeReferenceDAOIF) mdAttribute ).getReferenceMdBusinessDAO();

          if (referenceMdBusiness.getOid().equals(mdClassifier.getOid()))
          {
            String label = mdAttribute.getDisplayLabel(Session.getCurrentLocale());

            MdAttributeDAO cloneAttribute = (MdAttributeDAO) mdClassifierId.getBusinessDAO().copy();
            cloneAttribute.setValue(MdAttributeConcreteInfo.NAME, mdAttribute.definesAttribute());
            cloneAttribute.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
            cloneAttribute.apply();

            MdAttributeDAO mdAttributeDefaultLocale = MdAttributeCharacterDAO.newInstance();
            mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.NAME, mdAttribute.definesAttribute() + "DefaultLocale");
            mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
            mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
            mdAttributeDefaultLocale.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, label);
            mdAttributeDefaultLocale.apply();

            for (Locale locale : locales)
            {
              MdAttributeDAO mdAttributeLocale = MdAttributeCharacterDAO.newInstance();
              mdAttributeLocale.setValue(MdAttributeCharacterInfo.NAME, mdAttribute.definesAttribute() + locale.toString());
              mdAttributeLocale.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
              mdAttributeLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
              mdAttributeLocale.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, label + " (" + locale + ")");
              mdAttributeLocale.apply();
            }
          }
        }
        else
        {
          MdAttributeDAO cloneAttribute = (MdAttributeDAO) mdAttribute.getBusinessDAO().copy();
          cloneAttribute.setValue(MdAttributeConcreteInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
          cloneAttribute.apply();
        }
      }
    }

    if (!type.isLeaf())
    {
      String name = DefaultAttribute.DISPLAY_LABEL.getName();
      String label = DefaultAttribute.DISPLAY_LABEL.getDefaultLocalizedName();

      MdAttributeDAO mdAttributeDefaultLocale = MdAttributeCharacterDAO.newInstance();
      mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.NAME, name + "DefaultLocale");
      mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
      mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
      mdAttributeDefaultLocale.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, label);
      mdAttributeDefaultLocale.apply();

      for (Locale locale : locales)
      {
        MdAttributeDAO mdAttributeLocale = MdAttributeCharacterDAO.newInstance();
        mdAttributeLocale.setValue(MdAttributeCharacterInfo.NAME, name + locale.toString());
        mdAttributeLocale.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
        mdAttributeLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
        mdAttributeLocale.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, label + " (" + locale + ")");
        mdAttributeLocale.apply();
      }
    }

    JsonArray hierarchies = this.getHierarchiesAsJson();

    for (int i = 0; i < hierarchies.size(); i++)
    {
      JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();

      List<String> pCodes = this.getParentCodes(hierarchy);

      if (pCodes.size() > 0)
      {
        String hCode = hierarchy.get("code").getAsString();

        HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(hCode).get();
        String hierarchyLabel = hierarchyType.getLabel().getValue(currentLocale);

        for (String pCode : pCodes)
        {
          GeoObjectType got = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(pCode).get();
          String typeLabel = got.getLabel().getValue(currentLocale);
          String attributeName = hCode.toLowerCase() + pCode.toLowerCase();

          MdAttributeCharacterDAO mdAttributeCode = MdAttributeCharacterDAO.newInstance();
          mdAttributeCode.setValue(MdAttributeCharacterInfo.NAME, attributeName);
          mdAttributeCode.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
          mdAttributeCode.setValue(MdAttributeCharacterInfo.SIZE, "255");
          mdAttributeCode.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, hierarchyLabel + " " + typeLabel);
          mdAttributeCode.apply();

          MdAttributeCharacterDAO mdAttributeDefaultLocale = MdAttributeCharacterDAO.newInstance();
          mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.NAME, attributeName + "DefaultLocale");
          mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
          mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
          mdAttributeDefaultLocale.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, hierarchyLabel + " " + typeLabel);
          mdAttributeDefaultLocale.apply();

          for (Locale locale : locales)
          {
            MdAttributeCharacterDAO mdAttributeLocale = MdAttributeCharacterDAO.newInstance();
            mdAttributeLocale.setValue(MdAttributeCharacterInfo.NAME, attributeName + locale.toString());
            mdAttributeLocale.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
            mdAttributeLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
            mdAttributeLocale.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, hierarchyLabel + " " + typeLabel + " (" + locale + ")");
            mdAttributeLocale.apply();
          }
        }
      }
    }

    return (MdBusiness) BusinessFacade.get(mdTableDAO);

  }

  private List<String> getParentCodes(JsonObject hierarchy)
  {
    List<String> list = new LinkedList<String>();

    JsonArray parents = hierarchy.get("parents").getAsJsonArray();

    for (int i = 0; i < parents.size(); i++)
    {
      JsonObject parent = parents.get(i).getAsJsonObject();

      if (parent.has("selected") && parent.get("selected").getAsBoolean())
      {
        list.add(parent.get("code").getAsString());
      }
    }

    return list;
  }

  public boolean isValid(MdAttributeConcreteDAOIF mdAttribute)
  {
    if (mdAttribute.isSystem())
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(ComponentInfo.KEY))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(BusinessInfo.DOMAIN))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(BusinessInfo.OWNER))
    {
      return false;
    }

    return true;
  }

  private String getTableName()
  {
    int count = 0;

    String name = PREFIX + count + this.getUniversal().getMdBusiness().getTableName();

    while (Database.tableExists(name))
    {
      count++;

      name = PREFIX + count + this.getUniversal().getMdBusiness().getTableName();
    }

    return name;
  }

  public ValueQuery getValueQuery(JsonArray hierarchies)
  {
    ConversionService service = ServiceFactory.getConversionService();

    Universal universal = this.getUniversal();
    GeoObjectType type = service.universalToGeoObjectType(this.getUniversal());

    GeoObjectQuery query = new GeoObjectQuery(type, universal);
    ValueQuery vQuery = query.getValueQuery();

    ValueQuery outer = new ValueQuery(new QueryFactory());
    outer.setDependentPreSqlStatements(vQuery.getDependentPreSqlStatements());
    outer.FROM("(" + vQuery.getSQLWithoutDependentPreSql() + ")", "original_query");

    for (Selectable s : vQuery.getSelectableRefs())
    {
      Selectable c = outer.aSQLCharacter(s.getColumnAlias(), s.getColumnAlias());

      outer.SELECT(c);
    }

    return outer;
  }

  public JsonArray getHierarchiesAsJson()
  {
    if (this.getHierarchies() != null && this.getHierarchies().length() > 0)
    {
      return new JsonParser().parse(this.getHierarchies()).getAsJsonArray();
    }

    return new JsonArray();
  }

  public JsonObject toJSON()
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    Locale locale = Session.getCurrentLocale();
    LocaleSerializer serializer = new LocaleSerializer(locale);

    ConversionService service = ServiceFactory.getConversionService();

    GeoObjectType type = service.universalToGeoObjectType(this.getUniversal());

    JsonObject object = new JsonObject();
    object.addProperty(MasterList.OID, this.getOid());
    object.addProperty(MasterList.TYPE_CODE, type.getCode());
    object.add(MasterList.DISPLAYLABEL, service.convert(this.getDisplayLabel()).toJSON(serializer));
    object.addProperty(MasterList.CODE, this.getCode());
    object.addProperty(MasterList.LISTABSTRACT, this.getListAbstract());
    object.addProperty(MasterList.PROCESS, this.getProcess());
    object.addProperty(MasterList.PROGRESS, this.getProgress());
    object.addProperty(MasterList.ACCESSCONSTRAINTS, this.getAccessConstraints());
    object.addProperty(MasterList.USECONSTRAINTS, this.getUseConstraints());
    object.addProperty(MasterList.ACKNOWLEDGEMENTS, this.getAcknowledgements());
    object.addProperty(MasterList.DISCLAIMER, this.getDisclaimer());
    object.addProperty(MasterList.CONTACTNAME, this.getContactName());
    object.addProperty(MasterList.ORGANIZATION, this.getOrganization());
    object.addProperty(MasterList.TELEPHONENUMBER, this.getTelephoneNumber());
    object.addProperty(MasterList.EMAIL, this.getEmail());
    object.add(MasterList.HIERARCHIES, this.getHierarchiesAsJson());

    if (this.getRepresentativityDate() != null)
    {
      object.addProperty(MasterList.REPRESENTATIVITYDATE, format.format(this.getRepresentativityDate()));
    }

    if (this.getPublishDate() != null)
    {
      object.addProperty(MasterList.PUBLISHDATE, format.format(this.getPublishDate()));
    }

    return object;
  }

  public static MasterList fromJSON(JsonObject object)
  {
    try
    {
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

      String typeCode = object.get(MasterList.TYPE_CODE).getAsString();
      GeoObjectType type = ServiceFactory.getAdapter().getMetadataCache().getGeoObjectType(typeCode).get();

      Universal universal = ServiceFactory.getConversionService().getUniversalFromGeoObjectType(type);

      LocalizedValue label = LocalizedValue.fromJSON(object.get(MasterList.DISPLAYLABEL).getAsJsonObject());

      MasterList list = new MasterList();
      list.setUniversal(universal);
      ServiceFactory.getConversionService().populate(list.getDisplayLabel(), label);
      list.setCode(object.get(MasterList.CODE).getAsString());
      list.setListAbstract(object.get(MasterList.LISTABSTRACT).getAsString());
      list.setProcess(object.get(MasterList.PROCESS).getAsString());
      list.setProgress(object.get(MasterList.PROGRESS).getAsString());
      list.setAccessConstraints(object.get(MasterList.ACCESSCONSTRAINTS).getAsString());
      list.setUseConstraints(object.get(MasterList.USECONSTRAINTS).getAsString());
      list.setAcknowledgements(object.get(MasterList.ACKNOWLEDGEMENTS).getAsString());
      list.setDisclaimer(object.get(MasterList.DISCLAIMER).getAsString());
      list.setContactName(object.get(MasterList.CONTACTNAME).getAsString());
      list.setOrganization(object.get(MasterList.ORGANIZATION).getAsString());
      list.setTelephoneNumber(object.get(MasterList.TELEPHONENUMBER).getAsString());
      list.setEmail(object.get(MasterList.EMAIL).getAsString());
      list.setHierarchies(object.get(MasterList.HIERARCHIES).getAsJsonArray().toString());

      if (object.has(MasterList.REPRESENTATIVITYDATE))
      {
        if (!object.get(MasterList.REPRESENTATIVITYDATE).isJsonNull())
        {
          list.setRepresentativityDate(format.parse(object.get(MasterList.REPRESENTATIVITYDATE).getAsString()));
        }
        else
        {
          list.setRepresentativityDate(null);
        }
      }

      if (object.has(MasterList.PUBLISHDATE))
      {
        if (!object.get(MasterList.PUBLISHDATE).isJsonNull())
        {
          list.setPublishDate(format.parse(object.get(MasterList.PUBLISHDATE).getAsString()));
        }
        else
        {
          list.setPublishDate(null);
        }
      }

      return list;
    }
    catch (ParseException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }

  @Transaction
  public static MasterList create(JsonObject object)
  {
    MasterList list = MasterList.fromJSON(object);
    MdBusiness mdTable = list.createTable();

    list.setMdBusiness(mdTable);
    list.apply();

    return list;
  }

  @Transaction
  public static void deleteAll(Universal universal)
  {
    MasterListQuery query = new MasterListQuery(new QueryFactory());
    query.WHERE(query.getUniversal().EQ(universal));

    List<? extends MasterList> lists = query.getIterator().getAll();

    for (MasterList list : lists)
    {
      list.delete();
    }
  }
}
