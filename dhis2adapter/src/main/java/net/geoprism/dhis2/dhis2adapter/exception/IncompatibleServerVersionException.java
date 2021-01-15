package net.geoprism.dhis2.dhis2adapter.exception;

public class IncompatibleServerVersionException extends Exception
{
  private static final long serialVersionUID = 6432902440602083768L;

  private int requestedVersion;
  
  private int remoteServerVersion;
  
  public IncompatibleServerVersionException(int requestedVersion, int remoteServerVersion)
  {
    this.requestedVersion = requestedVersion;
    this.remoteServerVersion = remoteServerVersion;
  }

  public int getRequestedVersion()
  {
    return requestedVersion;
  }

  public void setRequestedVersion(int requestedVersion)
  {
    this.requestedVersion = requestedVersion;
  }

  public int getRemoteServerVersion()
  {
    return remoteServerVersion;
  }

  public void setRemoteServerVersion(int remoteServerVersion)
  {
    this.remoteServerVersion = remoteServerVersion;
  }
}
