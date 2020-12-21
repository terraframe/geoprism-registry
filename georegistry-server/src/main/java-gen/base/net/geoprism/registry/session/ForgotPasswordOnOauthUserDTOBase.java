package net.geoprism.registry.session;

@com.runwaysdk.business.ClassSignature(hash = -574482639)
public abstract class ForgotPasswordOnOauthUserDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.session.ForgotPasswordOnOauthUser";
  private static final long serialVersionUID = -574482639;
  
  public ForgotPasswordOnOauthUserDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected ForgotPasswordOnOauthUserDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public ForgotPasswordOnOauthUserDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public ForgotPasswordOnOauthUserDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public ForgotPasswordOnOauthUserDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public ForgotPasswordOnOauthUserDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public ForgotPasswordOnOauthUserDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public ForgotPasswordOnOauthUserDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String OAUTHSERVER = "oauthServer";
  public static java.lang.String OID = "oid";
  public static java.lang.String USERNAME = "username";
  public String getOauthServer()
  {
    return getValue(OAUTHSERVER);
  }
  
  public void setOauthServer(String value)
  {
    if(value == null)
    {
      setValue(OAUTHSERVER, "");
    }
    else
    {
      setValue(OAUTHSERVER, value);
    }
  }
  
  public boolean isOauthServerWritable()
  {
    return isWritable(OAUTHSERVER);
  }
  
  public boolean isOauthServerReadable()
  {
    return isReadable(OAUTHSERVER);
  }
  
  public boolean isOauthServerModified()
  {
    return isModified(OAUTHSERVER);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getOauthServerMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(OAUTHSERVER).getAttributeMdDTO();
  }
  
  public String getUsername()
  {
    return getValue(USERNAME);
  }
  
  public void setUsername(String value)
  {
    if(value == null)
    {
      setValue(USERNAME, "");
    }
    else
    {
      setValue(USERNAME, value);
    }
  }
  
  public boolean isUsernameWritable()
  {
    return isWritable(USERNAME);
  }
  
  public boolean isUsernameReadable()
  {
    return isReadable(USERNAME);
  }
  
  public boolean isUsernameModified()
  {
    return isModified(USERNAME);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getUsernameMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(USERNAME).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{oauthServer}", this.getOauthServer().toString());
    template = template.replace("{oid}", this.getOid().toString());
    template = template.replace("{username}", this.getUsername().toString());
    
    return template;
  }
  
}
