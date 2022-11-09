package net.geoprism.registry.exception;

@com.runwaysdk.business.ClassSignature(hash = 1937233765)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to DuplicateExternalIdException.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class DuplicateExternalIdExceptionBase extends com.runwaysdk.business.SmartException
{
  public final static String CLASS = "net.geoprism.registry.exception.DuplicateExternalIdException";
  public static java.lang.String EXTERNALID = "externalId";
  public static java.lang.String EXTERNALSYSTEM = "externalSystem";
  public static java.lang.String OID = "oid";
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1937233765;
  
  public DuplicateExternalIdExceptionBase()
  {
    super();
  }
  
  public DuplicateExternalIdExceptionBase(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public DuplicateExternalIdExceptionBase(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public DuplicateExternalIdExceptionBase(java.lang.Throwable cause)
  {
    super(cause);
  }
  
  public String getExternalId()
  {
    return getValue(EXTERNALID);
  }
  
  public void validateExternalId()
  {
    this.validateAttribute(EXTERNALID);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getExternalIdMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.exception.DuplicateExternalIdException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(EXTERNALID);
  }
  
  public void setExternalId(String value)
  {
    if(value == null)
    {
      setValue(EXTERNALID, "");
    }
    else
    {
      setValue(EXTERNALID, value);
    }
  }
  
  public String getExternalSystem()
  {
    return getValue(EXTERNALSYSTEM);
  }
  
  public void validateExternalSystem()
  {
    this.validateAttribute(EXTERNALSYSTEM);
  }
  
  public static com.runwaysdk.dataaccess.MdAttributeTextDAOIF getExternalSystemMd()
  {
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.exception.DuplicateExternalIdException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeTextDAOIF)mdClassIF.definesAttribute(EXTERNALSYSTEM);
  }
  
  public void setExternalSystem(String value)
  {
    if(value == null)
    {
      setValue(EXTERNALSYSTEM, "");
    }
    else
    {
      setValue(EXTERNALSYSTEM, value);
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.exception.DuplicateExternalIdException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(OID);
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public java.lang.String localize(java.util.Locale locale)
  {
    java.lang.String message = super.localize(locale);
    message = replace(message, "{externalId}", this.getExternalId());
    message = replace(message, "{externalSystem}", this.getExternalSystem());
    message = replace(message, "{oid}", this.getOid());
    return message;
  }
  
}
