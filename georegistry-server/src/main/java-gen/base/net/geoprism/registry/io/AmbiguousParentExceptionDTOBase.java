package net.geoprism.registry.io;

@com.runwaysdk.business.ClassSignature(hash = 824774792)
public abstract class AmbiguousParentExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.io.AmbiguousParentException";
  private static final long serialVersionUID = 824774792;
  
  public AmbiguousParentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected AmbiguousParentExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public AmbiguousParentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public AmbiguousParentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public AmbiguousParentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public AmbiguousParentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public AmbiguousParentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public AmbiguousParentExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String CONTEXT = "context";
  public static java.lang.String OID = "oid";
  public static java.lang.String PARENTLABEL = "parentLabel";
  public String getContext()
  {
    return getValue(CONTEXT);
  }
  
  public void setContext(String value)
  {
    if(value == null)
    {
      setValue(CONTEXT, "");
    }
    else
    {
      setValue(CONTEXT, value);
    }
  }
  
  public boolean isContextWritable()
  {
    return isWritable(CONTEXT);
  }
  
  public boolean isContextReadable()
  {
    return isReadable(CONTEXT);
  }
  
  public boolean isContextModified()
  {
    return isModified(CONTEXT);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getContextMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(CONTEXT).getAttributeMdDTO();
  }
  
  public String getParentLabel()
  {
    return getValue(PARENTLABEL);
  }
  
  public void setParentLabel(String value)
  {
    if(value == null)
    {
      setValue(PARENTLABEL, "");
    }
    else
    {
      setValue(PARENTLABEL, value);
    }
  }
  
  public boolean isParentLabelWritable()
  {
    return isWritable(PARENTLABEL);
  }
  
  public boolean isParentLabelReadable()
  {
    return isReadable(PARENTLABEL);
  }
  
  public boolean isParentLabelModified()
  {
    return isModified(PARENTLABEL);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getParentLabelMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(PARENTLABEL).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{context}", this.getContext().toString());
    template = template.replace("{oid}", this.getOid().toString());
    template = template.replace("{parentLabel}", this.getParentLabel().toString());
    
    return template;
  }
  
}
