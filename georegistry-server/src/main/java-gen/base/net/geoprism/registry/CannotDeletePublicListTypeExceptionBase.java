package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = 825516070)
/**
 * This class is generated automatically.
 * DO NOT MAKE CHANGES TO IT - THEY WILL BE OVERWRITTEN
 * Custom business logic should be added to CannotDeletePublicListTypeException.java
 *
 * @author Autogenerated by RunwaySDK
 */
public abstract class CannotDeletePublicListTypeExceptionBase extends com.runwaysdk.business.SmartException
{
  public final static String CLASS = "net.geoprism.registry.CannotDeletePublicListTypeException";
  public static java.lang.String OID = "oid";
  private static final long serialVersionUID = 825516070;
  
  public CannotDeletePublicListTypeExceptionBase()
  {
    super();
  }
  
  public CannotDeletePublicListTypeExceptionBase(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public CannotDeletePublicListTypeExceptionBase(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public CannotDeletePublicListTypeExceptionBase(java.lang.Throwable cause)
  {
    super(cause);
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
    com.runwaysdk.dataaccess.MdClassDAOIF mdClassIF = com.runwaysdk.dataaccess.metadata.MdClassDAO.getMdClassDAO(net.geoprism.registry.CannotDeletePublicListTypeException.CLASS);
    return (com.runwaysdk.dataaccess.MdAttributeUUIDDAOIF)mdClassIF.definesAttribute(OID);
  }
  
  protected String getDeclaredType()
  {
    return CLASS;
  }
  
  public java.lang.String localize(java.util.Locale locale)
  {
    java.lang.String message = super.localize(locale);
    message = replace(message, "{oid}", this.getOid());
    return message;
  }
  
}