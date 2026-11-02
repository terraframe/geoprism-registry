package net.geoprism.registry.ws;

public class HeartbeatMessage extends NotificationMessage
{

  public HeartbeatMessage()
  {
    super(MessageType.HB);
  }

  @Override
  public void run()
  {
    // Broadcast the heartbeat to all websocket endpoints
    NotificationEndpoint.broadcast(this);

    ProgressEndpoint.broadcast(this);
  }

}
