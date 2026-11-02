package net.geoprism.registry.ws;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.websocket.OnError;
import jakarta.websocket.Session;
import tools.jackson.databind.json.JsonMapper;

public abstract class MessageEndpoint
{
  private static Logger logger = LoggerFactory.getLogger(ProgressEndpoint.class);

  @OnError
  public void onError(Session session, Throwable throwable)
  {
    logger.error("Error in websocket", throwable);
  }

  public static String getText(NotificationMessage message)
  {
    JsonMapper mapper = JsonMapper.shared();
    return mapper.writeValueAsString(message);
  }

  public static void sendText(Session session, String text)
  {
    try
    {
      session.getBasicRemote().sendText(text);
    }
    catch (IOException e)
    {
      logger.error("Failed to broadscast websocket message", e);
    }
  }

}
