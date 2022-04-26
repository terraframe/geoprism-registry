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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.commongeoregistry.adapter.metadata.AttributeType;

public class ValueOverTimeCollectionDTO implements Collection<ValueOverTimeDTO>
{
  private LinkedList<ValueOverTimeDTO> valuesOverTime;
  
  private AttributeType attributeType;
  
  public ValueOverTimeCollectionDTO(AttributeType attributeType)
  {
    this.valuesOverTime = new LinkedList<ValueOverTimeDTO>();
    this.attributeType = attributeType;
  }
  
  public boolean add(ValueOverTimeDTO dto)
  {
    return this.valuesOverTime.add(dto);
  }
  
  public ValueOverTimeDTO get(int i)
  {
    return this.valuesOverTime.get(i);
  }
  
  @Override
  public Iterator<ValueOverTimeDTO> iterator()
  {
    return this.valuesOverTime.iterator();
  }
  
  @Override
  public int size()
  {
    return this.valuesOverTime.size();
  }
  
  public AttributeType getAttributeType()
  {
    return attributeType;
  }

  public void setAttributeType(AttributeType type)
  {
    this.attributeType = type;
  }
  
  /**
   * Returns the Attribute which exists exactly at the given startDate. Does not include between values.
   * 
   * @param date
   * @return
   */
  public Attribute getAttributeAtStartDate(Date date)
  {
    Date localDate = ValueOverTimeDTO.toLocal(date);
    
    for (ValueOverTimeDTO vot : this.valuesOverTime)
    {
      if (vot.getStartDate().equals(localDate))
      {
        return vot.getAttribute();
      }
    }
    
    return null;
  }
  
  public ValueOverTimeDTO getOrCreate(Date date)
  {
    if (date == null)
    {
      date = new Date();
    }
    
    ValueOverTimeDTO vot = getAtStartDate(date);
    
    if (vot != null) { return vot; }
    
    vot = new ValueOverTimeDTO(null, date, null, this);
    this.add(vot);
    return vot;
  }
  
  public ValueOverTimeDTO getAtStartDate(Date date)
  {
    Date localDate = ValueOverTimeDTO.toLocal(date);
    
    for (ValueOverTimeDTO vot : this.valuesOverTime)
    {
      if (vot.getStartDate().equals(localDate))
      {
        return vot;
      }
    }
    
    return null;
  }
  
  public Attribute getOrCreateAttribute(Date date)
  {
    if (date == null)
    {
      date = new Date();
    }
    
    Attribute attr = getAttributeAtStartDate(date);
    
    if (attr != null) { return attr; }
    
    ValueOverTimeDTO vot = new ValueOverTimeDTO(null, date, null, this);
    this.add(vot);
    return vot.getAttribute();
  }
  
  public Attribute getAttributeOnDate(Date date)
  {
    Date localDate = ValueOverTimeDTO.toLocal(date);
    
    for (ValueOverTimeDTO vot : this.valuesOverTime)
    {
      if (vot.between(localDate))
      {
        return vot.getAttribute();
      }
    }
    
    return null;
  }
  
  public Object getValueOnDate(Date date)
  {
    for (ValueOverTimeDTO vot : this.valuesOverTime)
    {
      if (vot.between(date))
      {
        return vot.getValue();
      }
    }
    
    return Attribute.attributeFactory(this.getAttributeType()).getValue();
  }
  
  public void setValue(Object value, Date date)
  {
    for (ValueOverTimeDTO vot : this.valuesOverTime)
    {
      if (vot.between(date))
      {
        vot.setValue(value);
        return;
      }
    }
  }

//  public JsonArray toJSON(CustomSerializer serializer)
//  {
//    JsonArray ret = new JsonArray();
//    
//    for (ValueOverTimeDTO vot : this.valuesOverTime)
//    {
//      ret.add(vot.toJSON(serializer));
//    }
//    
//    return ret;
//  }
//  
//  public static ValueOverTimeCollectionDTO fromJSON(String json, AttributeType type, RegistryAdapter adapter)
//  {
//    ValueOverTimeCollectionDTO ret = new ValueOverTimeCollectionDTO(type);
//    JsonArray ja = new JsonParser().parse(json).getAsJsonArray();
//    
//    for (int i = 0; i < ja.size(); ++i)
//    {
//      ValueOverTimeDTO vot = ValueOverTimeDTO.fromJSON(ja.get(i).toString(), type, adapter);
//      
//      ret.add(vot);
//    }
//    
//    return ret;
//  }

  @Override
  public boolean isEmpty()
  {
    return valuesOverTime.isEmpty();
  }

  @Override
  public boolean contains(Object o)
  {
    return valuesOverTime.contains(o);
  }

  @Override
  public Object[] toArray()
  {
    return valuesOverTime.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a)
  {
    return valuesOverTime.toArray(a);
  }

  @Override
  public boolean remove(Object o)
  {
    return valuesOverTime.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c)
  {
    return valuesOverTime.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends ValueOverTimeDTO> c)
  {
    return valuesOverTime.addAll(c);
  }

  @Override
  public boolean removeAll(Collection<?> c)
  {
    return valuesOverTime.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c)
  {
    return valuesOverTime.retainAll(c);
  }

  @Override
  public void clear()
  {
    valuesOverTime.clear();
  }
  
  @Override
  public String toString()
  {
    String ret = "ValueOverTimeCollectionDTO [";

    List<String> vots = new ArrayList<String>();
    for (ValueOverTimeDTO vot : this)
    {
      vots.add(vot.toString());
      
      ret = ret + vot.toString() + ", ";
    }

    return ret;
  }
}
