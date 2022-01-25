package net.geoprism.registry;

public class CannotDeleteClassificationWithChildrenException extends CannotDeleteClassificationWithChildrenExceptionBase
{
  private static final long serialVersionUID = 965048281;
  
  public CannotDeleteClassificationWithChildrenException()
  {
    super();
  }
  
  public CannotDeleteClassificationWithChildrenException(java.lang.String developerMessage)
  {
    super(developerMessage);
  }
  
  public CannotDeleteClassificationWithChildrenException(java.lang.String developerMessage, java.lang.Throwable cause)
  {
    super(developerMessage, cause);
  }
  
  public CannotDeleteClassificationWithChildrenException(java.lang.Throwable cause)
  {
    super(cause);
  }
  
}
