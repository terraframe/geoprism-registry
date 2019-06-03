package net.geoprism.registry;

public class CannotDeleteGeoObjectTypeWithChildrenDTO extends CannotDeleteGeoObjectTypeWithChildrenDTOBase
{
  private static final long serialVersionUID = 1999957188;
  
  public CannotDeleteGeoObjectTypeWithChildrenDTO(com.runwaysdk.constants.ClientRequestIF clientRequestIF)
  {
    super(clientRequestIF);
  }
  
  public CannotDeleteGeoObjectTypeWithChildrenDTO(com.runwaysdk.business.ExceptionDTO exceptionDTO)
  {
    super(exceptionDTO);
  }
  
  public CannotDeleteGeoObjectTypeWithChildrenDTO(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale)
  {
    super(clientRequest, locale);
  }
  
  public CannotDeleteGeoObjectTypeWithChildrenDTO(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale,java.lang.String developerMessage)
  {
    super(clientRequest, locale, developerMessage);
  }
  
  public CannotDeleteGeoObjectTypeWithChildrenDTO(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.Throwable cause)
  {
    super(clientRequest, locale, cause);
  }
  
  public CannotDeleteGeoObjectTypeWithChildrenDTO(com.runwaysdk.constants.ClientRequestIF clientRequest, java.util.Locale locale, java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(clientRequest, locale, developerMessage, cause);
  }
  
  public CannotDeleteGeoObjectTypeWithChildrenDTO(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.Throwable cause)
  {
    super(clientRequest, cause);
  }
  
  public CannotDeleteGeoObjectTypeWithChildrenDTO(com.runwaysdk.constants.ClientRequestIF clientRequest, java.lang.String msg, java.lang.Throwable cause)
  {
    super(clientRequest, msg, cause);
  }
  
}
