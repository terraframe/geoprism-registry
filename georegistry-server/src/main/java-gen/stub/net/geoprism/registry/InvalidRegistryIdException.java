package net.geoprism.registry;

import net.geoprism.registry.InvalidRegistryIdExceptionBase;

public class InvalidRegistryIdException extends InvalidRegistryIdExceptionBase
{
  private static final long serialVersionUID = 1366592182;
  
  public InvalidRegistryIdException()
  {
    super();
  }
  
  public InvalidRegistryIdException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public InvalidRegistryIdException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public InvalidRegistryIdException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
