package net.geoprism.registry.exception;

public class DuplicateExternalIdException extends DuplicateExternalIdExceptionBase
{
  @SuppressWarnings("unused")
  private static final long serialVersionUID = -272137624;
  
  public DuplicateExternalIdException()
  {
    super();
  }
  
  public DuplicateExternalIdException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public DuplicateExternalIdException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public DuplicateExternalIdException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
