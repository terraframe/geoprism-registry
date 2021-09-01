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
package net.geoprism.registry.view.action;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.commongeoregistry.adapter.Term;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.LocalizedValue;
import org.commongeoregistry.adapter.metadata.AttributeBooleanType;
import org.commongeoregistry.adapter.metadata.AttributeDateType;
import org.commongeoregistry.adapter.metadata.AttributeFloatType;
import org.commongeoregistry.adapter.metadata.AttributeIntegerType;
import org.commongeoregistry.adapter.metadata.AttributeLocalType;
import org.commongeoregistry.adapter.metadata.AttributeTermType;
import org.commongeoregistry.adapter.metadata.AttributeType;
import org.wololo.jts2geojson.GeoJSONReader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.annotations.JsonAdapter;
import com.runwaysdk.constants.MdAttributeLocalInfo;
import com.runwaysdk.dataaccess.MdAttributeEmbeddedDAOIF;
import com.runwaysdk.dataaccess.MdVertexDAOIF;
import com.runwaysdk.dataaccess.graph.GraphObjectDAO;
import com.runwaysdk.dataaccess.graph.VertexObjectDAO;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.localization.LocalizationFacade;
import com.vividsolutions.jts.geom.Geometry;

import net.geoprism.ontology.Classifier;
import net.geoprism.registry.GeoObjectStatus;
import net.geoprism.registry.GeometryTypeException;
import net.geoprism.registry.RegistryJsonTimeFormatter;
import net.geoprism.registry.action.ExecuteOutOfDateChangeRequestException;
import net.geoprism.registry.action.InvalidChangeRequestException;
import net.geoprism.registry.conversion.TermConverter;
import net.geoprism.registry.model.ServerGeoObjectType;
import net.geoprism.registry.model.graph.VertexServerGeoObject;
import net.geoprism.registry.service.ConversionService;

public class UpdateValueOverTimeView
{
  protected String oid;
  
  public enum UpdateActionType {
    DELETE,
    UPDATE,
    CREATE
  }
  
  protected UpdateActionType action;
  
  protected JsonElement oldValue;
  
  protected JsonElement newValue;
  
  @JsonAdapter(RegistryJsonTimeFormatter.class)
  protected Date newStartDate;
  
  @JsonAdapter(RegistryJsonTimeFormatter.class)
  protected Date newEndDate;
  
  @JsonAdapter(RegistryJsonTimeFormatter.class)
  protected Date oldStartDate;
  
  @JsonAdapter(RegistryJsonTimeFormatter.class)
  protected Date oldEndDate;
  
  /*
   * You should NOT be directly setting values on the VOTC contained within the GeoObject here. Use the looseVotc instead.
   * For reasons why, {{@see UpdateChangeOverTimeAttributeView.execute}}
   */
  public void execute(UpdateChangeOverTimeAttributeView cotView, VertexServerGeoObject go, List<ValueOverTime> looseVotc)
  {
    if (this.action.equals(UpdateActionType.DELETE))
    {
      ValueOverTime vot = this.getValueByOid(looseVotc, this.getOid());
      
      if (vot == null)
      {
        ExecuteOutOfDateChangeRequestException ex = new ExecuteOutOfDateChangeRequestException();
        throw ex;
      }
      
      looseVotc.remove(vot);
    }
    else if (this.action.equals(UpdateActionType.UPDATE))
    {
      ValueOverTime vot = this.getValueByOid(looseVotc, this.getOid());
      
      if (vot == null)
      {
        ExecuteOutOfDateChangeRequestException ex = new ExecuteOutOfDateChangeRequestException();
        throw ex;
      }
      
      if (this.newValue == null && this.newStartDate == null && this.newEndDate == null)
      {
        throw new InvalidChangeRequestException();
      }
      
      if (newStartDate != null)
      {
        vot.setStartDate(newStartDate);
      }
      
      if (newEndDate != null)
      {
        vot.setEndDate(newEndDate);
      }
      
      this.persistValue(vot, cotView, go, looseVotc);
    }
    else if (this.action.equals(UpdateActionType.CREATE))
    {
      ValueOverTime vot = this.getValueByDate(looseVotc, this.newStartDate, this.newEndDate);
      
      if (vot != null)
      {
        ExecuteOutOfDateChangeRequestException ex = new ExecuteOutOfDateChangeRequestException();
        throw ex;
      }
      
      if (this.newValue == null || this.newStartDate == null || this.newEndDate == null)
      {
        throw new InvalidChangeRequestException();
      }
      
      this.persistValue(null, cotView, go, looseVotc);
    }
    else
    {
      throw new UnsupportedOperationException("Unsupported action type [" + this.action + "].");
    }
  }
  
  protected ValueOverTime getValueByOid(List<ValueOverTime> looseVotc, String oid)
  {
    for (ValueOverTime vot : looseVotc)
    {
      if (vot.getOid().equals(oid))
      {
        return vot;
      }
    }
    
    return null;
  }
  
  protected ValueOverTime getValueByDate(List<ValueOverTime> looseVotc, Date startDate, Date endDate)
  {
    for (ValueOverTime vt : looseVotc)
    {
      if (vt.getStartDate().equals(startDate) && vt.getEndDate().equals(endDate))
      {
        return vt;
      }
    }

    return null;
  }
  
  private void persistValue(ValueOverTime vot, UpdateChangeOverTimeAttributeView cotView, VertexServerGeoObject go, List<ValueOverTime> looseVotc)
  {
    if (this.newValue == null)
    {
      return;
    }
    
    if (cotView.getAttributeName().equals("geometry"))
    {
      Geometry convertedValue = null;
      
      if (! this.newValue.isJsonNull())
      {
        GeoJSONReader reader = new GeoJSONReader();
        convertedValue = reader.read(this.newValue.toString());
        
        if (!go.isValidGeometry(convertedValue))
        {
          GeometryTypeException ex = new GeometryTypeException();
          ex.setActualType(convertedValue.getGeometryType());
          ex.setExpectedType(go.getType().getGeometryType().name());

          throw ex;
        }
      }
      
      if (vot != null)
      {
        vot.setValue(convertedValue);
      }
      else
      {
        looseVotc.add(new ValueOverTime(this.newStartDate, this.newEndDate, convertedValue));
      }
    }
    else
    {
      ServerGeoObjectType type = go.getType();
      
      AttributeType attype = type.getAttribute(cotView.getAttributeName()).get();
      
      if (attype instanceof AttributeLocalType)
      {
        LocalizedValue convertedValue = null;
        
        if (! this.newValue.isJsonNull())
        {
          convertedValue = LocalizedValue.fromJSON(this.newValue.getAsJsonObject());
        }
        
        final Set<Locale> locales = LocalizationFacade.getInstalledLocales(); 
        
        if (vot != null)
        {
          if (convertedValue != null)
          {
            GraphObjectDAO votEmbeddedValue = (GraphObjectDAO) vot.getValue();
            
            votEmbeddedValue.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, convertedValue.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE));
    
            for (Locale locale : locales)
            {
              if (convertedValue.contains(locale))
              {
                votEmbeddedValue.setValue(locale.toString(), convertedValue.getValue(locale));
              }
            }
          }
          else
          {
            vot.setValue(null);
          }
        }
        else
        {
          if (convertedValue != null)
          {
            MdAttributeEmbeddedDAOIF mdAttrEmbedded = (MdAttributeEmbeddedDAOIF) go.getMdAttributeDAO(attype.getName());
            VertexObjectDAO votEmbeddedValue = VertexObjectDAO.newInstance((MdVertexDAOIF) mdAttrEmbedded.getEmbeddedMdClassDAOIF());
            
            votEmbeddedValue.setValue(MdAttributeLocalInfo.DEFAULT_LOCALE, convertedValue.getValue(MdAttributeLocalInfo.DEFAULT_LOCALE));
            
            for (Locale locale : locales)
            {
              if (convertedValue.contains(locale))
              {
                votEmbeddedValue.setValue(locale.toString(), convertedValue.getValue(locale));
              }
            }
            
            looseVotc.add(new ValueOverTime(this.newStartDate, this.newEndDate, votEmbeddedValue));
          }
          else
          {
            looseVotc.add(new ValueOverTime(this.newStartDate, this.newEndDate, null));
          }
        }
      }
      else if (attype.getName().equals(DefaultAttribute.STATUS.getName()))
      {
        if (this.newValue.isJsonNull())
        {
          if (vot != null)
          {
            vot.setValue(null);
          }
          else
          {
            looseVotc.add(new ValueOverTime(this.newStartDate, this.newEndDate, null));
          }
        }
        else
        {
          JsonArray ja = this.newValue.getAsJsonArray();
          
          if (ja.size() > 0)
          {
            String code = ja.get(0).getAsString();
            
            if (code == null || code.length() == 0)
            {
              if (vot != null)
              {
                vot.setValue(null);
              }
              else
              {
                looseVotc.add(new ValueOverTime(this.newStartDate, this.newEndDate, null));
              }
            }
            else
            {
              Term value = ( (AttributeTermType) attype ).getTermByCode(code).get();
              GeoObjectStatus gos = ConversionService.getInstance().termToGeoObjectStatus(value);
    
              if (vot != null)
              {
                vot.setValue(gos.getOid());
              }
              else
              {
                looseVotc.add(new ValueOverTime(this.newStartDate, this.newEndDate, gos.getOid()));
              }
            }
          }
        }
      }
      else
      {
        Object convertedValue = null;
        
        if (! this.newValue.isJsonNull())
        {
          if (attype instanceof AttributeDateType)
          {
            long epoch = this.newValue.getAsLong();
            
            convertedValue = new Date(epoch);
          }
          else if (attype instanceof AttributeTermType)
          {
            JsonArray ja = this.newValue.getAsJsonArray();
            
            if (ja.size() > 0)
            {
              String code = ja.get(0).getAsString();
              
              Term root = ( (AttributeTermType) attype ).getRootTerm();
              String parent = TermConverter.buildClassifierKeyFromTermCode(root.getCode());

              String classifierKey = Classifier.buildKey(parent, code);
              Classifier classifier = Classifier.getByKey(classifierKey);
              
              convertedValue = classifier.getOid();
            }
          }
          else if (attype instanceof AttributeBooleanType)
          {
            convertedValue = this.newValue.getAsBoolean();
          }
          else if (attype instanceof AttributeFloatType)
          {
            convertedValue = this.newValue.getAsDouble();
          }
          else if (attype instanceof AttributeIntegerType)
          {
            convertedValue = this.newValue.getAsLong();
          }
          else
          {
            convertedValue = this.newValue.getAsString();
          }
        }
        
        if (vot != null)
        {
          vot.setValue(convertedValue);
        }
        else
        {
          looseVotc.add(new ValueOverTime(this.newStartDate, this.newEndDate, convertedValue));
        }
      }
    }
  }

  public String getOid()
  {
    return oid;
  }

  public void setOid(String oid)
  {
    this.oid = oid;
  }

  public UpdateActionType getAction()
  {
    return action;
  }

  public void setAction(UpdateActionType action)
  {
    this.action = action;
  }

  public JsonElement getOldValue()
  {
    return oldValue;
  }

  public void setOldValue(JsonElement oldValue)
  {
    this.oldValue = oldValue;
  }

  public JsonElement getNewValue()
  {
    return newValue;
  }

  public void setNewValue(JsonElement newValue)
  {
    this.newValue = newValue;
  }

  public Date getNewStartDate()
  {
    return newStartDate;
  }

  public void setNewStartDate(Date newStartDate)
  {
    this.newStartDate = newStartDate;
  }

  public Date getNewEndDate()
  {
    return newEndDate;
  }

  public void setNewEndDate(Date newEndDate)
  {
    this.newEndDate = newEndDate;
  }

  public Date getOldStartDate()
  {
    return oldStartDate;
  }

  public void setOldStartDate(Date oldStartDate)
  {
    this.oldStartDate = oldStartDate;
  }

  public Date getOldEndDate()
  {
    return oldEndDate;
  }

  public void setOldEndDate(Date oldEndDate)
  {
    this.oldEndDate = oldEndDate;
  }
}
