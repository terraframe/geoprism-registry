package net.geoprism.registry.etl;

public class DuplicateJobException extends DuplicateJobExceptionBase
{
  private static final long serialVersionUID = 1687777231;
  
  public DuplicateJobException()
  {
    super();
  }
  
  public DuplicateJobException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public DuplicateJobException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public DuplicateJobException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
