/**
 *
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
