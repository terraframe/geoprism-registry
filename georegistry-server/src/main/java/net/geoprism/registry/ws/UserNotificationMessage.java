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
