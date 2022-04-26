/**
 * Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
 *
 * This file is part of Common Geo Registry Adapter(tm).
 *
 * Common Geo Registry Adapter(tm) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Common Geo Registry Adapter(tm) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Common Geo Registry Adapter(tm).  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commongeoregistry.adapter.http;

import java.net.HttpURLConnection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ResponseProcessor
{
  public static void validateStatusCode(HttpResponse resp) throws ServerResponseException
  {
    int statusCode = resp.getStatusCode();

    if (statusCode != HttpURLConnection.HTTP_OK && statusCode != HttpURLConnection.HTTP_CREATED)
    {
      String content = resp.getAsString();

      if (content != null && content.length() > 0)
      {
        try
        {
          JsonParser parser = new JsonParser();
          JsonObject object = parser.parse(content).getAsJsonObject();

          String message = object.get("developerMessage").getAsString();
          String type = object.get("wrappedException").getAsString();
          String localizedMessage = object.get("localizedMessage").getAsString();

          throw new ServerResponseException(message, statusCode, type, localizedMessage);
        }
        catch (ServerResponseException t)
        {
          throw t;
        }
        catch (Throwable t)
        {
          throw new ServerResponseException(content, statusCode, null, null);
        }
      }
      else
      {
        throw new ServerResponseException(content, statusCode, null, null);
      }
    }
  }
}
