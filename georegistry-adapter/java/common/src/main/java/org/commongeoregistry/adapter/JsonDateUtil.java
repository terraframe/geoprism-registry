/**
 *
 */
package org.commongeoregistry.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class JsonDateUtil
{
  public static Date parse(String dateStr)
  {
    if (dateStr != null)
    {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

      try
      {
        return formatter.parse(dateStr);
      }
      catch (ParseException e)
      {
        throw new RuntimeException(e);
      }
    }
    
    return null;
  }

  public static String format(Date date)
  {
    if (date != null)
    {

      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

      return formatter.format(date);
    }

    return null;
  }
}
