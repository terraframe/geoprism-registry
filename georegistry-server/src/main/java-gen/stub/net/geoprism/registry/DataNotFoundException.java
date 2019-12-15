package net.geoprism.registry;

public class DataNotFoundException extends DataNotFoundExceptionBase
{
  private static final long serialVersionUID = 1157482356;
  
  public DataNotFoundException()
  {
    super();
  }
  
  public DataNotFoundException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public DataNotFoundException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public DataNotFoundException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
