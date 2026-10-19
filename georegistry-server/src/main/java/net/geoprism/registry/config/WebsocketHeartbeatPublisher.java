package net.geoprism.registry.config;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.geoprism.registry.ws.GlobalNotificationMessage;
import net.geoprism.registry.ws.MessageType;
import net.geoprism.registry.ws.NotificationFacade;

@Component
public class WebsocketHeartbeatPublisher
{

  @Scheduled(fixedRate = 30_000)
  public void sendHeartbeat()
  {
    NotificationFacade.queue(new GlobalNotificationMessage(MessageType.HB, null));
  }
}