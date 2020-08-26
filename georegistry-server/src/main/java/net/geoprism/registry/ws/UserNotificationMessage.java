package net.geoprism.registry.ws;

import org.json.JSONObject;

import com.runwaysdk.session.SessionIF;

public class UserNotificationMessage extends NotificationMessage
{
  private String userId;

  public UserNotificationMessage(SessionIF session, MessageType type, JSONObject content)
  {
    this(session.getUser().getOid(), type, content);
  }

  public UserNotificationMessage(String userId, MessageType type, JSONObject content)
  {
    super(type, content);

    this.userId = userId;
  }

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  @Override
  public void run()
  {
    NotificationEndpoint.broadcast(this.userId, this.getMessage());
  }

}
