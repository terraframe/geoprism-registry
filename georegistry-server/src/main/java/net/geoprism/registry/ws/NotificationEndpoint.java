/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.ws;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.constants.ClientConstants;
import com.runwaysdk.web.WebClientSession;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/websocket/notify", configurator = GetHttpSessionConfigurator.class)
public class NotificationEndpoint extends MessageEndpoint
{
  private static Set<NotificationEndpoint> endpoints = new CopyOnWriteArraySet<>();

  private static Logger                    logger    = LoggerFactory.getLogger(NotificationEndpoint.class);

  private Session                          session;

  private String                           userId;

  @OnOpen
  public void onOpen(Session session, EndpointConfig config) throws IOException
  {
    HttpSession s = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
    WebClientSession clientSession = (WebClientSession) s.getAttribute(ClientConstants.CLIENTSESSION);

    this.session = session;

    if (!clientSession.getRequest().isPublicUser())
    {
      this.userId = clientSession.getRequest().getSessionUser().getOid();
    }

    if (this.userId != null)
    {
      endpoints.add(this);

      logger.debug("Connecting websocket for user [" + this.userId + "]");
    }
  }

  // @OnMessage
  // public void onMessage(Session session) throws IOException
  // {
  // // Do nothing
  // }

  @OnClose
  public void onClose(Session session) throws IOException
  {
    if (this.userId != null)
    {
      endpoints.remove(this);

      logger.debug("Closing websocket for user [" + this.userId + "]");
    }
  }

  public static void broadcast(String userId, NotificationMessage message)
  {
    String text = getText(message);

    logger.debug("Broadcasting to [" + userId + "]: " + text);

    endpoints.forEach(endpoint -> {
      synchronized (endpoint)
      {
        if (endpoint.userId.equals(userId))
        {
          sendText(endpoint.session, text);
        }
      }
    });
  }

  public static void broadcast(NotificationMessage message)
  {
    String text = getText(message);

    logger.debug("Broadcasting to [all]: " + text);

    endpoints.forEach(endpoint -> {
      synchronized (endpoint)
      {
        sendText(endpoint.session, text);
      }
    });
  }

}