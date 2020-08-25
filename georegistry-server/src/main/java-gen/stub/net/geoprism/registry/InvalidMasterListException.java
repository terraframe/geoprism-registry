package net.geoprism.registry;

public class InvalidMasterListException extends InvalidMasterListExceptionBase
{
  private static final long serialVersionUID = 767154407;
  
  public InvalidMasterListException()
  {
    super();
  }
  
  public InvalidMasterListException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public InvalidMasterListException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public InvalidMasterListException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
