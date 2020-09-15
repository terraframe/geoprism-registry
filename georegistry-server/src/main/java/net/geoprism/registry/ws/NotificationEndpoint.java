/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
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

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.runwaysdk.constants.ClientConstants;
import com.runwaysdk.web.WebClientSession;

@ServerEndpoint(value = "/websocket/notify", configurator = GetHttpSessionConfigurator.class)
public class NotificationEndpoint
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

  @OnError
  public void onError(Session session, Throwable throwable)
  {
    // Do error handling here
  }

  public static void broadcast(String userId, JsonObject message)
  {
    logger.debug("Broadcasting to [" + userId + "]: " + message.toString());

    endpoints.forEach(endpoint -> {
      synchronized (endpoint)
      {
        try
        {
          if (endpoint.userId.equals(userId))
          {
            endpoint.session.getBasicRemote().sendText(message.toString());
          }
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  public static void broadcast(JsonObject message)
  {
    logger.debug("Broadcasting to [all]: " + message.toString());

    endpoints.forEach(endpoint -> {
      synchronized (endpoint)
      {
        try
        {
          endpoint.session.getBasicRemote().sendText(message.toString());
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    });
  }

}