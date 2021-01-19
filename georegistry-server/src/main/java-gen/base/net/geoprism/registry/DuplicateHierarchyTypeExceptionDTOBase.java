package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = 379541657)
public abstract class DuplicateHierarchyTypeExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.DuplicateHierarchyTypeException";
  private static final long serialVersionUID = 379541657;
  
  public DuplicateHierarchyTypeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected DuplicateHierarchyTypeExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public DuplicateHierarchyTypeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public DuplicateHierarchyTypeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public DuplicateHierarchyTypeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public DuplicateHierarchyTypeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public DuplicateHierarchyTypeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public DuplicateHierarchyTypeExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String DUPLICATEVALUE = "duplicateValue";
  public static java.lang.String OID = "oid";
  public String getDuplicateValue()
  {
    return getValue(DUPLICATEVALUE);
  }
  
  public void setDuplicateValue(String value)
  {
    if(value == null)
    {
      setValue(DUPLICATEVALUE, "");
    }
    else
    {
      setValue(DUPLICATEVALUE, value);
    }
  }
  
  public boolean isDuplicateValueWritable()
  {
    return isWritable(DUPLICATEVALUE);
  }
  
  public boolean isDuplicateValueReadable()
  {
    return isReadable(DUPLICATEVALUE);
  }
  
  public boolean isDuplicateValueModified()
  {
    return isModified(DUPLICATEVALUE);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getDuplicateValueMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(DUPLICATEVALUE).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{duplicateValue}", this.getDuplicateValue().toString());
    template = template.replace("{oid}", this.getOid().toString());
    
    return template;
  }
  
}
