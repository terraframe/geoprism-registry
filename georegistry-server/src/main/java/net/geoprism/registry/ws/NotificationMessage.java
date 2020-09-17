/**
 * Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Geoprism Registry(tm).
 *
 * Geoprism Registry(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Geoprism Registry(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Geoprism Registry(tm). If not, see <http://www.gnu.org/licenses/>.
 */
package net.geoprism.registry.ws;

import com.google.gson.JsonObject;

public abstract class NotificationMessage implements Runnable
{

  private MessageType type;

  private JsonObject  content;

  public NotificationMessage(MessageType type, JsonObject content)
  {
    super();
    this.type = type;
    this.content = content;
  }

  public JsonObject getMessage()
  {
    JsonObject message = new JsonObject();
    message.addProperty("type", this.type.name());

    if (this.content != null)
    {
      message.add("content", this.content);
    }

    return message;
  }

  public JsonObject getContent()
  {
    return content;
  }

  public void setContent(JsonObject content)
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
