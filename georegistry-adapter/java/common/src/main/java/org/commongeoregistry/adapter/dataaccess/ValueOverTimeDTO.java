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
package org.commongeoregistry.adapter.dataaccess;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.commongeoregistry.adapter.RegistryAdapter;
import org.commongeoregistry.adapter.metadata.CustomSerializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ValueOverTimeDTO
{
  public static final Date INFINITY_END_DATE;

  static
  {
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    cal.clear();
    cal.set(5000, Calendar.DECEMBER, 31);

    INFINITY_END_DATE = cal.getTime();
  }
  
  private Date   startDate;

  private Date endDate;
  
  private String oid;
  
  private Attribute attribute;

  private ValueOverTimeCollectionDTO collection;
  
  public ValueOverTimeDTO(String oid, Date startDate, Date endDate, ValueOverTimeCollectionDTO collection)
  {
    this.oid = oid;
    this.collection = collection;
    this.attribute = Attribute.attributeFactory(collection.getAttributeType());
    
    this.setStartDate(startDate);
    this.setEndDate(endDate);
  }
  
  public boolean between(Date date)
  {
    Date localDate = toLocal(date);
    Date endDate = this.getEndDate();
    Date startDate = this.getStartDate();
    
    return ( startDate.equals(localDate) || startDate.before(localDate) ) && ( endDate.equals(localDate) || endDate.after(localDate) );
  }
  
  public static Date toLocal(Date date) {
    if (date == null)
    {
      date = INFINITY_END_DATE;
    }
    
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    cal.clear();
    cal.setTime(date);
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return cal.getTime();
    
//    DateUtils.truncate(date, Calendar.DATE);
//    DateUtils.
    
//    DateUtils
    
//    Calendar cal = Calendar.getInstance();
//    cal.setTime(date);
//    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
//    cal.set(Calendar.HOUR_OF_DAY, 0);
//    cal.set(Calendar.MINUTE, 0);
//    cal.set(Calendar.SECOND, 0);
//    cal.set(Calendar.MILLISECOND, 0);
//    return cal.getTime();
}
  
  public JsonObject toJSON(CustomSerializer serializer)
  {
    JsonObject ret = new JsonObject();
    
    SimpleDateFormat format = getTimeFormatter();
    
    ret.addProperty("oid", this.getOid());
    ret.addProperty("startDate", format.format(this.getStartDate()));
    ret.addProperty("endDate", format.format(this.getEndDate()));
    
    JsonElement value = this.attribute.toJSON(serializer);
    ret.add("value", value);
    
    return ret;
  }
  
  public static SimpleDateFormat getTimeFormatter()
  {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
    return format;
  }
  
  public static ValueOverTimeDTO fromJSON(String json, ValueOverTimeCollectionDTO collection, RegistryAdapter registry)
  {
    JsonObject jo = new JsonParser().parse(json).getAsJsonObject();
    
    SimpleDateFormat format = getTimeFormatter();
    
    try
    {
      String oid = null;
      if (jo.has("oid"))
      {
        oid = jo.get("oid").getAsString();
      }
      
      Date startDate = format.parse(jo.get("startDate").getAsString());
      Date endDate = format.parse(jo.get("endDate").getAsString());
      
      ValueOverTimeDTO ret = new ValueOverTimeDTO(oid, startDate, endDate, collection);
      
      ret.attribute.fromJSON(jo.get("value"), registry);
      
      return ret;
    }
    catch (ParseException e)
    {
      throw new RuntimeException(e); // TODO : Error handling
    }
  }

  public Date getStartDate()
  {
//    return Date.from(startDate.atStartOfDay().atZone(ZoneId.of("Z")).toInstant());
    return startDate;
  }

  public void setStartDate(Date startDate)
  {
//    this.startDate = startDate.toInstant().atZone(ZoneId.of("Z")).toLocalDate();
    this.startDate = toLocal(startDate);
  }
  
//  public LocalDate getLocalStartDate()
//  {
//    return this.startDate;
//  }

  public Date getEndDate()
  {
    if (endDate == null)
    {
      return INFINITY_END_DATE;
    }
    
//    return Date.from(endDate.atStartOfDay().atZone(ZoneId.of("Z")).toInstant());
    return endDate;
  }
  
//  public LocalDate getLocalEndDate()
//  {
//    return this.endDate;
//  }

  public void setEndDate(Date endDate)
  {
    if (endDate != null)
    {
//      this.endDate = endDate.toInstant().atZone(ZoneId.of("Z")).toLocalDate();
      this.endDate = toLocal(endDate);
    }
    else
    {
      this.endDate = INFINITY_END_DATE;
    }
  }

  public Object getValue()
  {
    return this.attribute.getValue();
  }

  public void setValue(Object value)
  {
    this.attribute.setValue(value);
  }
  
  public String getOid()
  {
    return oid;
  }

  public void setOid(String oid)
  {
    this.oid = oid;
  }

  public Attribute getAttribute()
  {
    return attribute;
  }

  public void setAttribute(Attribute attribute)
  {
    this.attribute = attribute;
  }
  
  @Override
  public String toString()
  {
    DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");

    String endDate = "null";
    if (this.endDate != null)
    {
      endDate = dateFormat.format(this.endDate);
    }

    return "value [" + this.attribute.getValue() + "] from " + dateFormat.format(this.startDate) + " to " + endDate;
  }
  
}
