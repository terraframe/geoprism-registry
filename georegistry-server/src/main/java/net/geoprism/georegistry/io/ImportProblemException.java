package net.geoprism.georegistry.io;

public class ImportProblemException extends RuntimeException
{

  /**
   * 
   */
  private static final long serialVersionUID = 3981837089663189908L;

  public ImportProblemException()
  {
    super();
  }

  public ImportProblemException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public ImportProblemException(String message)
  {
    super(message);
  }

  public ImportProblemException(Throwable cause)
  {
    super(cause);
  }

}
