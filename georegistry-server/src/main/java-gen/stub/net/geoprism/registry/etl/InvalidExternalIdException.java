package net.geoprism.registry.etl;

public class InvalidExternalIdException extends InvalidExternalIdExceptionBase
{
  private static final long serialVersionUID = 2016984114;
  
  public InvalidExternalIdException()
  {
    super();
  }
  
  public InvalidExternalIdException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public InvalidExternalIdException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public InvalidExternalIdException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
