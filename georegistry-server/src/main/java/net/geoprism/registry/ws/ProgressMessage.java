package net.geoprism.registry.ws;

import net.geoprism.registry.progress.Progress;

public class ProgressMessage extends NotificationMessage
{
  private String key;

  public ProgressMessage(String key, Progress progress)
  {
    super(MessageType.PROGRESS, progress.toJson());

    this.key = key;
  }

  public String getKey()
  {
    return key;
  }

  public void setKey(String key)
  {
    this.key = key;
  }

  @Override
  public void run()
  {
    ProgressEndpoint.broadcast(this.key, this.getMessage());
  }

}
