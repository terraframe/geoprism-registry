package net.geoprism.registry;

@com.runwaysdk.business.ClassSignature(hash = 2089579307)
public abstract class CodeLengthExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.CodeLengthException";
  private static final long serialVersionUID = 2089579307;
  
  public CodeLengthExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected CodeLengthExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public CodeLengthExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public CodeLengthExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public CodeLengthExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public CodeLengthExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public CodeLengthExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public CodeLengthExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String LENGTH = "length";
  public static java.lang.String OID = "oid";
  public Integer getLength()
  {
    return com.runwaysdk.constants.MdAttributeIntegerUtil.getTypeSafeValue(getValue(LENGTH));
  }
  
  public void setLength(Integer value)
  {
    if(value == null)
    {
      setValue(LENGTH, "");
    }
    else
    {
      setValue(LENGTH, java.lang.Integer.toString(value));
    }
  }
  
  public boolean isLengthWritable()
  {
    return isWritable(LENGTH);
  }
  
  public boolean isLengthReadable()
  {
    return isReadable(LENGTH);
  }
  
  public boolean isLengthModified()
  {
    return isModified(LENGTH);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeNumberMdDTO getLengthMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeNumberMdDTO) getAttributeDTO(LENGTH).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{length}", this.getLength().toString());
    template = template.replace("{oid}", this.getOid().toString());
    
    return template;
  }
  
}
