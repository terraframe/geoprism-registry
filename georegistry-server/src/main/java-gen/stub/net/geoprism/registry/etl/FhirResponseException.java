package net.geoprism.registry.etl;

public class FhirResponseException extends FhirResponseExceptionBase
{
  private static final long serialVersionUID = -1814857084;
  
  public FhirResponseException()
  {
    super();
  }
  
  public FhirResponseException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public FhirResponseException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public FhirResponseException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
