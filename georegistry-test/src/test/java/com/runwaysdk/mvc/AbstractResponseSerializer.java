package com.runwaysdk.mvc;

/**
 * Maybe a hack, but we need access to the 'serialize' method on AbstractResponse and we're getting it this way via package level access.
 */
public class AbstractResponseSerializer
{
  public static Object serialize(AbstractRestResponse resp)
  {
    return resp.serialize();
  }
}
