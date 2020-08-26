package net.geoprism.registry.ws;

import org.json.JSONObject;

public abstract class NotificationMessage implements Runnable
{

  private MessageType type;

  private JSONObject  content;

  public NotificationMessage(MessageType type, JSONObject content)
  {
    super();
    this.type = type;
    this.content = content;
  }

  public JSONObject getMessage()
  {
    JSONObject message = new JSONObject();
    message.put("type", this.type.name());

    if (this.content != null)
    {
      message.put("content", this.content);
    }

    return message;
  }

  public JSONObject getContent()
  {
    return content;
  }

  public void setContent(JSONObject content)
  {
    this.content = content;
  }

  public MessageType getType()
  {
    return type;
  }

  public void setType(MessageType type)
  {
    this.type = type;
  }
}
