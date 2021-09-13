package net.geoprism.registry.geoobject;

@com.runwaysdk.business.ClassSignature(hash = 882041771)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to ValueOutOfRangeException.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class ValueOutOfRangeExceptionBase extends com.runwaysdk.business.SmartException
{
  public final static String CLASS = "net.geoprism.registry.geoobject.ValueOutOfRangeException";
  public static java.lang.String ATTRIBUTE = "attribute";
  public static java.lang.String ENDDATE = "endDate";
  public static java.lang.String GEOOBJECT = "geoObject";
  public static java.lang.String OID = "oid";
  public static java.lang.String STARTDATE = "startDate";
  private static final long serialVersionUID = 882041771;
  
  public ValueOutOfRangeExceptionBase()
  {
    super();
  }
  
  public ValueOutOfRangeExceptionBase(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public ValueOutOfRangeExceptionBase(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public ValueOutOfRangeExceptionBase(java.lang.Throwable cause)
  {
    super(cause);
  }
  
  public String getAttribute()
  {
    return getValue(ATTRIBUTE);
  }
  
  public void validateAttribute()
  {
    this.validateAttribute(ATTRIBUTE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getAttributeMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.geoobject.ValueOutOfRangeException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(ATTRIBUTE);
  }
  
  public void setAttribute(String value)
  {
    if(value == null)
    {
      setValue(ATTRIBUTE, "");
    }
    else
    {
      setValue(ATTRIBUTE, value);
    }
  }
  
  public String getEndDate()
  {
    return getValue(ENDDATE);
  }
  
  public void validateEndDate()
  {
    this.validateAttribute(ENDDATE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getEndDateMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.geoobject.ValueOutOfRangeException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(ENDDATE);
  }
  
  public void setEndDate(String value)
  {
    if(value == null)
    {
      setValue(ENDDATE, "");
    }
    else
    {
      setValue(ENDDATE, value);
    }
  }
  
  public String getGeoObject()
  {
    return getValue(GEOOBJECT);
  }
  
  public void validateGeoObject()
  {
    this.validateAttribute(GEOOBJECT);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getGeoObjectMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.geoobject.ValueOutOfRangeException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(GEOOBJECT);
  }
  
  public void setGeoObject(String value)
  {
    if(value == null)
    {
      setValue(GEOOBJECT, "");
    }
    else
    {
      setValue(GEOOBJECT, value);
    }
  }
  
  public String getOid()
  {
    return getValue(OID);
  }
  
  public void validateOid()
  {
    this.validateAttribute(OID);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF getOidMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.geoobject.ValueOutOfRangeException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(OID);
  }
  
  public String getStartDate()
  {
    return getValue(STARTDATE);
  }
  
  public void validateStartDate()
  {
    this.validateAttribute(STARTDATE);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getStartDateMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.geoobject.ValueOutOfRangeException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(STARTDATE);
  }
  
  public void setStartDate(String value)
  {
    if(value == null)
    {
      setValue(STARTDATE, "");
    }
    else
    {
      setValue(STARTDATE, value);
    }
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public java.lang.String localize(java.util.Locale locale)
  {
    java.lang.String message = super.localize(locale);
    message = replace(message, "{attribute}", this.getAttribute());
    message = replace(message, "{endDate}", this.getEndDate());
    message = replace(message, "{geoObject}", this.getGeoObject());
    message = replace(message, "{oid}", this.getOid());
    message = replace(message, "{startDate}", this.getStartDate());
    return message;
  }
  
}
