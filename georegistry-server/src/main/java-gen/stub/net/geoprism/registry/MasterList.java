package net.geoprism.registry;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.constants.GeometryType;
import org.commongeoregistry.adapter.dataaccess.GeoObject;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeCharacterType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.commongeoregistry.adapter.metadata.GeoObjectType;
import org.commongeoregistry.adapter.metadata.HierarchyType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.runwaysdk.ComponentIF;
import com.runwaysdk.business.Business;
import com.runwaysdk.business.BusinessFacade;
import com.runwaysdk.business.BusinessQuery;
import com.runwaysdk.business.rbac.Operation;
import com.runwaysdk.business.rbac.RoleDAO;
import com.runwaysdk.constants.BusinessInfo;
import com.runwaysdk.constants.MdAttributeBooleanInfo;
import com.runwaysdk.constants.MdAttributeCharacterInfo;
import com.runwaysdk.constants.MdAttributeConcreteInfo;
import com.runwaysdk.constants.MdAttributeDoubleInfo;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.constants.MdTableInfo;
import com.runwaysdk.dataaccess.MdAttributeConcreteDAOIF;
import com.runwaysdk.dataaccess.MdBusinessDAOIF;
import com.runwaysdk.dataaccess.ProgrammingErrorException;
import com.runwaysdk.dataaccess.ValueObject;
import com.runwaysdk.dataaccess.database.Database;
import com.runwaysdk.dataaccess.metadata.MdAttributeCharacterDAO;
import com.runwaysdk.dataaccess.metadata.MdBusinessDAO;
import com.runwaysdk.dataaccess.metadata.SupportedLocaleDAO;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.query.OIterator;
import com.runwaysdk.query.QueryFactory;
import com.runwaysdk.session.Session;
import com.runwaysdk.system.gis.geo.GeoEntity;
import com.runwaysdk.system.gis.geo.Universal;
import com.runwaysdk.system.gis.metadata.MdAttributeGeometry;
import com.runwaysdk.system.gis.metadata.MdAttributeLineString;
import com.runwaysdk.system.gis.metadata.MdAttributeMultiLineString;
import com.runwaysdk.system.gis.metadata.MdAttributeMultiPoint;
import com.runwaysdk.system.gis.metadata.MdAttributeMultiPolygon;
import com.runwaysdk.system.gis.metadata.MdAttributePoint;
import com.runwaysdk.system.gis.metadata.MdAttributePolygon;
import com.runwaysdk.system.metadata.MdAttributeBoolean;
import com.runwaysdk.system.metadata.MdAttributeCharacter;
import com.runwaysdk.system.metadata.MdAttributeConcrete;
import com.runwaysdk.system.metadata.MdAttributeDateTime;
import com.runwaysdk.system.metadata.MdAttributeDouble;
import com.runwaysdk.system.metadata.MdAttributeLong;
import com.runwaysdk.system.metadata.MdBusiness;

import net.geoprism.DefaultConfiguration;
import net.geoprism.registry.io.GeoObjectUtil;
import net.geoprism.registry.progress.Progress;
import net.geoprism.registry.progress.ProgressService;
import net.geoprism.registry.query.GeoObjectIterator;
import net.geoprism.registry.query.GeoObjectQuery;
import net.geoprism.registry.service.ConversionService;
import net.geoprism.registry.service.LocaleSerializer;
import net.geoprism.registry.service.ServiceFactory;

public class MasterList extends MasterListBase
{
  private static final long serialVersionUID = 190790165;

  public static String      TYPE_CODE        = "typeCode";

  public static String      ATTRIBUTES       = "attributes";

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

  @Transaction
  public JsonObject publish()
  {
    this.lock();

    try
    {
      MdBusinessDAO mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid()).getBusinessDAO();
      mdBusiness.deleteAllRecords();

      List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

      Universal universal = this.getUniversal();
      GeoObjectType type = ServiceFactory.getConversionService().universalToGeoObjectType(universal);

      // Add the type ancestor fields
      Map<HierarchyType, List<GeoObjectType>> ancestorMap = this.getAncestorMap(type);
      Collection<AttributeType> attributes = type.getAttributeMap().values();

      GeoObjectQuery query = new GeoObjectQuery(type, universal);

      Long count = query.getCount();
      long current = 0;

      ProgressService.put(this.getOid(), new Progress(0L, count, ""));

      try
      {

        GeoObjectIterator objects = query.getIterator();

        try
        {
          while (objects.hasNext())
          {
            Business business = new Business(mdBusiness.definesType());

            GeoObject object = objects.next();

            publish(object, business, attributes, ancestorMap, locales);

            ProgressService.put(this.getOid(), new Progress(current++, count, ""));
          }
        }
        finally
        {
          objects.close();
        }

        this.setPublishDate(new Date());
        this.apply();

        return this.toJSON();
      }
      finally
      {
        ProgressService.remove(this.getOid());
      }
    }
    finally
    {
      this.unlock();
    }
  }

  @SuppressWarnings("unchecked")
  private void publish(GeoObject object, Business business, Collection<AttributeType> attributes, Map<HierarchyType, List<GeoObjectType>> ancestorMap, List<Locale> locales)
  {
    business.setValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME, object.getGeometry());

    for (AttributeType attribute : attributes)
    {
      String name = attribute.getName();

      if (this.isValid(attribute))
      {
        Object value = object.getValue(name);

        if (value != null)
        {

          if (attribute instanceof AttributeTermType)
          {
            Iterator<String> codes = (Iterator<String>) value;

            if (codes.hasNext())
            {
              String code = codes.next();

              Term term = ( (AttributeTermType) attribute ).getTermByCode(code).get();
              LocalizedValue label = term.getLabel();

              business.setValue(name, term.getCode());
              business.setValue(name + "DefaultLocale", label.getValue(LocalizedValue.DEFAULT_LOCALE));

              for (Locale locale : locales)
              {
                business.setValue(name + locale.toString(), label.getValue(locale));
              }
            }
          }
          else if (attribute instanceof AttributeLocalType)
          {
            LocalizedValue label = (LocalizedValue) value;

            business.setValue(name + "DefaultLocale", label.getValue(LocalizedValue.DEFAULT_LOCALE));

            for (Locale locale : locales)
            {
              business.setValue(name + locale.toString(), label.getValue(locale));
            }
          }
          else
          {
            business.setValue(name, value);
          }
        }
      }
    }

    Set<Entry<HierarchyType, List<GeoObjectType>>> entries = ancestorMap.entrySet();

    for (Entry<HierarchyType, List<GeoObjectType>> entry : entries)
    {
      HierarchyType hierarchy = entry.getKey();
      // List<GeoObjectType> parents = entry.getValue();
      Map<String, ValueObject> map = GeoObjectUtil.getAncestorMap(object, hierarchy);

      Set<Entry<String, ValueObject>> locations = map.entrySet();

      for (Entry<String, ValueObject> location : locations)
      {
        String pCode = location.getKey();
        ValueObject vObject = location.getValue();

        if (vObject != null)
        {
          String attributeName = hierarchy.getCode().toLowerCase() + pCode.toLowerCase();
          if (business.hasAttribute(attributeName))
          {
            business.setValue(attributeName, vObject.getValue(GeoEntity.GEOID));
            business.setValue(attributeName + "DefaultLocale", vObject.getValue(DefaultAttribute.DISPLAY_LABEL.getName()));

            for (Locale locale : locales)
            {
              business.setValue(attributeName + locale.toString(), vObject.getValue(DefaultAttribute.DISPLAY_LABEL.getName() + "_" + locale.toString()));
            }
          }
        }
      }
    }

    business.apply();
  }

  @Transaction
  public void updateRecord(GeoObject object)
  {
    MdBusinessDAO mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid()).getBusinessDAO();
    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    Universal universal = this.getUniversal();
    GeoObjectType type = ServiceFactory.getConversionService().universalToGeoObjectType(universal);

    // Add the type ancestor fields
    Map<HierarchyType, List<GeoObjectType>> ancestorMap = this.getAncestorMap(type);
    Collection<AttributeType> attributes = type.getAttributeMap().values();

    BusinessQuery query = new QueryFactory().businessQuery(mdBusiness.definesType());
    query.WHERE(query.aCharacter(DefaultAttribute.CODE.getName()).EQ(object.getCode()));

    List<Business> records = query.getIterator().getAll();

    for (Business record : records)
    {
      this.publish(object, record, attributes, ancestorMap, locales);
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

    MdBusiness mdBusiness = (MdBusiness) BusinessFacade.get(mdTableDAO);

    List<Locale> locales = SupportedLocaleDAO.getSupportedLocales();

    Universal universal = this.getUniversal();
    GeoObjectType type = ServiceFactory.getConversionService().universalToGeoObjectType(universal);

    this.createMdAttributeFromAttributeType(mdBusiness, type.getGeometryType());

    Collection<AttributeType> attributeTypes = type.getAttributeMap().values();

    for (AttributeType attributeType : attributeTypes)
    {
      if (this.isValid(attributeType))
      {
        this.createMdAttributeFromAttributeType(mdBusiness, attributeType, type, locales);
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
          String label = typeLabel + " (" + hierarchyLabel + ")";

          MdAttributeCharacterDAO mdAttributeCode = MdAttributeCharacterDAO.newInstance();
          mdAttributeCode.setValue(MdAttributeCharacterInfo.NAME, attributeName);
          mdAttributeCode.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
          mdAttributeCode.setValue(MdAttributeCharacterInfo.SIZE, "255");
          mdAttributeCode.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, label);
          mdAttributeCode.apply();

          MdAttributeCharacterDAO mdAttributeDefaultLocale = MdAttributeCharacterDAO.newInstance();
          mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.NAME, attributeName + "DefaultLocale");
          mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
          mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
          mdAttributeDefaultLocale.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, label);
          mdAttributeDefaultLocale.apply();

          for (Locale locale : locales)
          {
            MdAttributeCharacterDAO mdAttributeLocale = MdAttributeCharacterDAO.newInstance();
            mdAttributeLocale.setValue(MdAttributeCharacterInfo.NAME, attributeName + locale.toString());
            mdAttributeLocale.setValue(MdAttributeCharacterInfo.DEFINING_MD_CLASS, mdTableDAO.getOid());
            mdAttributeLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
            mdAttributeLocale.setStructValue(MdAttributeCharacterInfo.DISPLAY_LABEL, MdAttributeLocalInfo.DEFAULT_LOCALE, label + " (" + locale + ")");
            mdAttributeLocale.apply();
          }
        }
      }
    }

    return mdBusiness;
  }

  public void createMdAttributeFromAttributeType(MdBusiness mdBusiness, AttributeType attributeType, GeoObjectType type, List<Locale> locales)
  {
    if (! ( attributeType instanceof AttributeTermType || attributeType instanceof AttributeLocalType ))
    {
      MdAttributeConcrete mdAttribute = null;

      if (attributeType.getType().equals(AttributeCharacterType.TYPE))
      {
        mdAttribute = new MdAttributeCharacter();
        MdAttributeCharacter mdAttributeCharacter = (MdAttributeCharacter) mdAttribute;
        mdAttributeCharacter.setDatabaseSize(MdAttributeCharacterInfo.MAX_CHARACTER_SIZE);
      }
      else if (attributeType.getType().equals(AttributeDateType.TYPE))
      {
        mdAttribute = new MdAttributeDateTime();
      }
      else if (attributeType.getType().equals(AttributeIntegerType.TYPE))
      {
        mdAttribute = new MdAttributeLong();
      }
      else if (attributeType.getType().equals(AttributeFloatType.TYPE))
      {
        AttributeFloatType attributeFloatType = (AttributeFloatType) attributeType;

        mdAttribute = new MdAttributeDouble();
        mdAttribute.setValue(MdAttributeDoubleInfo.LENGTH, Integer.toString(attributeFloatType.getPrecision()));
        mdAttribute.setValue(MdAttributeDoubleInfo.DECIMAL, Integer.toString(attributeFloatType.getScale()));
      }
      else if (attributeType.getType().equals(AttributeBooleanType.TYPE))
      {
        mdAttribute = new MdAttributeBoolean();
      }
      else
      {
        throw new UnsupportedOperationException("Unsupported type [" + attributeType.getType() + "]");
      }

      mdAttribute.setAttributeName(attributeType.getName());

      ServiceFactory.getConversionService().populate(mdAttribute.getDisplayLabel(), attributeType.getLabel());
      ServiceFactory.getConversionService().populate(mdAttribute.getDescription(), attributeType.getDescription());

      mdAttribute.setDefiningMdClass(mdBusiness);
      mdAttribute.apply();
    }
    else if (attributeType instanceof AttributeTermType)
    {
      MdAttributeCharacter cloneAttribute = new MdAttributeCharacter();
      cloneAttribute.setValue(MdAttributeConcreteInfo.NAME, attributeType.getName());
      cloneAttribute.setValue(MdAttributeCharacterInfo.SIZE, "255");
      ServiceFactory.getConversionService().populate(cloneAttribute.getDisplayLabel(), attributeType.getLabel());
      ServiceFactory.getConversionService().populate(cloneAttribute.getDescription(), attributeType.getDescription());
      cloneAttribute.setDefiningMdClass(mdBusiness);
      cloneAttribute.apply();

      MdAttributeCharacter mdAttributeDefaultLocale = new MdAttributeCharacter();
      mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.NAME, attributeType.getName() + "DefaultLocale");
      mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
      mdAttributeDefaultLocale.setDefiningMdClass(mdBusiness);
      ServiceFactory.getConversionService().populate(mdAttributeDefaultLocale.getDisplayLabel(), attributeType.getLabel());
      ServiceFactory.getConversionService().populate(mdAttributeDefaultLocale.getDescription(), attributeType.getDescription());
      mdAttributeDefaultLocale.apply();

      for (Locale locale : locales)
      {
        MdAttributeCharacter mdAttributeLocale = new MdAttributeCharacter();
        mdAttributeLocale.setValue(MdAttributeCharacterInfo.NAME, attributeType.getName() + locale.toString());
        mdAttributeLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
        mdAttributeLocale.setDefiningMdClass(mdBusiness);
        ServiceFactory.getConversionService().populate(mdAttributeLocale.getDisplayLabel(), attributeType.getLabel(), " (" + locale.toString() + ")");
        ServiceFactory.getConversionService().populate(mdAttributeLocale.getDescription(), attributeType.getDescription());
        mdAttributeLocale.apply();
      }
    }
    else if (attributeType instanceof AttributeLocalType)
    {
      boolean isDisplayLabel = attributeType.getName().equals(DefaultAttribute.DISPLAY_LABEL.getName());

      MdAttributeCharacter mdAttributeDefaultLocale = new MdAttributeCharacter();
      mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.NAME, attributeType.getName() + "DefaultLocale");
      mdAttributeDefaultLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
      mdAttributeDefaultLocale.setDefiningMdClass(mdBusiness);
      ServiceFactory.getConversionService().populate(mdAttributeDefaultLocale.getDisplayLabel(), isDisplayLabel ? type.getLabel() : attributeType.getLabel());
      ServiceFactory.getConversionService().populate(mdAttributeDefaultLocale.getDescription(), attributeType.getDescription());
      mdAttributeDefaultLocale.apply();

      for (Locale locale : locales)
      {
        MdAttributeCharacter mdAttributeLocale = new MdAttributeCharacter();
        mdAttributeLocale.setValue(MdAttributeCharacterInfo.NAME, attributeType.getName() + locale.toString());
        mdAttributeLocale.setValue(MdAttributeCharacterInfo.SIZE, "255");
        mdAttributeLocale.setDefiningMdClass(mdBusiness);
        ServiceFactory.getConversionService().populate(mdAttributeLocale.getDisplayLabel(), isDisplayLabel ? type.getLabel() : attributeType.getLabel(), " (" + locale.toString() + ")");
        ServiceFactory.getConversionService().populate(mdAttributeLocale.getDescription(), attributeType.getDescription());
        mdAttributeLocale.apply();
      }
    }
  }

  public void createMdAttributeFromAttributeType(MdBusiness mdBusiness, GeometryType attributeType)
  {
    MdAttributeGeometry mdAttribute = null;

    if (attributeType.equals(GeometryType.POINT))
    {
      mdAttribute = new MdAttributePoint();
    }
    else if (attributeType.equals(GeometryType.MULTIPOINT))
    {
      mdAttribute = new MdAttributeMultiPoint();
    }
    else if (attributeType.equals(GeometryType.LINE))
    {
      mdAttribute = new MdAttributeLineString();
    }
    else if (attributeType.equals(GeometryType.MULTILINE))
    {
      mdAttribute = new MdAttributeMultiLineString();
    }
    else if (attributeType.equals(GeometryType.POLYGON))
    {
      mdAttribute = new MdAttributePolygon();
    }
    else if (attributeType.equals(GeometryType.MULTIPOLYGON))
    {
      mdAttribute = new MdAttributeMultiPolygon();
    }

    mdAttribute.setAttributeName(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
    mdAttribute.getDisplayLabel().setValue(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME);
    mdAttribute.setDefiningMdClass(mdBusiness);
    mdAttribute.setSrid(4326);
    mdAttribute.apply();
  }

  public JsonArray getHierarchiesAsJson()
  {
    if (this.getHierarchies() != null && this.getHierarchies().length() > 0)
    {
      return new JsonParser().parse(this.getHierarchies()).getAsJsonArray();
    }

    return new JsonArray();
  }

  private JsonArray getAttributesAsJson()
  {
    JsonArray attributes = new JsonArray();
    String mdBusinessId = this.getMdBusinessOid();

    if (mdBusinessId != null && mdBusinessId.length() > 0)
    {
      MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(mdBusinessId);
      List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdBusiness.definesAttributes();

      Collections.sort(mdAttributes, new Comparator<MdAttributeConcreteDAOIF>()
      {
        @Override
        public int compare(MdAttributeConcreteDAOIF o1, MdAttributeConcreteDAOIF o2)
        {
          return o1.definesAttribute().compareTo(o2.definesAttribute());
        }
      });

      for (MdAttributeConcreteDAOIF mdAttribute : mdAttributes)
      {
        if (this.isValid(mdAttribute))
        {
          JsonObject attribute = new JsonObject();
          attribute.addProperty("name", mdAttribute.definesAttribute());
          attribute.addProperty("label", mdAttribute.getDisplayLabel(Session.getCurrentLocale()));

          attributes.add(attribute);
        }
      }
    }

    return attributes;
  }

  private Map<HierarchyType, List<GeoObjectType>> getAncestorMap(GeoObjectType type)
  {
    Map<HierarchyType, List<GeoObjectType>> map = new HashMap<>();

    JsonArray hierarchies = this.getHierarchiesAsJson();

    for (int i = 0; i < hierarchies.size(); i++)
    {
      JsonObject hierarchy = hierarchies.get(i).getAsJsonObject();

      List<String> pCodes = this.getParentCodes(hierarchy);

      if (pCodes.size() > 0)
      {
        String hCode = hierarchy.get("code").getAsString();
        HierarchyType hierarchyType = ServiceFactory.getAdapter().getMetadataCache().getHierachyType(hCode).get();

        map.put(hierarchyType, ServiceFactory.getUtilities().getAncestors(type, hCode));
      }
    }

    return map;
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

  private boolean isValid(AttributeType attributeType)
  {
    if (attributeType.getName().equals(DefaultAttribute.UID.getName()))
    {
      return false;
    }

    if (attributeType.getName().equals(DefaultAttribute.SEQUENCE.getName()))
    {
      return false;
    }

    if (attributeType.getName().equals(DefaultAttribute.STATUS.getName()))
    {
      return false;
    }

    if (attributeType.getName().equals(DefaultAttribute.LAST_UPDATE_DATE.getName()))
    {
      return false;
    }

    if (attributeType.getName().equals(DefaultAttribute.CREATE_DATE.getName()))
    {
      return false;
    }

    if (attributeType.getName().equals(DefaultAttribute.TYPE.getName()))
    {
      return false;
    }

    return true;
  }

  public boolean isValid(MdAttributeConcreteDAOIF mdAttribute)
  {
    if (mdAttribute.isSystem() || mdAttribute.definesAttribute().equals(DefaultAttribute.UID.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.SEQUENCE.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.LAST_UPDATE_DATE.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.CREATE_DATE.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.TYPE.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(DefaultAttribute.STATUS.getName()))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(RegistryConstants.GEOMETRY_ATTRIBUTE_NAME))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(BusinessInfo.OWNER))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(BusinessInfo.KEY))
    {
      return false;
    }

    if (mdAttribute.definesAttribute().equals(BusinessInfo.DOMAIN))
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

  public JsonObject data(Integer pageNumber, Integer pageSize, String filter)
  {
    DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Session.getCurrentLocale());
    NumberFormat numberFormat = NumberFormat.getInstance(Session.getCurrentLocale());

    JsonArray results = new JsonArray();

    MdBusinessDAOIF mdBusiness = MdBusinessDAO.get(this.getMdBusinessOid());
    List<? extends MdAttributeConcreteDAOIF> mdAttributes = mdBusiness.definesAttributes();

    BusinessQuery query = new QueryFactory().businessQuery(mdBusiness.definesType());

    if (filter != null && filter.length() > 0)
    {
      query.WHERE(query.aCharacter(DefaultAttribute.CODE.getName()).LIKEi("%" + filter + "%"));
    }

    query.ORDER_BY_DESC(query.aCharacter(DefaultAttribute.CODE.getName()));

    OIterator<Business> iterator = query.getIterator(pageSize, pageNumber);

    try
    {
      while (iterator.hasNext())
      {
        Business row = iterator.next();
        JsonObject object = new JsonObject();

        for (MdAttributeConcreteDAOIF mdAttribute : mdAttributes)
        {
          if (this.isValid(mdAttribute))
          {
            String attributeName = mdAttribute.definesAttribute();
            Object value = row.getObjectValue(attributeName);

            if (value != null)
            {

              if (value instanceof Double)
              {
                object.addProperty(mdAttribute.definesAttribute(), numberFormat.format((Double) value));
              }
              else if (value instanceof Number)
              {
                object.addProperty(mdAttribute.definesAttribute(), (Number) value);
              }
              else if (value instanceof Boolean)
              {
                object.addProperty(mdAttribute.definesAttribute(), (Boolean) value);
              }
              else if (value instanceof String)
              {
                object.addProperty(mdAttribute.definesAttribute(), (String) value);
              }
              else if (value instanceof Character)
              {
                object.addProperty(mdAttribute.definesAttribute(), (Character) value);
              }
              else if (value instanceof Date)
              {
                object.addProperty(mdAttribute.definesAttribute(), dateFormat.format((Date) value));
              }
            }
          }
        }

        results.add(object);
      }
    }
    finally
    {
      iterator.close();
    }

    JsonObject page = new JsonObject();
    page.addProperty("pageNumber", pageNumber);
    page.addProperty("pageSize", pageSize);
    page.addProperty("filter", filter);
    page.addProperty("count", query.getCount());
    page.add("results", results);

    return page;
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

    object.add(MasterList.ATTRIBUTES, this.getAttributesAsJson());

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

    MasterList.assignDefaultRolePermissions(mdTable);

    return list;
  }

  private static void assignDefaultRolePermissions(ComponentIF component)
  {
    RoleDAO adminRole = RoleDAO.findRole(DefaultConfiguration.ADMIN).getBusinessDAO();
    adminRole.grantPermission(Operation.CREATE, component.getOid());
    adminRole.grantPermission(Operation.DELETE, component.getOid());
    adminRole.grantPermission(Operation.WRITE, component.getOid());
    adminRole.grantPermission(Operation.WRITE_ALL, component.getOid());

    RoleDAO maintainer = RoleDAO.findRole(RegistryConstants.REGISTRY_MAINTAINER_ROLE).getBusinessDAO();
    maintainer.grantPermission(Operation.CREATE, component.getOid());
    maintainer.grantPermission(Operation.DELETE, component.getOid());
    maintainer.grantPermission(Operation.WRITE, component.getOid());
    maintainer.grantPermission(Operation.WRITE_ALL, component.getOid());

    RoleDAO consumer = RoleDAO.findRole(RegistryConstants.API_CONSUMER_ROLE).getBusinessDAO();
    consumer.grantPermission(Operation.READ, component.getOid());
    consumer.grantPermission(Operation.READ_ALL, component.getOid());

    RoleDAO contributor = RoleDAO.findRole(RegistryConstants.REGISTRY_CONTRIBUTOR_ROLE).getBusinessDAO();
    contributor.grantPermission(Operation.READ, component.getOid());
    contributor.grantPermission(Operation.READ_ALL, component.getOid());
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
