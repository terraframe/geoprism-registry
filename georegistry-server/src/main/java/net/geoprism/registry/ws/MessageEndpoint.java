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
