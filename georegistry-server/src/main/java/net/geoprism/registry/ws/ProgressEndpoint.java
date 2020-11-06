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

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.JsonObject;

import net.geoprism.registry.progress.Progress;
import net.geoprism.registry.progress.ProgressService;

@ServerEndpoint(value = "/websocket/progress/{key}", configurator = GetHttpSessionConfigurator.class)
public class ProgressEndpoint
{
  private static Set<ProgressEndpoint> endpoints = new CopyOnWriteArraySet<>();

  private Session                      session;

  private String                       key;

  @OnOpen
  public void onOpen(Session session, EndpointConfig config, @PathParam("key") String key) throws IOException
  {
    this.session = session;
    this.key = key;

    endpoints.add(this);

    // If the key is already in progress then update the client immediately
    Progress progress = ProgressService.get(key);

    if (progress != null)
    {
      this.session.getBasicRemote().sendText(progress.toJson().toString());
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
    endpoints.remove(this);
  }

  @OnError
  public void onError(Session session, Throwable throwable)
  {
    // Do error handling here
  }

  public static void broadcast(String key, JsonObject message)
  {
    final String text = message.toString();

    endpoints.forEach(endpoint -> {
      synchronized (endpoint)
      {
        try
        {
          if (endpoint.key.equals(key))
          {
            endpoint.session.getBasicRemote().sendText(text);
          }
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    });
  }
}