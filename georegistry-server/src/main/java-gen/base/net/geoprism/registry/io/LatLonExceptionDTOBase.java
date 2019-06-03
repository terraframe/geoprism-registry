package net.geoprism.registry.io;

@com.runwaysdk.business.ClassSignature(hash = -2046408965)
public abstract class LatLonExceptionDTOBase extends com.runwaysdk.business.SmartExceptionDTO
{
  public final static String CLASS = "net.geoprism.registry.io.LatLonException";
  private static final long serialVersionUID = -2046408965;
  
  public LatLonExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  protected LatLonExceptionDTOBase(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public LatLonExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public LatLonExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public LatLonExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public LatLonExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public LatLonExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public LatLonExceptionDTOBase(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
  protected java.lang.String getDeclaredType()
  {
    return CLASS;
  }
  
  public static java.lang.String LAT = "lat";
  public static java.lang.String LON = "lon";
  public static java.lang.String OID = "oid";
  public String getLat()
  {
    return getValue(LAT);
  }
  
  public void setLat(String value)
  {
    if(value == null)
    {
      setValue(LAT, "");
    }
    else
    {
      setValue(LAT, value);
    }
  }
  
  public boolean isLatWritable()
  {
    return isWritable(LAT);
  }
  
  public boolean isLatReadable()
  {
    return isReadable(LAT);
  }
  
  public boolean isLatModified()
  {
    return isModified(LAT);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getLatMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(LAT).getAttributeMdDTO();
  }
  
  public String getLon()
  {
    return getValue(LON);
  }
  
  public void setLon(String value)
  {
    if(value == null)
    {
      setValue(LON, "");
    }
    else
    {
      setValue(LON, value);
    }
  }
  
  public boolean isLonWritable()
  {
    return isWritable(LON);
  }
  
  public boolean isLonReadable()
  {
    return isReadable(LON);
  }
  
  public boolean isLonModified()
  {
    return isModified(LON);
  }
  
  public final com.runwaysdk.transport.metadata.AttributeTextMdDTO getLonMd()
  {
    return (com.runwaysdk.transport.metadata.AttributeTextMdDTO) getAttributeDTO(LON).getAttributeMdDTO();
  }
  
  /**
   * Overrides java.lang.Throwable#getMessage() to retrieve the localized
   * message from the exceptionDTO, instead of from a class variable.
   */
  public String getMessage()
  {
    java.lang.String template = super.getMessage();
    
    template = template.replace("{lat}", this.getLat().toString());
    template = template.replace("{lon}", this.getLon().toString());
    template = template.replace("{oid}", this.getOid().toString());
    
    return template;
  }
  
}
