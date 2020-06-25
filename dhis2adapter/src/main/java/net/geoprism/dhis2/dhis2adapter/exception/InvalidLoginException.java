package net.geoprism.dhis2.dhis2adapter.exception;

public class InvalidLoginException extends Exception
{

  private static final long serialVersionUID = 5138509402364454307L;
  
  public InvalidLoginException() {
      super();
  }

  public InvalidLoginException(String message) {
      super(message);
  }

  public InvalidLoginException(String message, Throwable cause) {
      super(message, cause);
  }

  public InvalidLoginException(Throwable cause) {
      super(cause);
  }

  protected InvalidLoginException(String message, Throwable cause,
                      boolean enableSuppression,
                      boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
  }
  
}
