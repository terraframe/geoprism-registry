package net.geoprism.registry;

public class TypeInUseException extends TypeInUseExceptionBase
{
  private static final long serialVersionUID = 1092269110;
  
  public TypeInUseException()
  {
    super();
  }
  
  public TypeInUseException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public TypeInUseException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public TypeInUseException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
