package net.geoprism.registry;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.runwaysdk.dataaccess.ProgrammingErrorException;

public class RegistryJsonTimeFormatter extends TypeAdapter<Date>
{

  @Override
  public void write(JsonWriter out, Date value) throws IOException
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
    
    out.value(sdf.format(value));
  }

  @Override
  public Date read(JsonReader in) throws IOException
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sdf.setTimeZone(GeoRegistryUtil.SYSTEM_TIMEZONE);
    
    try
    {
      return sdf.parse(in.nextString());
    }
    catch (ParseException | IOException e)
    {
      throw new ProgrammingErrorException(e);
    }
  }
  
}
